package com.radig.smartmendcraft.mixin;

import com.radig.smartmendcraft.network.SmartMendingClients;
import com.radig.smartmendcraft.repair.MendingRepairEvents;
import com.radig.smartmendcraft.repair.SmartMendingManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {

    @Inject(
        method = "repairPlayerItems",
        at = @At("HEAD"),
        cancellable = true
    )
    private void smartMendCraft$repairPlayerItems(
            ServerPlayer player,
            int experienceAmount,
            CallbackInfoReturnable<Integer> cir) {

        /*
         * If the player does not have SmartMendCraft
         * installed on the client, keep vanilla Mending.
         */
        if (!SmartMendingClients.hasSmartMendCraft(player)) {
            return;
        }

        /*
         * The player has SmartMendCraft.
         * Replace vanilla item selection with our
         * configurable priority system.
         */
        int remainingExperience =
                smartMendCraft$repairItems(
                        player,
                        experienceAmount
                );

        cir.setReturnValue(
                remainingExperience
        );
    }

    /**
     * Repairs items using the priority selected by
     * SmartMendingManager while preserving the current
     * Minecraft Mending repair calculation.
     */
    private int smartMendCraft$repairItems(
            ServerPlayer player,
            int experienceAmount) {

        while (experienceAmount > 0) {

            ItemStack stack =
                    SmartMendingManager.findItemToRepair(
                            player
                    );

            if (stack.isEmpty()) {
                break;
            }

            int damage =
                    stack.getDamageValue();

            if (damage <= 0) {
                break;
            }

            /*
             * Ask Minecraft 26.2 how much durability
             * this amount of XP can repair.
             */
            int possibleRepair =
                    EnchantmentHelper.modifyDurabilityToRepairFromXp(
                            player.level(),
                            stack,
                            experienceAmount
                    );

            int repairAmount =
                    Math.min(
                            possibleRepair,
                            damage
                    );

            if (repairAmount <= 0) {
                break;
            }

            stack.setDamageValue(
                    damage - repairAmount
            );

            MendingRepairEvents.notifyItemRepaired(
                    player,
                    stack,
                    repairAmount
            );

            /*
             * Vanilla 26.2 calculates the remaining XP
             * proportionally from the actual repair amount.
             */
            int remainingExperience =
                    experienceAmount
                            - repairAmount
                            * experienceAmount
                            / possibleRepair;

            if (remainingExperience <= 0) {
                return 0;
            }

            experienceAmount =
                    remainingExperience;
        }

        return experienceAmount;
    }
}