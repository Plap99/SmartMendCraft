package com.radig.smartmendcraft.repair;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MendingRepairListener {

    void onItemRepaired(
            Player player,
            ItemStack stack,
            int repairAmount
    );
}