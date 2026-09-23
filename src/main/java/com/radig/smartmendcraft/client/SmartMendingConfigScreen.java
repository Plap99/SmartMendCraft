package com.radig.smartmendcraft.client;

import com.radig.smartmendcraft.config.SmartMendingConfig;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class SmartMendingConfigScreen extends Screen {

    private final Screen parent;

    public SmartMendingConfigScreen(Screen parent) {
        super(new LiteralText("Smart Mend Craft"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 50;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;

        // MANO PRINCIPAL
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - buttonWidth / 2,
                        startY,
                        buttonWidth,
                        buttonHeight,
                        getMainHandText(),
                        button -> {
                            SmartMendingConfig.setRepairMainHand(
                                    !SmartMendingConfig.repairMainHand()
                            );

                            button.setMessage(getMainHandText());
                        }
                )
        );

        // MANO SECUNDARIA
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - buttonWidth / 2,
                        startY + spacing,
                        buttonWidth,
                        buttonHeight,
                        getOffHandText(),
                        button -> {
                            SmartMendingConfig.setRepairOffHand(
                                    !SmartMendingConfig.repairOffHand()
                            );

                            button.setMessage(getOffHandText());
                        }
                )
        );

        // ARMADURA
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - buttonWidth / 2,
                        startY + spacing * 2,
                        buttonWidth,
                        buttonHeight,
                        getArmorText(),
                        button -> {
                            SmartMendingConfig.setRepairArmor(
                                    !SmartMendingConfig.repairArmor()
                            );

                            button.setMessage(getArmorText());
                        }
                )
        );

        // INVENTARIO
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - buttonWidth / 2,
                        startY + spacing * 3,
                        buttonWidth,
                        buttonHeight,
                        getInventoryText(),
                        button -> {
                            SmartMendingConfig.setRepairInventory(
                                    !SmartMendingConfig.repairInventory()
                            );

                            button.setMessage(getInventoryText());
                        }
                )
        );

        // LISTO
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - 50,
                        startY + spacing * 5,
                        100,
                        buttonHeight,
                        new LiteralText("Listo"),
                        button -> onClose()
                )
        );
    }

    private Text getMainHandText() {
        return new LiteralText(
                "Mano principal: "
                        + (SmartMendingConfig.repairMainHand() ? "ON" : "OFF")
        );
    }

    private Text getOffHandText() {
        return new LiteralText(
                "Mano secundaria: "
                        + (SmartMendingConfig.repairOffHand() ? "ON" : "OFF")
        );
    }

    private Text getArmorText() {
        return new LiteralText(
                "Armadura: "
                        + (SmartMendingConfig.repairArmor() ? "ON" : "OFF")
        );
    }

    private Text getInventoryText() {
        return new LiteralText(
                "Inventario: "
                        + (SmartMendingConfig.repairInventory() ? "ON" : "OFF")
        );
    }

    @Override
    public void render(
            MatrixStack matrices,
            int mouseX,
            int mouseY,
            float delta) {

        this.renderBackground(matrices);

        drawCenteredText(
                matrices,
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}