package com.radig.smartmendcraft.repair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface MendingRepairListener {

    void onItemRepaired(
            PlayerEntity player,
            ItemStack stack,
            int repairAmount
    );
}