package com.radig.smartmendcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.radig.smartmendcraft.SmartMendCraft;
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
        if (!(inventory.player instanceof ServerPlayer player)) {
            return original.call(inventory, stack);
        }

        boolean registered = SmartMendingClients.hasSmartMendCraft(player);
        var preferences = PlayerMendingConfigs.get(player).getHotbar();
        var itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        int slot = preferences.preferredSlot(stack);
        SmartMendCraft.LOGGER.debug("Hotbar pickup: player={}, item={}, registered={}, enabled={}, target={}",
                player.getUUID(), itemId, registered, preferences.enabled(), slot + 1);

        if (!registered) {
            return original.call(inventory, stack);
        }

        if (slot < 0) {
            return original.call(inventory, stack);
        }

        ItemStack existing = inventory.getItem(slot);
        if (!existing.isEmpty()
                && (stack.isDamaged() || !ItemStack.isSameItemSameComponents(existing, stack))) {
            // Keep the first item when this slot already contains another item
            // assigned to the same category. Otherwise move the unrelated item
            // out of the way so the configured category can claim its slot.
            if (preferences.preferredSlot(existing) == slot) {
                SmartMendCraft.LOGGER.debug("Hotbar pickup skipped: target={} already contains another item for that category",
                        slot + 1);
                return original.call(inventory, stack);
            }

            int displacedSlot = preferences.preferredSlot(existing);
            if (displacedSlot < 0 || displacedSlot == slot || !inventory.getItem(displacedSlot).isEmpty()) {
                displacedSlot = smc$findFreeUnassignedSlot(inventory, preferences);
            }
            if (displacedSlot < 0) {
                SmartMendCraft.LOGGER.debug("Hotbar pickup skipped: no free slot to move item from target={}", slot + 1);
                return original.call(inventory, stack);
            }

            SmartMendCraft.LOGGER.debug("Hotbar pickup moved existing item from slot {} to slot {}",
                    slot + 1, displacedSlot + 1);
            inventory.setItem(displacedSlot, existing);
            inventory.setItem(slot, ItemStack.EMPTY);
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
        SmartMendCraft.LOGGER.debug("Hotbar pickup result: item={}, target={}, moved={}, remainder={}",
                itemId, slot + 1, moved, stack.getCount());
        return moved > 0 || pickedUpRemainder;
    }

    private static int smc$findFreeUnassignedSlot(Inventory inventory,
            com.radig.smartmendcraft.hotbar.HotbarPreferences preferences) {
        for (int slot = 0; slot < 9; slot++) {
            if (preferences.rules().get(slot).isEmpty() && inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        for (int slot = 9; slot < 36; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }
}
