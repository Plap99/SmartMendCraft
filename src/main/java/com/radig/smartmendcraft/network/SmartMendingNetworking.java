package com.radig.smartmendcraft.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.radig.smartmendcraft.SmartMendCraft;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.hotbar.HotbarPreferences;
import com.radig.smartmendcraft.network.payload.ClientHelloPayload;
import com.radig.smartmendcraft.network.payload.ConfigSyncPayload;
import com.radig.smartmendcraft.network.payload.ConfigUpdatePayload;
import com.radig.smartmendcraft.network.payload.HotbarPreferencesPayload;
import com.radig.smartmendcraft.network.payload.MendingUpdatePayload;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

public final class SmartMendingNetworking {

    private static final Channel<CustomPacketPayload> CHANNEL = createChannel();

    private SmartMendingNetworking() {
    }

    private static Channel<CustomPacketPayload> createChannel() {
        var play = ChannelBuilder.named(SmartMendCraft.id("main"))
                .networkProtocolVersion(1)
                .optional()
                .payloadChannel()
                .play()
                .bidirectional();

        play.bidirectional().addMain(HotbarPreferencesPayload.TYPE, HotbarPreferencesPayload.CODEC,
                (payload, context) -> {
                    if (context.isServerSide()) {
                        handleHotbarPreferences(payload, context);
                    } else {
                        com.radig.smartmendcraft.client.SmartMendingClientNetworking.handleHotbarPreferences(payload);
                    }
                });

        play.serverbound()
                .addMain(ClientHelloPayload.TYPE, ClientHelloPayload.CODEC,
                        SmartMendingNetworking::handleClientHello)
                .addMain(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC,
                        SmartMendingNetworking::handleConfigUpdate);

        play.clientbound()
                .addMain(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC,
                        (payload, context) -> com.radig.smartmendcraft.client.SmartMendCraftClient
                                .handleConfigSync(payload))
                .addMain(MendingUpdatePayload.TYPE, MendingUpdatePayload.CODEC,
                        (payload, context) -> com.radig.smartmendcraft.client.SmartMendCraftClient
                                .handleMendingUpdate(payload));

        return play.build();
    }

    public static void register() {
        // Channel construction registers every payload before the mod can accept connections.
    }

    private static void handleHotbarPreferences(HotbarPreferencesPayload payload,
            net.minecraftforge.event.network.CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !SmartMendingClients.hasSmartMendCraft(player)
                || !payload.rules().stream().allMatch(HotbarPreferences::isValidRule)) {
            return;
        }

        PlayerMendingConfig config = PlayerMendingConfigs.get(player);
        HotbarPreferences preferences = new HotbarPreferences(payload.enabled(), payload.rules());
        if (!preferences.equals(config.getHotbar())) {
            config.setHotbar(preferences);
            PlayerMendingConfigStorage.save(player.getUUID(), config);
        }
        sendHotbarPreferences(player, config);
    }

    private static void handleClientHello(ClientHelloPayload payload,
            net.minecraftforge.event.network.CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        SmartMendingClients.register(player);
        PlayerMendingConfig config = PlayerMendingConfigs.get(player);
        sendConfigSync(player, config);
        sendHotbarPreferences(player, config);
    }

    private static void handleConfigUpdate(ConfigUpdatePayload payload,
            net.minecraftforge.event.network.CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !SmartMendingClients.hasSmartMendCraft(player)) {
            return;
        }

        List<Integer> priorityOrdinals = payload.priorityOrdinals();
        if (priorityOrdinals.size() != MendingTarget.values().length) {
            return;
        }

        List<MendingTarget> priority = new ArrayList<>();
        Set<MendingTarget> seen = new HashSet<>();
        for (int ordinal : priorityOrdinals) {
            if (ordinal < 0 || ordinal >= MendingTarget.values().length) {
                return;
            }
            MendingTarget target = MendingTarget.values()[ordinal];
            if (!seen.add(target)) {
                return;
            }
            priority.add(target);
        }

        int inventoryModeOrdinal = payload.inventoryModeOrdinal();
        if (inventoryModeOrdinal < 0 || inventoryModeOrdinal >= InventoryMendingMode.values().length) {
            return;
        }

        PlayerMendingConfig config = PlayerMendingConfigs.get(player);
        config.setRepairMainHand(payload.repairMainHand());
        config.setRepairOffHand(payload.repairOffHand());
        config.setRepairArmor(payload.repairArmor());
        config.setRepairInventory(payload.repairInventory());
        config.setPriority(List.copyOf(priority));
        config.setInventoryMode(InventoryMendingMode.values()[inventoryModeOrdinal]);
        PlayerMendingConfigStorage.save(player.getUUID(), config);
    }

    private static void sendHotbarPreferences(ServerPlayer player, PlayerMendingConfig config) {
        if (isRemotePresent(player)) {
            CHANNEL.send(new HotbarPreferencesPayload(config.getHotbar()), PacketDistributor.PLAYER.with(player));
        }
    }

    public static void sendMendingUpdate(ServerPlayer player, ItemStack stack, int repairAmount) {
        if (player == null || stack == null || stack.isEmpty() || repairAmount <= 0
                || !SmartMendingClients.hasSmartMendCraft(player) || !isRemotePresent(player)) {
            return;
        }
        CHANNEL.send(new MendingUpdatePayload(stack, repairAmount), PacketDistributor.PLAYER.with(player));
    }

    public static void sendConfigSync(ServerPlayer player, PlayerMendingConfig config) {
        if (player == null || config == null || !SmartMendingClients.hasSmartMendCraft(player)
                || !isRemotePresent(player)) {
            return;
        }
        CHANNEL.send(ConfigSyncPayload.fromConfig(config), PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
    }

    public static boolean canSendToServer(Connection connection) {
        return connection != null && CHANNEL.isRemotePresent(connection);
    }

    private static boolean isRemotePresent(ServerPlayer player) {
        return CHANNEL.isRemotePresent(player.connection.getConnection());
    }
}
