package com.radig.smartmendcraft.repair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;

public final class SmartMendingManager {

    /*
     * Objeto que FINISH_ITEM está reparando actualmente.
     *
     * No se usa en modo BALANCE.
     */
    private static final Map<UUID, ItemStack> CURRENT_INVENTORY_TARGETS =
        new HashMap<>();

    private SmartMendingManager() {
        // Evita que esta clase pueda instanciarse.
    }

    /**
     * Busca el siguiente objeto que SmartMendCraft debería reparar
     * siguiendo el orden configurado por el jugador.
     */
    public static ItemStack findItemToRepair(Player player) {
        PlayerMendingConfig config =
                PlayerMendingConfigs.get(player);

        for (MendingTarget target : config.getPriority()) {

            ItemStack stack =
                    findItemForTarget(
                            player,
                            target,
                            config
                    );

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
            Player player,
            MendingTarget target,
            PlayerMendingConfig config) {

        switch (target) {

            case MAIN_HAND:

                if (config.isRepairMainHand()) {

                    ItemStack stack =
                            player.getMainHandItem();

                    if (canBeRepaired(stack)) {
                        return stack;
                    }
                }

                break;

            case OFF_HAND:

                if (config.isRepairOffHand()) {

                    ItemStack stack =
                            player.getOffhandItem();

                    if (canBeRepaired(stack)) {
                        return stack;
                    }
                }

                break;

            case ARMOR:

                if (config.isRepairArmor()) {
                    return findArmorToRepair(player);
                }

                break;

            case INVENTORY:

                if (config.isRepairInventory()) {
                    return findInventoryItemToRepair(
                            player,
                            config
                    );
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
    private static ItemStack findArmorToRepair(Player player) {

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {

            ItemStack stack = player.getItemBySlot(slot);

            if (canBeRepaired(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Decide cómo buscar un objeto dentro del inventario
     * según el modo configurado.
     */
    private static ItemStack findInventoryItemToRepair(
            Player player,
            PlayerMendingConfig config) {

        UUID playerId = player.getUUID();

        if (config.getInventoryMode()
                == InventoryMendingMode.FINISH_ITEM) {

            return findInventoryItemFinishMode(player);
        }

        CURRENT_INVENTORY_TARGETS.remove(playerId);

        return findMostDamagedInventoryItem(player);
    }

    /**
     * Modo BALANCE:
     * devuelve siempre el objeto con mayor porcentaje de daño.
     */
    private static ItemStack findMostDamagedInventoryItem(
            Player player) {

        ItemStack mostDamagedItem = ItemStack.EMPTY;
        double highestDamagePercentage = -1.0;

        for (int slot = 0; slot < 36; slot++) {

            ItemStack stack =
                    player.getInventory().getItem(slot);

            if (!canBeRepaired(stack)) {
                continue;
            }

            double damagePercentage =
                    (double) stack.getDamageValue()
                            / stack.getMaxDamage();

            if (damagePercentage > highestDamagePercentage) {

                highestDamagePercentage = damagePercentage;
                mostDamagedItem = stack;
            }
        }

        return mostDamagedItem;
    }

    /**
     * Modo FINISH_ITEM:
     *
     * Mantiene el mismo objeto como objetivo hasta que:
     * - quede completamente reparado,
     * - salga del inventario,
     * - o deje de ser válido.
     *
     * Después selecciona otro objeto.
     */
    private static ItemStack findInventoryItemFinishMode(
            Player player) {

        UUID playerId = player.getUUID();

        ItemStack currentTarget =
                CURRENT_INVENTORY_TARGETS.get(playerId);

        if (isCurrentTargetValid(player, currentTarget)) {
            return currentTarget;
        }

        ItemStack newTarget =
                findMostDamagedInventoryItem(player);

        if (newTarget.isEmpty()) {
            CURRENT_INVENTORY_TARGETS.remove(playerId);
        } else {
            CURRENT_INVENTORY_TARGETS.put(
                    playerId,
                    newTarget
            );
        }

        return newTarget;
    }

    /**
     * Comprueba que el objetivo actual siga siendo válido
     * y continúe dentro del inventario del jugador.
     */
    private static boolean isCurrentTargetValid(
            Player player,
            ItemStack currentTarget) {

        if (currentTarget == null
                || currentTarget.isEmpty()
                || !canBeRepaired(currentTarget)) {

            return false;
        }

        /*
        * Solo buscamos dentro de los 36 slots reales
        * del inventario/hotbar.
        */
        for (int slot = 0; slot < 36; slot++) {

            ItemStack stack =
                    player.getInventory().getItem(slot);

            if (stack == currentTarget) {
                return true;
            }
        }

        return false;
    }

    /**
     * Limpia cualquier objetivo temporal de reparación
     * asociado a un jugador.
     *
     * Se utiliza cuando el jugador abandona el servidor
     * para evitar conservar referencias innecesarias
     * a ItemStack en memoria.
     */
    public static void clearPlayer(Player player) {

        if (player == null) {
            return;
        }

        CURRENT_INVENTORY_TARGETS.remove(
                player.getUUID()
        );
    }

     /**
     * Comprueba si un objeto:
     * - existe
     * - esta danado
     * - tiene Reparacion
     */
    private static boolean canBeRepaired(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (!stack.isDamaged()) {
            return false;
        }

        var enchantments =
                EnchantmentHelper.getEnchantmentsForCrafting(stack);

        return enchantments.entrySet()
                .stream()
                .anyMatch(entry ->
                        entry.getIntValue() > 0
                        && entry.getKey()
                                .unwrapKey()
                                .map(Enchantments.MENDING::equals)
                                .orElse(false)
                );
    }
}