package com.radig.smartmendcraft.client;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class SmartMendCraftClient implements ClientModInitializer {

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {

        openConfigKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.smartmendcraft.open_config",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_F7,
                        "category.smartmendcraft"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openConfigKey.wasPressed()) {

                if (client.currentScreen == null) {
                    client.setScreen(
                            new SmartMendingConfigScreen(null)
                    );
                }
            }
        });
    }
}