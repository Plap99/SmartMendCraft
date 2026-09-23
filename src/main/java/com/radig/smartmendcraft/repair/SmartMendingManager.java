package com.radig.smartmendcraft.repair;

import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class SmartMendingManager {

    private SmartMendingManager() {
        // Evita que esta clase pueda instanciarse.
    }

    /**
     * Busca el siguiente objeto que SmartMendCraft debería reparar.
     *
     * Prioridad inicial:
     * 1. Mano principal
     * 2. Mano secundaria
     * 3. Armadura
     *
     * Por ahora mantenemos el mismo alcance que Mending vanilla 1.17.1:
     * solamente equipo equipado.
     */
    public static ItemStack findItemToRepair(PlayerEntity player) {

        // 1. MANO PRINCIPAL
        ItemStack mainHand = player.getMainHandStack();

        if (canBeRepaired(mainHand)) {
            return mainHand;
        }

        // 2. MANO SECUNDARIA
        ItemStack offHand = player.getOffHandStack();

        if (canBeRepaired(offHand)) {
            return offHand;
        }

        // 3. ARMADURA
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = player.getEquippedStack(slot);

            if (canBeRepaired(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Comprueba que el objeto:
     * - exista,
     * - esté dañado,
     * - y tenga Mending.
     */
    private static boolean canBeRepaired(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!stack.isDamaged()) {
            return false;
        }

        Map<?, Integer> enchantments = EnchantmentHelper.get(stack);

        Integer mendingLevel = enchantments.get(Enchantments.MENDING);

        return mendingLevel != null && mendingLevel > 0;
    }
}