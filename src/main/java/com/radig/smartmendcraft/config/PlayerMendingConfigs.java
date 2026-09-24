package com.radig.smartmendcraft.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public final class PlayerMendingConfigs {

    private static final Map<UUID, PlayerMendingConfig> CONFIGS =
            new HashMap<>();

    private PlayerMendingConfigs() {
    }

    /**
     * Obtiene la configuración individual del jugador.
     *
     * Si todavía no existe una configuración para él,
     * se crea automáticamente con los valores predeterminados.
     */
    public static PlayerMendingConfig get(PlayerEntity player) {

        if (player == null) {
            return new PlayerMendingConfig();
        }

        UUID playerId = player.getUuid();

        return CONFIGS.computeIfAbsent(
                playerId,
                id -> new PlayerMendingConfig()
        );
    }

    /**
     * Obtiene la configuración directamente mediante UUID.
     */
    public static PlayerMendingConfig get(UUID playerId) {

        if (playerId == null) {
            return new PlayerMendingConfig();
        }

        return CONFIGS.computeIfAbsent(
                playerId,
                id -> new PlayerMendingConfig()
        );
    }

    /**
     * Reemplaza la configuración de un jugador.
     *
     * Nos servirá cuando el cliente envíe al servidor
     * los cambios realizados desde la pantalla F7.
     */
    public static void set(
            UUID playerId,
            PlayerMendingConfig config) {

        if (playerId == null || config == null) {
            return;
        }

        CONFIGS.put(playerId, config);
    }

    /**
     * Elimina de memoria la configuración de un jugador.
     *
     * Más adelante podremos usarlo cuando abandone el servidor,
     * si decidimos descargar configuraciones que ya no se usan.
     */
    public static void remove(UUID playerId) {

        if (playerId == null) {
            return;
        }

        CONFIGS.remove(playerId);
    }

    public static void clear() {
        CONFIGS.clear();
    }
}