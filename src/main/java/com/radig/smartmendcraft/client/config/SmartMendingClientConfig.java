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

    private static final float DEFAULT_HUD_SCALE =
            0.50F;

    private static final float MIN_HUD_SCALE =
            0.25F;

    private static final float MAX_HUD_SCALE =
            1.00F;

    private static SmartMendingClientConfig instance =
            new SmartMendingClientConfig();

    /*
     * Opciones visuales del HUD.
     */
    private boolean showHud = true;

    private HudPosition hudPosition =
            HudPosition.TOP_LEFT;

    private float hudScale =
            DEFAULT_HUD_SCALE;

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
             * Proteccion por si una configuracion antigua
             * no contiene todavia la posicion del HUD.
             */
            if (instance.hudPosition == null) {

                instance.hudPosition =
                        HudPosition.TOP_LEFT;
            }

            /*
             * Una configuracion antigua no contiene
             * hudScale, por lo que puede llegar como 0.
             *
             * En ese caso usamos el tamano actual
             * del HUD como valor predeterminado: 50%.
             */
            if (instance.hudScale <= 0.0F) {

                instance.hudScale =
                        DEFAULT_HUD_SCALE;
            } else {

                float normalizedScale =
                        clampHudScale(
                                instance.hudScale
                        );

                if (normalizedScale
                        != instance.hudScale) {

                    instance.hudScale =
                            normalizedScale;
                }
            }

            save();

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

    public static float getHudScale() {
        return instance.hudScale;
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

    public static void setHudScale(float scale) {

        instance.hudScale =
                clampHudScale(scale);

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

    private static float clampHudScale(
            float scale) {

        return Math.max(
                MIN_HUD_SCALE,
                Math.min(
                        MAX_HUD_SCALE,
                        scale
                )
        );
    }
}