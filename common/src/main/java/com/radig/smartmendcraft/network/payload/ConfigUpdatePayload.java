package com.radig.smartmendcraft.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.radig.smartmendcraft.SmartMendCraft;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigUpdatePayload(
        boolean repairMainHand,
        boolean repairOffHand,
        boolean repairArmor,
        boolean repairInventory,
        List<Integer> priorityOrdinals,
        int inventoryModeOrdinal
) implements CustomPacketPayload {

    public static final Type<ConfigUpdatePayload> TYPE =
            new Type<>(SmartMendCraft.id("config_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigUpdatePayload> CODEC =
            StreamCodec.of(
                    ConfigUpdatePayload::write,
                    ConfigUpdatePayload::read
            );

    private static void write(
            RegistryFriendlyByteBuf buffer,
            ConfigUpdatePayload payload) {

        buffer.writeBoolean(payload.repairMainHand());
        buffer.writeBoolean(payload.repairOffHand());
        buffer.writeBoolean(payload.repairArmor());
        buffer.writeBoolean(payload.repairInventory());

        buffer.writeInt(payload.priorityOrdinals().size());

        for (int ordinal : payload.priorityOrdinals()) {
            buffer.writeInt(ordinal);
        }

        buffer.writeInt(payload.inventoryModeOrdinal());
    }

    private static ConfigUpdatePayload read(
            RegistryFriendlyByteBuf buffer) {

        boolean repairMainHand = buffer.readBoolean();
        boolean repairOffHand = buffer.readBoolean();
        boolean repairArmor = buffer.readBoolean();
        boolean repairInventory = buffer.readBoolean();

        int priorityCount = buffer.readInt();

        if (priorityCount != MendingTarget.values().length) {
            throw new IllegalArgumentException(
                    "Invalid priority count: " + priorityCount
            );
        }

        List<Integer> priorityOrdinals =
                new ArrayList<>(priorityCount);

        for (int i = 0; i < priorityCount; i++) {
            priorityOrdinals.add(
                    buffer.readInt()
            );
        }

        int inventoryModeOrdinal =
                buffer.readInt();

        return new ConfigUpdatePayload(
                repairMainHand,
                repairOffHand,
                repairArmor,
                repairInventory,
                priorityOrdinals,
                inventoryModeOrdinal
        );
    }

    public static ConfigUpdatePayload fromConfig(
            PlayerMendingConfig config) {

        List<Integer> priorityOrdinals =
                config.getPriority()
                        .stream()
                        .map(MendingTarget::ordinal)
                        .toList();

        return new ConfigUpdatePayload(
                config.isRepairMainHand(),
                config.isRepairOffHand(),
                config.isRepairArmor(),
                config.isRepairInventory(),
                priorityOrdinals,
                config.getInventoryMode().ordinal()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}