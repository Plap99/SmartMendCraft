package com.radig.smartmendcraft.client;

import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.network.SmartMendingNetworking;

import java.util.List;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.network.PacketByteBuf;

public final class SmartMendingClientNetworking {

    private SmartMendingClientNetworking() {
    }

    /**
     * Informa al servidor que este cliente tiene
     * SmartMendCraft instalado.
     */
    public static void sendClientHello() {

        PacketByteBuf buffer =
                PacketByteBufs.create();

        ClientPlayNetworking.send(
                SmartMendingNetworking.CLIENT_HELLO,
                buffer
        );
    }

    /**
     * Envía al servidor la configuración personal
     * seleccionada por el jugador.
     */
    public static void sendConfigUpdate(
            PlayerMendingConfig config) {

        if (config == null) {
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
                config.getInventoryMode()
                        .ordinal()
        );

        ClientPlayNetworking.send(
                SmartMendingNetworking.CONFIG_UPDATE,
                buffer
        );
    }
}