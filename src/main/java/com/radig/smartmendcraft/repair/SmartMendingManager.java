package com.radig.smartmendcraft.repair;

import java.util.Map;

import com.radig.smartmendcraft.config.SmartMendingConfig;

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
     * Prioridad:
     * 1. Mano principal
     * 2. Mano secundaria
     * 3. Armadura
     * 4. Inventario / hotbar
     */
    public static ItemStack findItemToRepair(PlayerEntity player) {

        // 1. MANO PRINCIPAL
        if (SmartMendingConfig.repairMainHand()) {

            ItemStack mainHand = player.getMainHandStack();

            if (canBeRepaired(mainHand)) {
                return mainHand;
            }
        }

        // 2. MANO SECUNDARIA
        if (SmartMendingConfig.repairOffHand()) {

            ItemStack offHand = player.getOffHandStack();

            if (canBeRepaired(offHand)) {
                return offHand;
            }
        }

        // 3. ARMADURA
        if (SmartMendingConfig.repairArmor()) {

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
        }

        // 4. INVENTARIO / HOTBAR
        if (SmartMendingConfig.repairInventory()) {

            ItemStack mostDamagedItem = ItemStack.EMPTY;
            double highestDamagePercentage = -1.0;

            for (int slot = 0; slot < player.getInventory().size(); slot++) {

                ItemStack stack = player.getInventory().getStack(slot);

                if (!canBeRepaired(stack)) {
                    continue;
                }

                double damagePercentage =
                        (double) stack.getDamage() / stack.getMaxDamage();

                if (damagePercentage > highestDamagePercentage) {
                    highestDamagePercentage = damagePercentage;
                    mostDamagedItem = stack;
                }
            }

            return mostDamagedItem;
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