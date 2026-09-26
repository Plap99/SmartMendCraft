package com.radig.smartmendcraft.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public final class SmartMendingClients {

    private static final Set<UUID> CONNECTED_CLIENTS =
            new HashSet<>();

    private SmartMendingClients() {
    }

    public static void register(
            PlayerEntity player) {

        if (player == null) {
            return;
        }

        CONNECTED_CLIENTS.add(
                player.getUuid()
        );
    }

    public static void unregister(
            PlayerEntity player) {

        if (player == null) {
            return;
        }

        CONNECTED_CLIENTS.remove(
                player.getUuid()
        );
    }

    public static boolean hasSmartMendCraft(
            PlayerEntity player) {

        if (player == null) {
            return false;
        }

        return CONNECTED_CLIENTS.contains(
                player.getUuid()
        );
    }
}