package com.radig.smartmendcraft.repair;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class MendingRepairEvents {

    private static final List<MendingRepairListener> LISTENERS =
            new ArrayList<>();

    private MendingRepairEvents() {
        // Evita que esta clase pueda instanciarse.
    }

    /**
     * Registra un listener interesado en las reparaciones.
     */
    public static void register(MendingRepairListener listener) {

        if (listener == null) {
            return;
        }

        LISTENERS.add(listener);
    }

    /**
     * Notifica que SmartMendCraft acaba de reparar un objeto.
     */
    public static void notifyItemRepaired(
            PlayerEntity player,
            ItemStack stack,
            int repairAmount) {

        if (player == null
                || stack == null
                || stack.isEmpty()
                || repairAmount <= 0) {

            return;
        }

        for (MendingRepairListener listener : LISTENERS) {

            listener.onItemRepaired(
                    player,
                    stack,
                    repairAmount
            );
        }
    }
}