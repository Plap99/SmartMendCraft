package com.radig.smartmendcraft.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public final class SmartMendingClientConfig {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path CONFIG_FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("smartmendcraft-client.json");

    private static SmartMendingClientConfig instance =
            new SmartMendingClientConfig();

    /*
     * Opciones visuales del HUD.
     */
    private boolean showHud = true;

    private HudPosition hudPosition =
            HudPosition.TOP_LEFT;

    private SmartMendingClientConfig() {
    }

    public static void load() {

        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }

        try (Reader reader =
                     Files.newBufferedReader(CONFIG_FILE)) {

            SmartMendingClientConfig loaded =
                    GSON.fromJson(
                            reader,
                            SmartMendingClientConfig.class
                    );

            if (loaded != null) {
                instance = loaded;
            }

            /*
             * Protección por si una configuración antigua
             * no contiene todavía la posición del HUD.
             */
            if (instance.hudPosition == null) {
                instance.hudPosition =
                        HudPosition.TOP_LEFT;

                save();
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            instance =
                    new SmartMendingClientConfig();

            save();
        }
    }

    public static void save() {

        try {

            Path parent =
                    CONFIG_FILE.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer =
                         Files.newBufferedWriter(CONFIG_FILE)) {

                GSON.toJson(
                        instance,
                        writer
                );
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static boolean isShowHud() {
        return instance.showHud;
    }

    public static HudPosition getHudPosition() {
        return instance.hudPosition;
    }

    public static void setShowHud(boolean value) {

        instance.showHud = value;

        save();
    }

    public static void setHudPosition(
            HudPosition position) {

        if (position == null) {
            return;
        }

        instance.hudPosition = position;

        save();
    }

    public static void toggleHud() {

        instance.showHud =
                !instance.showHud;

        save();
    }

    public static void nextHudPosition() {

        instance.hudPosition =
                instance.hudPosition.next();

        save();
    }
}