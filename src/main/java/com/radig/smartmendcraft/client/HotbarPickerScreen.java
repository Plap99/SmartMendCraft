package com.radig.smartmendcraft.client;

import static com.radig.smartmendcraft.client.HotbarConfigScreen.text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.radig.smartmendcraft.hotbar.HotbarPreferences;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

final class HotbarPickerScreen extends Screen {

    private static final int COLUMNS = 2;
    private static final int ROW_HEIGHT = 22;
    private static final int RESULTS_Y = 73;
    private static final Comparator<HotbarChoice> BY_NAME = Comparator
            .comparing((HotbarChoice choice) -> HotbarChoice.searchText(choice.name().getString()))
            .thenComparing(HotbarChoice::rule);

    private final Screen parent;
    private final Consumer<String> onSelect;
    private final List<HotbarChoice> items;
    private final List<HotbarChoice> tags;
    private final List<Button> results = new ArrayList<>();
    private List<HotbarChoice> matches = List.of();
    private String query = "";
    private boolean showTags;
    private int page;
    private Button previous;
    private Button next;

    HotbarPickerScreen(Screen parent, int slot, String currentRule, Consumer<String> onSelect) {
        super(Component.translatable("screen.smartmendcraft.hotbar.slot", slot + 1));
        this.parent = parent;
        this.onSelect = onSelect;
        showTags = currentRule.startsWith("#");
        items = BuiltInRegistries.ITEM.stream().map(HotbarChoice::forItem)
                .filter(HotbarPickerScreen::canChoose)
                .sorted(BY_NAME).toList();
        tags = BuiltInRegistries.ITEM.getTags().map(HotbarChoice::forTag)
                .filter(HotbarPickerScreen::canChoose).sorted(BY_NAME).toList();
    }

    @Override
    protected void init() {
        int center = width / 2;
        int bottom = height - 29;
        int columnWidth = columnWidth();
        int pageSize = COLUMNS * Math.max(1, (bottom - RESULTS_Y - 28) / ROW_HEIGHT);

        EditBox search = new EditBox(font, center - 102, 29, 204, 20, text("search"));
        search.setMaxLength(100);
        search.setHint(text("search"));
        search.setValue(query);
        addRenderableWidget(search);
        search.setResponder(value -> {
            query = value;
            page = 0;
            filter();
        });

        Button itemsButton = addRenderableWidget(Button.builder(text("items"), button -> {
            showTags = false;
            page = 0;
            filter();
        }).bounds(center - 102, 51, 100, 20).build());
        Button tagsButton = addRenderableWidget(Button.builder(text("tags"), button -> {
            showTags = true;
            page = 0;
            filter();
        }).bounds(center + 2, 51, 100, 20).build());

        results.clear();
        for (int index = 0; index < pageSize; index++) {
            int result = index;
            Button button = addRenderableWidget(Button.builder(Component.empty(),
                    ignored -> select(matches.get(page * results.size() + result).rule()))
                    .bounds(resultX(index), resultY(index), columnWidth, 20).build());
            results.add(button);
        }

        previous = addRenderableWidget(Button.builder(Component.literal("‹"), button -> {
            page--;
            refreshResults();
        }).bounds(center - 75, bottom - 23, 28, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal("›"), button -> {
            page++;
            refreshResults();
        }).bounds(center + 47, bottom - 23, 28, 20).build());
        previous.setTooltip(Tooltip.create(text("previous")));
        next.setTooltip(Tooltip.create(text("next")));
        addRenderableWidget(Button.builder(text("clear"), button -> select(""))
                .bounds(center - 102, bottom, 100, 20).build());
        addRenderableWidget(Button.builder(text("cancel"), button -> onClose())
                .bounds(center + 2, bottom, 100, 20).build());
        itemsButton.setTooltip(Tooltip.create(text("items")));
        tagsButton.setTooltip(Tooltip.create(text("tags")));
        filter();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Component mode = text(showTags ? "tags" : "items");
        graphics.centeredText(font, Component.translatable("screen.smartmendcraft.hotbar.picker_title", title, mode),
                width / 2, 13, 0xFFFFFFFF);
        if (matches.isEmpty()) {
            graphics.centeredText(font, text("no_results"), width / 2, RESULTS_Y + 15, 0xFFE0E0E0);
        }
        int count = Math.min(results.size(), matches.size() - page * results.size());
        for (int index = 0; index < count; index++) {
            graphics.fakeItem(matches.get(page * results.size() + index).icon(),
                    resultX(index) + 3, resultY(index) + 2);
        }
        graphics.centeredText(font, (page + 1) + " / " + pageCount(), width / 2, height - 46, 0xFFE0E0E0);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    private static boolean canChoose(HotbarChoice choice) {
        return !choice.icon().isEmpty() && HotbarPreferences.isValidRule(choice.rule());
    }

    private void select(String rule) {
        onSelect.accept(rule);
        onClose();
    }

    private void filter() {
        String needle = HotbarChoice.searchText(query.strip());
        matches = (showTags ? tags : items).stream()
                .filter(choice -> HotbarChoice.searchText(choice.name().getString()).contains(needle)
                        || choice.rule().contains(needle))
                .toList();
        page = Math.min(page, pageCount() - 1);
        refreshResults();
    }

    private void refreshResults() {
        for (int index = 0; index < results.size(); index++) {
            Button button = results.get(index);
            int match = page * results.size() + index;
            button.visible = button.active = match < matches.size();
            if (button.visible) {
                HotbarChoice choice = matches.get(match);
                button.setMessage(choice.shortName(font, button.getWidth() - 44));
                button.setTooltip(Tooltip.create(choice.name()));
            }
        }
        previous.active = page > 0;
        next.active = page + 1 < pageCount();
    }

    private int pageCount() {
        return Math.max(1, (matches.size() + results.size() - 1) / results.size());
    }

    private int columnWidth() {
        return Math.min(165, (width - 24) / COLUMNS);
    }

    private int resultX(int index) {
        return (width - COLUMNS * columnWidth() - 8) / 2 + (index % COLUMNS) * (columnWidth() + 8);
    }

    private static int resultY(int index) {
        return RESULTS_Y + (index / COLUMNS) * ROW_HEIGHT;
    }
}
