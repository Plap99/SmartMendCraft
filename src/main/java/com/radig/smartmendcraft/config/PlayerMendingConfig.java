package com.radig.smartmendcraft.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMendingConfig {

    private boolean repairMainHand = true;
    private boolean repairOffHand = true;
    private boolean repairArmor = true;
    private boolean repairInventory = true;

    private List<MendingTarget> priority =
            new ArrayList<>(Arrays.asList(
                    MendingTarget.MAIN_HAND,
                    MendingTarget.OFF_HAND,
                    MendingTarget.ARMOR,
                    MendingTarget.INVENTORY
            ));

    private InventoryMendingMode inventoryMode =
            InventoryMendingMode.BALANCE;

    public boolean isRepairMainHand() {
        return repairMainHand;
    }

    public boolean isRepairOffHand() {
        return repairOffHand;
    }

    public boolean isRepairArmor() {
        return repairArmor;
    }

    public boolean isRepairInventory() {
        return repairInventory;
    }

    public List<MendingTarget> getPriority() {
        return new ArrayList<>(priority);
    }

    public InventoryMendingMode getInventoryMode() {
        return inventoryMode;
    }

    public void setRepairMainHand(boolean value) {
        repairMainHand = value;
    }

    public void setRepairOffHand(boolean value) {
        repairOffHand = value;
    }

    public void setRepairArmor(boolean value) {
        repairArmor = value;
    }

    public void setRepairInventory(boolean value) {
        repairInventory = value;
    }

    public void setInventoryMode(InventoryMendingMode mode) {

        if (mode == null) {
            return;
        }

        inventoryMode = mode;
    }

    public void movePriorityUp(MendingTarget target) {

        int index = priority.indexOf(target);

        if (index <= 0) {
            return;
        }

        MendingTarget previous =
                priority.get(index - 1);

        priority.set(index - 1, target);
        priority.set(index, previous);
    }

    public void movePriorityDown(MendingTarget target) {

        int index = priority.indexOf(target);

        if (index < 0 || index >= priority.size() - 1) {
            return;
        }

        MendingTarget next =
                priority.get(index + 1);

        priority.set(index + 1, target);
        priority.set(index, next);
    }

    public void setPriority(List<MendingTarget> newPriority) {
        if (newPriority == null
                || newPriority.size() != MendingTarget.values().length) {

            return;
        }

        for (MendingTarget target : MendingTarget.values()) {

            if (!newPriority.contains(target)) {
                return;
            }
        }

        priority = new ArrayList<>(newPriority);
    }
}