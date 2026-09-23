package com.radig.smartmendcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmartMendCraft implements ModInitializer {

    public static final String MOD_ID = "smartmendcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SmartMendCraft iniciado correctamente.");
    }

    public static Identifier id(String path) {
        return Identifier.tryParse(MOD_ID + ":" + path);
    }
}