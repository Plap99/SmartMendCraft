package com.radig.smartmendcraft.client;

import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.network.payload.ClientHelloPayload;
import com.radig.smartmendcraft.network.payload.ConfigUpdatePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SmartMendingClientNetworking {

    private SmartMendingClientNetworking() {
    }

    public static void sendClientHello() {

        ClientPlayNetworking.send(
                new ClientHelloPayload()
        );
    }

    public static void sendConfigUpdate(
            PlayerMendingConfig config) {

        if (config == null) {
            return;
        }

        ClientPlayNetworking.send(
                ConfigUpdatePayload.fromConfig(config)
        );
    }
}