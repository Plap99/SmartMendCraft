package com.radig.smartmendcraft.hotbar;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public record HotbarPreferences(boolean enabled, List<String> rules) {

    public static final int SLOT_COUNT = 9;
    public static final int MAX_RULE_LENGTH = 128;
    public static final HotbarPreferences DEFAULT =
            new HotbarPreferences(false, Collections.nCopies(SLOT_COUNT, ""));

    public HotbarPreferences {
        if (rules == null || rules.size() != SLOT_COUNT
                || !rules.stream().allMatch(HotbarPreferences::isValidRule)) {
            throw new IllegalArgumentException("Invalid hotbar rules");
        }
        rules = List.copyOf(rules);
    }

    public static boolean isValidRule(String rule) {
        return rule != null && rule.length() <= MAX_RULE_LENGTH
                && (rule.isEmpty() || identifier(rule) != null);
    }

    public int preferredSlot(ItemStack stack) {
        if (!enabled || stack.isEmpty()) {
            return -1;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        int tagSlot = -1;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            String rule = rules.get(slot);
            if (rule.isEmpty()) {
                continue;
            }
            Identifier id = identifier(rule);
            if (rule.startsWith("#")) {
                if (tagSlot < 0 && stack.typeHolder().is(TagKey.create(Registries.ITEM, id))) {
                    tagSlot = slot;
                }
            } else if (itemId.equals(id)) {
                return slot;
            }
        }
        return tagSlot;
    }

    private static Identifier identifier(String rule) {
        String id = rule.startsWith("#") ? rule.substring(1) : rule;
        return id.isEmpty() || id.endsWith(":") ? null : Identifier.tryParse(id);
    }
}
