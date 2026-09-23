package com.radig.smartmendcraft.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.radig.smartmendcraft.SmartMendCraft;

import net.fabricmc.loader.api.FabricLoader;

public final class SmartMendingConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("smartmendcraft.json");

    /*
     * Valores predeterminados.
     */
    private boolean repairMainHand = true;
    private boolean repairOffHand = true;
    private boolean repairArmor = true;
    private boolean repairInventory = true;

    private List<MendingTarget> priority = createDefaultPriority();

    private static SmartMendingConfig instance =
            new SmartMendingConfig();

    private SmartMendingConfig() {
    }

    private static List<MendingTarget> createDefaultPriority() {
        return new ArrayList<>(
                Arrays.asList(
                        MendingTarget.MAIN_HAND,
                        MendingTarget.OFF_HAND,
                        MendingTarget.ARMOR,
                        MendingTarget.INVENTORY
                )
        );
    }

    public static void load() {

        /*
         * Si todavía no existe configuración,
         * creamos el archivo con los valores predeterminados.
         */
        if (!Files.exists(CONFIG_PATH)) {

            save();

            SmartMendCraft.LOGGER.info(
                    "Configuración creada en {}",
                    CONFIG_PATH
            );

            return;
        }

        try {

            /*
             * Primero comprobamos qué propiedades existen
             * realmente en el archivo JSON.
             */
            boolean priorityExists;

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {

                JsonObject json =
                    new JsonParser()
                            .parse(reader)
                            .getAsJsonObject();

                priorityExists = json.has("priority");
            }

            /*
             * Después cargamos normalmente la configuración.
             */
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {

                SmartMendingConfig loaded =
                        GSON.fromJson(reader, SmartMendingConfig.class);

                if (loaded != null) {

                    instance = loaded;

                    /*
                     * Si el JSON antiguo no tenía priority,
                     * agregamos el valor predeterminado.
                     */
                    if (!priorityExists
                            || instance.priority == null
                            || instance.priority.isEmpty()) {

                        instance.priority = createDefaultPriority();

                        save();

                        SmartMendCraft.LOGGER.info(
                                "Configuración actualizada con prioridades predeterminadas."
                        );
                    }
                }
            }

            SmartMendCraft.LOGGER.info(
                    "Configuración cargada desde {}",
                    CONFIG_PATH
            );

        } catch (Exception exception) {

            SmartMendCraft.LOGGER.error(
                    "No se pudo cargar la configuración de SmartMendCraft.",
                    exception
            );
        }
    }

    public static void save() {

        try {

            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer =
                         Files.newBufferedWriter(CONFIG_PATH)) {

                GSON.toJson(instance, writer);
            }

        } catch (IOException exception) {

            SmartMendCraft.LOGGER.error(
                    "No se pudo guardar la configuración de SmartMendCraft.",
                    exception
            );
        }
    }

    public static boolean repairMainHand() {
        return instance.repairMainHand;
    }

    public static boolean repairOffHand() {
        return instance.repairOffHand;
    }

    public static boolean repairArmor() {
        return instance.repairArmor;
    }

    public static boolean repairInventory() {
        return instance.repairInventory;
    }

    public static List<MendingTarget> getPriority() {
        return new ArrayList<>(instance.priority);
    }

    public static void setRepairMainHand(boolean value) {
        instance.repairMainHand = value;
        save();
    }

    public static void setRepairOffHand(boolean value) {
        instance.repairOffHand = value;
        save();
    }

    public static void setRepairArmor(boolean value) {
        instance.repairArmor = value;
        save();
    }

    public static void setRepairInventory(boolean value) {
        instance.repairInventory = value;
        save();
    }

    public static void movePriorityUp(MendingTarget target) {
        int index = instance.priority.indexOf(target);

        // Ya está arriba o no existe.
        if (index <= 0) {
            return;
        }

        MendingTarget previous =
                instance.priority.get(index - 1);

        instance.priority.set(index - 1, target);
        instance.priority.set(index, previous);

        save();
    }

    public static void movePriorityDown(MendingTarget target) {
        int index = instance.priority.indexOf(target);

        // No existe o ya está abajo.
        if (index < 0 || index >= instance.priority.size() - 1) {
            return;
        }

        MendingTarget next =
                instance.priority.get(index + 1);

        instance.priority.set(index + 1, target);
        instance.priority.set(index, next);

        save();
    }
}