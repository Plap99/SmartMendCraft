package com.radig.smartmendcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;

import net.fabricmc.loader.api.FabricLoader;

public final class PlayerMendingConfigStorage {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path CONFIG_DIRECTORY =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("smartmendcraft")
                    .resolve("players");

    private PlayerMendingConfigStorage() {
    }

    /**
     * Obtiene el archivo correspondiente a un jugador.
     */
    private static Path getPlayerFile(UUID playerId) {

        return CONFIG_DIRECTORY.resolve(
                playerId.toString() + ".json"
        );
    }

    /**
     * Guarda la configuración individual de un jugador.
     */
    public static void save(
            UUID playerId,
            PlayerMendingConfig config) {

        if (playerId == null || config == null) {
            return;
        }

        try {

            Files.createDirectories(
                    CONFIG_DIRECTORY
            );

            Path file =
                    getPlayerFile(playerId);

            try (Writer writer =
                         Files.newBufferedWriter(file)) {

                GSON.toJson(
                        config,
                        writer
                );
            }

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }

    /**
     * Carga la configuración individual de un jugador.
     *
     * Si todavía no existe un archivo para ese UUID,
     * devuelve una configuración nueva con los valores
     * predeterminados.
     */
    public static PlayerMendingConfig load(
            UUID playerId) {

        if (playerId == null) {
            return new PlayerMendingConfig();
        }

        Path file =
                getPlayerFile(playerId);

        if (!Files.exists(file)) {
            return new PlayerMendingConfig();
        }

        try (Reader reader =
                     Files.newBufferedReader(file)) {

            PlayerMendingConfig config =
                    GSON.fromJson(
                            reader,
                            PlayerMendingConfig.class
                    );

            if (config == null) {
                return new PlayerMendingConfig();
            }

            return config;

        } catch (Exception exception) {

            exception.printStackTrace();

            return new PlayerMendingConfig();
        }
    }
}