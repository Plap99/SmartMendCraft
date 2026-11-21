package com.radig.smartmendcraft.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.radig.smartmendcraft.SmartMendCraft;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigSyncPayload(
        boolean repairMainHand,
        boolean repairOffHand,
        boolean repairArmor,
        boolean repairInventory,
        List<MendingTarget> priority,
        InventoryMendingMode inventoryMode
) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE =
            new Type<>(SmartMendCraft.id("config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC =
            StreamCodec.of(
                    ConfigSyncPayload::write,
                    ConfigSyncPayload::read
            );

    private static void write(
            RegistryFriendlyByteBuf buffer,
            ConfigSyncPayload payload) {

        buffer.writeBoolean(payload.repairMainHand());
        buffer.writeBoolean(payload.repairOffHand());
        buffer.writeBoolean(payload.repairArmor());
        buffer.writeBoolean(payload.repairInventory());

        List<MendingTarget> priority =
                payload.priority();

        buffer.writeInt(priority.size());

        for (MendingTarget target : priority) {
            buffer.writeInt(target.ordinal());
        }

        buffer.writeInt(
                payload.inventoryMode().ordinal()
        );
    }

    private static ConfigSyncPayload read(
            RegistryFriendlyByteBuf buffer) {

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

        List<MendingTarget> priority =
                new ArrayList<>();

        for (int i = 0; i < priorityCount; i++) {

            int ordinal =
                    buffer.readInt();

            priority.add(
                    MendingTarget.values()[ordinal]
            );
        }

        int inventoryModeOrdinal =
                buffer.readInt();

        InventoryMendingMode inventoryMode =
                InventoryMendingMode.values()[
                        inventoryModeOrdinal
                ];

        return new ConfigSyncPayload(
                repairMainHand,
                repairOffHand,
                repairArmor,
                repairInventory,
                priority,
                inventoryMode
        );
    }

    public static ConfigSyncPayload fromConfig(
            PlayerMendingConfig config) {

        return new ConfigSyncPayload(
                config.isRepairMainHand(),
                config.isRepairOffHand(),
                config.isRepairArmor(),
                config.isRepairInventory(),
                List.copyOf(config.getPriority()),
                config.getInventoryMode()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}