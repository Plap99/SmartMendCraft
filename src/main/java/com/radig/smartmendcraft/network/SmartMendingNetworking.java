package com.radig.smartmendcraft.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;
import com.radig.smartmendcraft.network.payload.ClientHelloPayload;
import com.radig.smartmendcraft.network.payload.ConfigSyncPayload;
import com.radig.smartmendcraft.network.payload.ConfigUpdatePayload;
import com.radig.smartmendcraft.network.payload.MendingUpdatePayload;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class SmartMendingNetworking {

    private SmartMendingNetworking() {
    }

    public static void registerPayloads() {

        PayloadTypeRegistry.serverboundPlay().register(
                ClientHelloPayload.TYPE,
                ClientHelloPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                ConfigUpdatePayload.TYPE,
                ConfigUpdatePayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                MendingUpdatePayload.TYPE,
                MendingUpdatePayload.CODEC
        );
    }

    public static void registerServerReceivers() {

        ServerPlayNetworking.registerGlobalReceiver(
                ClientHelloPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player =
                            context.player();

                    SmartMendingClients.register(player);

                    PlayerMendingConfig config =
                            PlayerMendingConfigs.get(player);

                    sendConfigSync(
                            player,
                            config
                    );
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ConfigUpdatePayload.TYPE,
                (payload, context) -> {

                ServerPlayer player =
                        context.player();

                if (!SmartMendingClients.hasSmartMendCraft(player)) {
                        return;
                }

                List<Integer> priorityOrdinals =
                        payload.priorityOrdinals();

                if (priorityOrdinals.size()
                        != MendingTarget.values().length) {
                        return;
                }

                List<MendingTarget> priority =
                        new ArrayList<>();

                Set<MendingTarget> seen =
                        new HashSet<>();

                for (int ordinal : priorityOrdinals) {

                        if (ordinal < 0
                                || ordinal >= MendingTarget.values().length) {
                        return;
                        }

                        MendingTarget target =
                                MendingTarget.values()[ordinal];

                        if (!seen.add(target)) {
                        return;
                        }

                        priority.add(target);
                }

                int inventoryModeOrdinal =
                        payload.inventoryModeOrdinal();

                if (inventoryModeOrdinal < 0
                        || inventoryModeOrdinal
                        >= InventoryMendingMode.values().length) {
                        return;
                }

                InventoryMendingMode inventoryMode =
                        InventoryMendingMode.values()[
                                inventoryModeOrdinal
                        ];

                PlayerMendingConfig config =
                        PlayerMendingConfigs.get(player);

                config.setRepairMainHand(
                        payload.repairMainHand()
                );

                config.setRepairOffHand(
                        payload.repairOffHand()
                );

                config.setRepairArmor(
                        payload.repairArmor()
                );

                config.setRepairInventory(
                        payload.repairInventory()
                );

                config.setPriority(
                        List.copyOf(priority)
                );

                config.setInventoryMode(
                        inventoryMode
                );

                PlayerMendingConfigStorage.save(
                        player.getUUID(),
                        config
                );
                }
        );
    }

    public static void sendMendingUpdate(
            ServerPlayer player,
            ItemStack stack,
            int repairAmount) {

        if (player == null) {
            return;
        }

        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (repairAmount <= 0) {
            return;
        }

        if (!SmartMendingClients.hasSmartMendCraft(player)) {
            return;
        }

        MendingUpdatePayload payload =
                new MendingUpdatePayload(
                        stack,
                        repairAmount
                );

        ServerPlayNetworking.send(
                player,
                payload
        );
    }

    public static void sendConfigSync(
            ServerPlayer player,
            PlayerMendingConfig config) {

        if (player == null || config == null) {
            return;
        }

        if (!SmartMendingClients.hasSmartMendCraft(player)) {
            return;
        }

        ConfigSyncPayload payload =
                ConfigSyncPayload.fromConfig(
                        config
                );

        ServerPlayNetworking.send(
                player,
                payload
        );
    }

    private static boolean isValidPriority(
            List<MendingTarget> priority) {

        if (priority == null) {
            return false;
        }

        MendingTarget[] targets =
                MendingTarget.values();

        if (priority.size() != targets.length) {
            return false;
        }

        Set<MendingTarget> found =
                new HashSet<>();

        for (MendingTarget target : priority) {

            if (target == null) {
                return false;
            }

            if (!found.add(target)) {
                return false;
            }
        }

        return found.size() == targets.length;
    }
}