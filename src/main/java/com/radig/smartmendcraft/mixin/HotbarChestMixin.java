package com.radig.smartmendcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.hotbar.HotbarPreferences;
import com.radig.smartmendcraft.network.SmartMendingClients;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestMenu.class)
abstract class HotbarChestMixin {

    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/inventory/ChestMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z"))
    private boolean smc$preferHotbarSlot(ChestMenu menu, ItemStack stack, int start, int end,
            boolean reverse, Operation<Boolean> original) {
        // Chest menus put the nine hotbar slots at the end of the player inventory range.
        int hotbarStart = end - HotbarPreferences.SLOT_COUNT;
        if (hotbarStart < start
                || !(menu.getSlot(hotbarStart).container instanceof Inventory inventory)
                || !(inventory.player instanceof ServerPlayer player)
                || !SmartMendingClients.hasSmartMendCraft(player)) {
            return original.call(menu, stack, start, end, reverse);
        }

        int preferred = PlayerMendingConfigs.get(player).getHotbar().preferredSlot(stack);
        if (preferred < 0) {
            return original.call(menu, stack, start, end, reverse);
        }

        int target = hotbarStart + preferred;
        Slot slot = menu.getSlot(target);
        if (slot.container != inventory || slot.getContainerSlot() != preferred || !slot.mayPlace(stack)) {
            return original.call(menu, stack, start, end, reverse);
        }

        boolean moved = original.call(menu, stack, target, target + 1, reverse);
        boolean movedRemainder = !stack.isEmpty() && original.call(menu, stack, start, end, reverse);
        return moved || movedRemainder;
    }
}
