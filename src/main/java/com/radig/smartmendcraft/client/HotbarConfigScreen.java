package com.radig.smartmendcraft.client;

import java.util.ArrayList;
import java.util.List;

import com.radig.smartmendcraft.hotbar.HotbarPreferences;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class HotbarConfigScreen extends Screen {

    private final Screen parent;
    private final List<String> rules;
    private final List<HotbarChoice> choices = new ArrayList<>();
    private boolean enabled;

    HotbarConfigScreen(Screen parent, HotbarPreferences preferences) {
        super(text("title"));
        this.parent = parent;
        this.rules = new ArrayList<>(preferences.rules());
        this.enabled = preferences.enabled();
    }

    @Override
    protected void init() {
        int center = width / 2;
        addRenderableWidget(Button.builder(toggleLabel(), button -> {
            enabled = !enabled;
            button.setMessage(toggleLabel());
        }).bounds(center - 70, 53, 140, 20).build());

        choices.clear();
        for (int slot = 0; slot < HotbarPreferences.SLOT_COUNT; slot++) {
            int slotIndex = slot;
            HotbarChoice choice = HotbarChoice.fromRule(rules.get(slot));
            choices.add(choice);
            Button button = addRenderableWidget(Button.builder(choice.shortName(font, cellWidth() - 44),
                    ignored -> minecraft.setScreenAndShow(new HotbarPickerScreen(
                            this, slotIndex, rules.get(slotIndex), rule -> rules.set(slotIndex, rule))))
                    .bounds(cellX(slot), cellY(slot), cellWidth(), 20).build());
            button.setTooltip(Tooltip.create(choice.name()));
        }

        addRenderableWidget(Button.builder(text("save"), button -> {
            HotbarPreferences preferences = new HotbarPreferences(enabled, rules);
            if (SmartMendingClientNetworking.sendHotbarPreferences(preferences)) {
                onClose();
            } else {
                button.active = false;
            }
        }).bounds(center - 104, 193, 100, 20).build());
        addRenderableWidget(Button.builder(text("cancel"), button -> onClose())
                .bounds(center + 4, 193, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFFFF);
        graphics.centeredText(font, text("help"), width / 2, 35, 0xFFE0E0E0);
        for (int slot = 0; slot < choices.size(); slot++) {
            graphics.centeredText(font, Integer.toString(slot + 1), cellX(slot) + cellWidth() / 2,
                    cellY(slot) - 10, 0xFFFFFFFF);
            if (!choices.get(slot).icon().isEmpty()) {
                graphics.fakeItem(choices.get(slot).icon(), cellX(slot) + 3, cellY(slot) + 2);
            }
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    private int cellWidth() {
        return Math.min(150, (width - 32) / 3);
    }

    private int cellX(int slot) {
        int stride = cellWidth() + 8;
        return (width - 3 * stride + 8) / 2 + (slot % 3) * stride;
    }

    private int cellY(int slot) {
        return 94 + (slot / 3) * 30;
    }

    private Component toggleLabel() {
        return text(enabled ? "enabled" : "disabled");
    }

    static Component text(String key) {
        return Component.translatable("screen.smartmendcraft.hotbar." + key);
    }
}
