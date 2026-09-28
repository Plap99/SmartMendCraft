package com.radig.smartmendcraft.network.payload;

import com.radig.smartmendcraft.SmartMendCraft;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record MendingUpdatePayload(
        ItemStack stack,
        int repairAmount,
        int currentDamage,
        int maxDamage
) implements CustomPacketPayload {

    public static final Type<MendingUpdatePayload> TYPE =
            new Type<>(SmartMendCraft.id("mending_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MendingUpdatePayload> CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    MendingUpdatePayload::stack,
                    StreamCodec.of(
                            (buffer, value) -> buffer.writeInt(value),
                            RegistryFriendlyByteBuf::readInt
                    ),
                    MendingUpdatePayload::repairAmount,
                    StreamCodec.of(
                            (buffer, value) -> buffer.writeInt(value),
                            RegistryFriendlyByteBuf::readInt
                    ),
                    MendingUpdatePayload::currentDamage,
                    StreamCodec.of(
                            (buffer, value) -> buffer.writeInt(value),
                            RegistryFriendlyByteBuf::readInt
                    ),
                    MendingUpdatePayload::maxDamage,
                    MendingUpdatePayload::new
            );

    public MendingUpdatePayload(
            ItemStack stack,
            int repairAmount) {

        this(
                stack.copy(),
                repairAmount,
                stack.getDamageValue(),
                stack.getMaxDamage()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}