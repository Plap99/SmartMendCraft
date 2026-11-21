package com.radig.smartmendcraft.client;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

record HotbarChoice(String rule, Component name, ItemStack icon) {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    static HotbarChoice forItem(Item item) {
        ItemStack icon = item.getDefaultInstance();
        return new HotbarChoice(BuiltInRegistries.ITEM.getKey(item).toString(), icon.getHoverName(), icon);
    }

    static HotbarChoice forTag(HolderSet.Named<Item> tag) {
        Identifier id = tag.key().location();
        ItemStack icon = tag.stream().map(Holder::value).map(Item::getDefaultInstance)
                .filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
        return new HotbarChoice("#" + id, tagName(id), icon);
    }

    static HotbarChoice fromRule(String rule) {
        if (rule.isEmpty()) {
            return new HotbarChoice(rule, HotbarConfigScreen.text("empty"), ItemStack.EMPTY);
        }
        boolean tagRule = rule.startsWith("#");
        Identifier id = Identifier.tryParse(tagRule ? rule.substring(1) : rule);
        if (id != null && tagRule) {
            return BuiltInRegistries.ITEM.get(TagKey.create(Registries.ITEM, id))
                    .map(HotbarChoice::forTag)
                    .orElseGet(() -> new HotbarChoice(rule, tagName(id), ItemStack.EMPTY));
        }
        return id == null
                ? new HotbarChoice(rule, Component.literal(rule), ItemStack.EMPTY)
                : BuiltInRegistries.ITEM.getOptional(id).map(HotbarChoice::forItem)
                        .orElseGet(() -> new HotbarChoice(rule, Component.literal(rule), ItemStack.EMPTY));
    }

    Component shortName(Font font, int maxWidth) {
        String label = name.getString();
        if (font.width(label) <= maxWidth) {
            return name;
        }
        return Component.literal(font.plainSubstrByWidth(label, Math.max(0, maxWidth - font.width("…"))) + "…");
    }

    static String searchText(String value) {
        return DIACRITICS.matcher(Normalizer.normalize(value, Normalizer.Form.NFD))
                .replaceAll("").toLowerCase(Locale.ROOT);
    }

    private static Component tagName(Identifier id) {
        String label = id.getPath().replace('_', ' ').replace('/', ' ');
        return Component.translatableWithFallback(
                "tag.item." + id.getNamespace() + "." + id.getPath().replace('/', '.'),
                label.isBlank() ? id.toString() : Character.toUpperCase(label.charAt(0)) + label.substring(1));
    }
}
