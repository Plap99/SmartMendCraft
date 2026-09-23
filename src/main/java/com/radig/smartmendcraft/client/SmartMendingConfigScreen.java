package com.radig.smartmendcraft.client;

import java.util.List;

import com.radig.smartmendcraft.config.MendingTarget;
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

        createButtons();
    }

    private void createButtons() {

        int centerX = this.width / 2;
        int startY = 50;
        int rowSpacing = 24;

        List<MendingTarget> priority =
                SmartMendingConfig.getPriority();

        for (int index = 0; index < priority.size(); index++) {

            MendingTarget target = priority.get(index);

            int y = startY + (index * rowSpacing);

            /*
             * Botón principal:
             * activa/desactiva la categoría.
             */
            this.addDrawableChild(
                    new ButtonWidget(
                            centerX - 100,
                            y,
                            140,
                            20,
                            getTargetText(target),
                            button -> {
                                toggleTarget(target);
                                refreshScreen();
                            }
                    )
            );

            /*
             * Subir prioridad.
             */
            ButtonWidget upButton =
                    new ButtonWidget(
                            centerX + 44,
                            y,
                            24,
                            20,
                            new LiteralText("↑"),
                            button -> {
                                SmartMendingConfig.movePriorityUp(target);
                                refreshScreen();
                            }
                    );

            upButton.active = index > 0;

            this.addDrawableChild(upButton);

            /*
             * Bajar prioridad.
             */
            ButtonWidget downButton =
                    new ButtonWidget(
                            centerX + 72,
                            y,
                            24,
                            20,
                            new LiteralText("↓"),
                            button -> {
                                SmartMendingConfig.movePriorityDown(target);
                                refreshScreen();
                            }
                    );

            downButton.active =
                    index < priority.size() - 1;

            this.addDrawableChild(downButton);
        }

        /*
         * Botón Listo.
         */
        this.addDrawableChild(
                new ButtonWidget(
                        centerX - 50,
                        startY + 120,
                        100,
                        20,
                        new LiteralText("Listo"),
                        button -> onClose()
                )
        );
    }

    private void toggleTarget(MendingTarget target) {

        switch (target) {

            case MAIN_HAND:
                SmartMendingConfig.setRepairMainHand(
                        !SmartMendingConfig.repairMainHand()
                );
                break;

            case OFF_HAND:
                SmartMendingConfig.setRepairOffHand(
                        !SmartMendingConfig.repairOffHand()
                );
                break;

            case ARMOR:
                SmartMendingConfig.setRepairArmor(
                        !SmartMendingConfig.repairArmor()
                );
                break;

            case INVENTORY:
                SmartMendingConfig.setRepairInventory(
                        !SmartMendingConfig.repairInventory()
                );
                break;
        }
    }

    private Text getTargetText(MendingTarget target) {

        switch (target) {

            case MAIN_HAND:
                return new LiteralText(
                        "Mano principal: "
                                + getOnOff(
                                        SmartMendingConfig.repairMainHand()
                                )
                );

            case OFF_HAND:
                return new LiteralText(
                        "Mano secundaria: "
                                + getOnOff(
                                        SmartMendingConfig.repairOffHand()
                                )
                );

            case ARMOR:
                return new LiteralText(
                        "Armadura: "
                                + getOnOff(
                                        SmartMendingConfig.repairArmor()
                                )
                );

            case INVENTORY:
                return new LiteralText(
                        "Inventario: "
                                + getOnOff(
                                        SmartMendingConfig.repairInventory()
                                )
                );

            default:
                return new LiteralText(target.name());
        }
    }

    private String getOnOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    /**
     * Reconstruye la pantalla para reflejar inmediatamente
     * los cambios de orden o estado.
     */
    private void refreshScreen() {

        if (this.client != null) {
            this.client.setScreen(
                    new SmartMendingConfigScreen(parent)
            );
        }
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

        drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Prioridad de reparación"),
                this.width / 2,
                35,
                0xAAAAAA
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