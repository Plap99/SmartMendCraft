package com.radig.smartmendcraft.mixin;

import com.radig.smartmendcraft.repair.SmartMendingManager;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

    @Inject(
        method = "repairPlayerGears",
        at = @At("HEAD"),
        cancellable = true
    )
    private void smartMendCraft$repairPlayerGears(
            PlayerEntity player,
            int experienceAmount,
            CallbackInfoReturnable<Integer> cir) {

        int remainingExperience =
                smartMendCraft$repairItems(player, experienceAmount);

        cir.setReturnValue(remainingExperience);
    }

    /**
     * Repara los objetos siguiendo la prioridad definida
     * por SmartMendingManager.
     */
    private int smartMendCraft$repairItems(
            PlayerEntity player,
            int experienceAmount) {

        while (experienceAmount > 0) {

            ItemStack stack =
                    SmartMendingManager.findItemToRepair(player);

            if (stack.isEmpty()) {
                break;
            }

            int damage = stack.getDamage();

            /*
             * Mending:
             * 1 XP puede reparar hasta 2 puntos de durabilidad.
             */
            int repairAmount = Math.min(
                    experienceAmount * 2,
                    damage
            );

            stack.setDamage(damage - repairAmount);

            /*
             * Calculamos cuánta XP consumió la reparación.
             *
             * Redondeamos hacia arriba:
             * 1-2 durabilidad = 1 XP
             * 3-4 durabilidad = 2 XP
             * etc.
             */
            int experienceUsed = (repairAmount + 1) / 2;

            experienceAmount -= experienceUsed;
        }

        return experienceAmount;
    }
}