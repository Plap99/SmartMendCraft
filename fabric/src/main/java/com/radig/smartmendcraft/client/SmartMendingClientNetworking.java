package com.radig.smartmendcraft.client;

import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.hotbar.HotbarPreferences;
import com.radig.smartmendcraft.network.payload.HotbarPreferencesPayload;
import com.radig.smartmendcraft.network.payload.ClientHelloPayload;
import com.radig.smartmendcraft.network.payload.ConfigUpdatePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SmartMendingClientNetworking {

    private static HotbarPreferences hotbarPreferences;

    static void registerHotbarReceiver() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> hotbarPreferences = null);
        ClientPlayNetworking.registerGlobalReceiver(HotbarPreferencesPayload.TYPE, (payload, context) -> {
            if (payload.rules().stream().allMatch(HotbarPreferences::isValidRule)) {
                hotbarPreferences = new HotbarPreferences(payload.enabled(), payload.rules());
            }
        });
    }

    static HotbarPreferences getHotbarPreferences() {
        return hotbarPreferences;
    }

    static boolean sendHotbarPreferences(HotbarPreferences preferences) {
        if (!ClientPlayNetworking.canSend(HotbarPreferencesPayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new HotbarPreferencesPayload(preferences));
        hotbarPreferences = preferences;
        return true;
    }

    private SmartMendingClientNetworking() {
    }

    public static void sendClientHello() {
        if (!ClientPlayNetworking.canSend(ClientHelloPayload.TYPE)) {
            return;
        }

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