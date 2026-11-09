package com.radig.smartmendcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.network.SmartMendingClients;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
abstract class HotbarPickupMixin {

    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean smc$preferHotbarSlot(Inventory inventory, ItemStack stack, Operation<Boolean> original) {
        if (!(inventory.player instanceof ServerPlayer player)
                || !SmartMendingClients.hasSmartMendCraft(player)) {
            return original.call(inventory, stack);
        }

        int slot = PlayerMendingConfigs.get(player).getHotbar().preferredSlot(stack);
        if (slot < 0) {
            return original.call(inventory, stack);
        }

        ItemStack existing = inventory.getItem(slot);
        if (!existing.isEmpty()
                && (stack.isDamaged() || !ItemStack.isSameItemSameComponents(existing, stack))) {
            return original.call(inventory, stack);
        }

        int space = inventory.getMaxStackSize(stack) - existing.getCount();
        if (space <= 0) {
            return original.call(inventory, stack);
        }

        // Limit the explicit-slot insertion so any overflow follows vanilla pickup rules.
        int count = Math.min(space, stack.getCount());
        ItemStack portion = stack.copyWithCount(count);
        inventory.add(slot, portion);
        int moved = count - portion.getCount();
        stack.shrink(moved);
        boolean pickedUpRemainder = !stack.isEmpty() && original.call(inventory, stack);
        return moved > 0 || pickedUpRemainder;
    }
}
