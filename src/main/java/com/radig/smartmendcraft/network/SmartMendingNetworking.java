package com.radig.smartmendcraft.network;

import com.radig.smartmendcraft.SmartMendCraft;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;

import java.util.List;

import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;

public final class SmartMendingNetworking {

    public static final Identifier MENDING_UPDATE =
            SmartMendCraft.id("mending_update");

    public static final Identifier CONFIG_UPDATE =
            SmartMendCraft.id("config_update");

    public static final Identifier CONFIG_SYNC =
            SmartMendCraft.id("config_sync");

    private SmartMendingNetworking() {
    }

    public static void sendMendingUpdate(
            ServerPlayerEntity player,
            ItemStack stack,
            int repairAmount) {

        if (player == null
                || stack == null
                || stack.isEmpty()
                || repairAmount <= 0) {

            return;
        }

        PacketByteBuf buffer = PacketByteBufs.create();

        /*
         * Enviamos una copia del objeto para que el cliente
         * pueda renderizar exactamente su icono.
         */
        buffer.writeItemStack(stack);

        /*
         * Cantidad reparada en este evento.
         */
        buffer.writeInt(repairAmount);

        /*
         * Daño actual después de la reparación.
         */
        buffer.writeInt(stack.getDamage());

        /*
         * Durabilidad máxima.
         */
        buffer.writeInt(stack.getMaxDamage());

        ServerPlayNetworking.send(
                player,
                MENDING_UPDATE,
                buffer
        );
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                CONFIG_UPDATE,
                (server, player, handler, buffer, responseSender) -> {

                    boolean repairMainHand =
                            buffer.readBoolean();

                    boolean repairOffHand =
                            buffer.readBoolean();

                    boolean repairArmor =
                            buffer.readBoolean();

                    boolean repairInventory =
                            buffer.readBoolean();

                    int priorityCount =
                            buffer.readInt();

                    MendingTarget[] priority =
                            new MendingTarget[priorityCount];

                    for (int i = 0; i < priorityCount; i++) {

                        int ordinal =
                                buffer.readInt();

                        priority[i] =
                                MendingTarget.values()[ordinal];
                    }

                    int inventoryModeOrdinal =
                            buffer.readInt();

                    InventoryMendingMode inventoryMode =
                            InventoryMendingMode.values()[
                                    inventoryModeOrdinal
                            ];

                    server.execute(() -> {

                        PlayerMendingConfig config =
                                PlayerMendingConfigs.get(player);

                        config.setRepairMainHand(repairMainHand);
                        config.setRepairOffHand(repairOffHand);
                        config.setRepairArmor(repairArmor);
                        config.setRepairInventory(repairInventory);
                        config.setInventoryMode(inventoryMode);

                        config.setPriority(
                                List.of(priority)
                        );

                        PlayerMendingConfigStorage.save(
                                player.getUuid(),
                                config
                        );
                    });
                }
        );
    }

    public static void sendConfigSync(
            ServerPlayerEntity player,
            PlayerMendingConfig config) {

        if (player == null || config == null) {
            return;
        }

        PacketByteBuf buffer =
                PacketByteBufs.create();

        buffer.writeBoolean(
                config.isRepairMainHand()
        );

        buffer.writeBoolean(
                config.isRepairOffHand()
        );

        buffer.writeBoolean(
                config.isRepairArmor()
        );

        buffer.writeBoolean(
                config.isRepairInventory()
        );

        List<MendingTarget> priority =
                config.getPriority();

        buffer.writeInt(
                priority.size()
        );

        for (MendingTarget target : priority) {
            buffer.writeInt(
                    target.ordinal()
            );
        }

        buffer.writeInt(
                config.getInventoryMode().ordinal()
        );

        ServerPlayNetworking.send(
                player,
                CONFIG_SYNC,
                buffer
        );
    }
}