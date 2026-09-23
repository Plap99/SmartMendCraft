package com.radig.smartmendcraft.repair;

import java.util.Map;

import com.radig.smartmendcraft.config.MendingTarget;
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
     * Busca el siguiente objeto que SmartMendCraft debería reparar
     * siguiendo el orden configurado por el jugador.
     */
    public static ItemStack findItemToRepair(PlayerEntity player) {

        for (MendingTarget target : SmartMendingConfig.getPriority()) {

            ItemStack stack = findItemForTarget(player, target);

            if (!stack.isEmpty()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Busca un objeto reparable dentro de una categoría concreta.
     */
    private static ItemStack findItemForTarget(
            PlayerEntity player,
            MendingTarget target) {

        switch (target) {

            case MAIN_HAND:

                if (SmartMendingConfig.repairMainHand()) {

                    ItemStack mainHand = player.getMainHandStack();

                    if (canBeRepaired(mainHand)) {
                        return mainHand;
                    }
                }

                break;

            case OFF_HAND:

                if (SmartMendingConfig.repairOffHand()) {

                    ItemStack offHand = player.getOffHandStack();

                    if (canBeRepaired(offHand)) {
                        return offHand;
                    }
                }

                break;

            case ARMOR:

                if (SmartMendingConfig.repairArmor()) {
                    return findArmorToRepair(player);
                }

                break;

            case INVENTORY:

                if (SmartMendingConfig.repairInventory()) {
                    return findInventoryItemToRepair(player);
                }

                break;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Busca una pieza de armadura dañada con Reparación.
     *
     * Orden interno:
     * Cabeza -> Pecho -> Piernas -> Pies
     */
    private static ItemStack findArmorToRepair(PlayerEntity player) {

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
     * Busca en inventario/hotbar el objeto con
     * mayor porcentaje de daño.
     */
    private static ItemStack findInventoryItemToRepair(
            PlayerEntity player) {

        ItemStack mostDamagedItem = ItemStack.EMPTY;
        double highestDamagePercentage = -1.0;

        for (int slot = 0;
             slot < player.getInventory().size();
             slot++) {

            ItemStack stack =
                    player.getInventory().getStack(slot);

            if (!canBeRepaired(stack)) {
                continue;
            }

            double damagePercentage =
                    (double) stack.getDamage()
                            / stack.getMaxDamage();

            if (damagePercentage > highestDamagePercentage) {

                highestDamagePercentage = damagePercentage;
                mostDamagedItem = stack;
            }
        }

        return mostDamagedItem;
    }

    /**
     * Comprueba si un objeto:
     * - existe
     * - está dañado
     * - tiene Reparación
     */
    private static boolean canBeRepaired(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!stack.isDamaged()) {
            return false;
        }

        Map<?, Integer> enchantments =
                EnchantmentHelper.get(stack);

        Integer mendingLevel =
                enchantments.get(Enchantments.MENDING);

        return mendingLevel != null && mendingLevel > 0;
    }
}