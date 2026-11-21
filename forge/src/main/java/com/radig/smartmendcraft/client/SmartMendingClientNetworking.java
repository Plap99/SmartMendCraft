package com.radig.smartmendcraft.client;

import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.hotbar.HotbarPreferences;
import com.radig.smartmendcraft.network.SmartMendingNetworking;
import com.radig.smartmendcraft.network.payload.ClientHelloPayload;
import com.radig.smartmendcraft.network.payload.ConfigUpdatePayload;
import com.radig.smartmendcraft.network.payload.HotbarPreferencesPayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

public final class SmartMendingClientNetworking {

    private static HotbarPreferences hotbarPreferences;

    private SmartMendingClientNetworking() {
    }

    static void registerClientEvents() {
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> sendClientHello());
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> hotbarPreferences = null);
    }

    public static void handleHotbarPreferences(HotbarPreferencesPayload payload) {
        if (payload.rules().stream().allMatch(HotbarPreferences::isValidRule)) {
            hotbarPreferences = new HotbarPreferences(payload.enabled(), payload.rules());
        }
    }

    static HotbarPreferences getHotbarPreferences() {
        return hotbarPreferences;
    }

    static boolean sendHotbarPreferences(HotbarPreferences preferences) {
        if (!sendToServer(new HotbarPreferencesPayload(preferences))) {
            return false;
        }
        hotbarPreferences = preferences;
        return true;
    }

    public static void sendClientHello() {
        sendToServer(new ClientHelloPayload());
    }

    public static void sendConfigUpdate(PlayerMendingConfig config) {
        if (config != null) {
            sendToServer(ConfigUpdatePayload.fromConfig(config));
        }
    }

    private static boolean sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || !SmartMendingNetworking.canSendToServer(connection.getConnection())) {
            return false;
        }
        SmartMendingNetworking.sendToServer(payload);
        return true;
    }
}
