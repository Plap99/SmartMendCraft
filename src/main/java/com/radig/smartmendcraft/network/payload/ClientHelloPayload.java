package com.radig.smartmendcraft.network.payload;

import com.radig.smartmendcraft.SmartMendCraft;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientHelloPayload() implements CustomPacketPayload {

    public static final Type<ClientHelloPayload> TYPE =
            new Type<>(SmartMendCraft.id("client_hello"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientHelloPayload> CODEC =
            StreamCodec.unit(new ClientHelloPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}