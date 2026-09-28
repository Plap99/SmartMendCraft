package com.radig.smartmendcraft.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class SmartMendingClients {

    private static final Set<UUID> CONNECTED_CLIENTS =
            new HashSet<>();

    private SmartMendingClients() {
    }

    public static void register(
            Player player) {

        if (player == null) {
            return;
        }

        CONNECTED_CLIENTS.add(
                player.getUUID()
        );
    }

    public static void unregister(
            Player player) {

        if (player == null) {
            return;
        }

        CONNECTED_CLIENTS.remove(
                player.getUUID()
        );
    }

    public static boolean hasSmartMendCraft(
            Player player) {

        if (player == null) {
            return false;
        }

        return CONNECTED_CLIENTS.contains(
                player.getUUID()
        );
    }
}