package com.radig.smartmendcraft;

import com.radig.smartmendcraft.client.SmartMendCraftClient;

public final class SmartMendCraftClientBootstrap {
    private SmartMendCraftClientBootstrap() {
    }

    public static void initialize() {
        SmartMendCraftClient.initialize();
    }
}
