package com.radig.smartmendcraft.client;

import java.util.List;

import com.radig.smartmendcraft.client.config.HudPosition;
import com.radig.smartmendcraft.client.config.SmartMendingClientConfig;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class SmartMendingConfigScreen extends Screen {

    private final Screen parent;

    public SmartMendingConfigScreen(Screen parent) {

        super(
                new TranslatableText(
                        "screen.smartmendcraft.title"
                )
        );

        this.parent = parent;
    }

    /**
     * Obtiene la configuración personal del jugador local.
     */
    private PlayerMendingConfig getConfig() {

        if (this.client == null
                || this.client.player == null) {

            return null;
        }

        return PlayerMendingConfigs.get(
                this.client.player
        );
    }

    /**
     * Indica si debemos utilizar el diseño compacto.
     */
    private boolean isCompact() {

        return this.width < 600;
    }

    @Override
    protected void init() {

        super.init();

        createButtons();
    }

    private void createButtons() {

        PlayerMendingConfig config =
                getConfig();

        if (config == null) {
            return;
        }

        boolean compact =
                isCompact();

        int centerX =
                this.width / 2;

        /*
         * -------------------------------------------------
         * MEDIDAS RESPONSIVE
         * -------------------------------------------------
         *
         * En ventana pequeña reducimos:
         *
         * - separación entre columnas
         * - ancho de botones
         * - botones de flechas
         * - separación vertical
         */
        int columnOffset =
                compact ? 88 : 115;

        int leftCenterX =
                centerX - columnOffset;

        int rightCenterX =
                centerX + columnOffset;

        int startY =
                compact ? 60 : 72;

        int rowSpacing =
                compact ? 22 : 24;

        int categoryWidth =
                compact ? 112 : 145;

        int arrowWidth =
                compact ? 18 : 22;

        int rightButtonWidth =
                compact ? 160 : 200;

        /*
         * -------------------------------------------------
         * COLUMNA IZQUIERDA
         * PRIORIDAD DE REPARACIÓN
         * -------------------------------------------------
         */

        List<MendingTarget> priority =
                config.getPriority();

        /*
         * Ancho total aproximado de la fila izquierda:
         *
         * categoría + separación + ↑ + separación + ↓
         */
        int leftRowWidth =
                categoryWidth
                        + 4
                        + arrowWidth
                        + 4
                        + arrowWidth;

        int leftStartX =
                leftCenterX
                        - (leftRowWidth / 2);

        for (int index = 0;
                index < priority.size();
                index++) {

            MendingTarget target =
                    priority.get(index);

            int y =
                    startY
                            + (index * rowSpacing);

            /*
             * Botón ON/OFF de la categoría.
             */
            this.addDrawableChild(
                    new ButtonWidget(
                            leftStartX,
                            y,
                            categoryWidth,
                            20,
                            getTargetText(
                                    target,
                                    config,
                                    compact
                            ),
                            button -> {

                                toggleTarget(
                                        target,
                                        config
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    )
            );

            /*
             * Subir prioridad.
             */
            int upX =
                    leftStartX
                            + categoryWidth
                            + 4;

            ButtonWidget upButton =
                    new ButtonWidget(
                            upX,
                            y,
                            arrowWidth,
                            20,
                            new LiteralText("↑"),
                            button -> {

                                config.movePriorityUp(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    );

            upButton.active =
                    index > 0;

            this.addDrawableChild(
                    upButton
            );

            /*
             * Bajar prioridad.
             */
            int downX =
                    upX
                            + arrowWidth
                            + 4;

            ButtonWidget downButton =
                    new ButtonWidget(
                            downX,
                            y,
                            arrowWidth,
                            20,
                            new LiteralText("↓"),
                            button -> {

                                config.movePriorityDown(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    );

            downButton.active =
                    index < priority.size() - 1;

            this.addDrawableChild(
                    downButton
            );
        }

        /*
         * -------------------------------------------------
         * COLUMNA DERECHA
         * MODO DEL INVENTARIO
         * -------------------------------------------------
         */

        int rightButtonX =
                rightCenterX
                        - (rightButtonWidth / 2);

        this.addDrawableChild(
                new ButtonWidget(
                        rightButtonX,
                        startY,
                        rightButtonWidth,
                        20,
                        getInventoryModeText(config),
                        button -> {

                            toggleInventoryMode(config);

                            sendConfig(config);
                            refreshScreen();
                        }
                )
        );

        /*
         * -------------------------------------------------
         * COLUMNA DERECHA
         * NOTIFICACIÓN
         * -------------------------------------------------
         */

        int hudStartY =
                compact
                        ? startY + 52
                        : startY + 72;

        this.addDrawableChild(
                new ButtonWidget(
                        rightButtonX,
                        hudStartY,
                        rightButtonWidth,
                        20,
                        getHudEnabledText(),
                        button -> {

                            SmartMendingClientConfig
                                    .toggleHud();

                            refreshScreen();
                        }
                )
        );

        this.addDrawableChild(
                new ButtonWidget(
                        rightButtonX,
                        hudStartY + 24,
                        rightButtonWidth,
                        20,
                        getHudPositionText(
                                compact
                        ),
                        button -> {

                            SmartMendingClientConfig
                                    .nextHudPosition();

                            refreshScreen();
                        }
                )
        );

        /*
         * -------------------------------------------------
         * BOTÓN LISTO
         * -------------------------------------------------
         */

        int leftBottom =
                startY
                        + (priority.size()
                        * rowSpacing);

        int rightBottom =
                hudStartY + 44;

        int doneY =
                Math.max(
                        leftBottom,
                        rightBottom
                ) + (compact ? 10 : 18);

        this.addDrawableChild(
                new ButtonWidget(
                        centerX - 50,
                        doneY,
                        100,
                        20,
                        new TranslatableText(
                                "screen.smartmendcraft.done"
                        ),
                        button -> onClose()
                )
        );
    }

    private void toggleTarget(
            MendingTarget target,
            PlayerMendingConfig config) {

        switch (target) {

            case MAIN_HAND:

                config.setRepairMainHand(
                        !config.isRepairMainHand()
                );

                break;

            case OFF_HAND:

                config.setRepairOffHand(
                        !config.isRepairOffHand()
                );

                break;

            case ARMOR:

                config.setRepairArmor(
                        !config.isRepairArmor()
                );

                break;

            case INVENTORY:

                config.setRepairInventory(
                        !config.isRepairInventory()
                );

                break;
        }
    }

    private Text getTargetText(
            MendingTarget target,
            PlayerMendingConfig config,
            boolean compact) {

        switch (target) {

            case MAIN_HAND:

                return new TranslatableText(
                        compact
                                ? "screen.smartmendcraft.main_hand_short"
                                : "screen.smartmendcraft.main_hand",
                        getOnOff(
                                config.isRepairMainHand()
                        )
                );

            case OFF_HAND:

                return new TranslatableText(
                        compact
                                ? "screen.smartmendcraft.off_hand_short"
                                : "screen.smartmendcraft.off_hand",
                        getOnOff(
                                config.isRepairOffHand()
                        )
                );

            case ARMOR:

                return new TranslatableText(
                        "screen.smartmendcraft.armor",
                        getOnOff(
                                config.isRepairArmor()
                        )
                );

            case INVENTORY:

                return new TranslatableText(
                        "screen.smartmendcraft.inventory",
                        getOnOff(
                                config.isRepairInventory()
                        )
                );

            default:

                return new LiteralText(
                        target.name()
                );
        }
    }

    private Text getOnOff(
            boolean enabled) {

        return new TranslatableText(
                enabled
                        ? "screen.smartmendcraft.on"
                        : "screen.smartmendcraft.off"
        );
    }

    private void toggleInventoryMode(
            PlayerMendingConfig config) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            config.setInventoryMode(
                    InventoryMendingMode.FINISH_ITEM
            );

        } else {

            config.setInventoryMode(
                    InventoryMendingMode.BALANCE
            );
        }
    }

    private Text getInventoryModeText(
            PlayerMendingConfig config) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return new TranslatableText(
                    "screen.smartmendcraft.mode.balance"
            );
        }

        return new TranslatableText(
                "screen.smartmendcraft.mode.finish"
        );
    }

    private Text getInventoryModeDescription(
            PlayerMendingConfig config) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return new TranslatableText(
                    "screen.smartmendcraft.mode.balance.description"
            );
        }

        return new TranslatableText(
                "screen.smartmendcraft.mode.finish.description"
        );
    }

    private Text getHudEnabledText() {

        return new TranslatableText(
                "screen.smartmendcraft.notification",
                getOnOff(
                        SmartMendingClientConfig
                                .isShowHud()
                )
        );
    }

    private Text getHudPositionText(
            boolean compact) {

        HudPosition position =
                SmartMendingClientConfig
                        .getHudPosition();

        return new TranslatableText(
                "screen.smartmendcraft.position",
                getHudPositionName(
                        position,
                        compact
                )
        );
    }

    /**
     * Devuelve el nombre traducido de la posición
     * del HUD.
     */
    private Text getHudPositionName(
            HudPosition position,
            boolean compact) {

        String suffix;

        switch (position) {

            case TOP_LEFT:
                suffix = "top_left";
                break;

            case TOP_CENTER:
                suffix = "top_center";
                break;

            case TOP_RIGHT:
                suffix = "top_right";
                break;

            case BOTTOM_LEFT:
                suffix = "bottom_left";
                break;

            case BOTTOM_RIGHT:
                suffix = "bottom_right";
                break;

            default:
                suffix = "top_left";
                break;
        }

        String key =
                "screen.smartmendcraft.position."
                        + suffix;

        if (compact) {
            key += "_short";
        }

        return new TranslatableText(
                key
        );
    }

    /**
     * Envía al servidor la configuración personal
     * después de realizar cualquier cambio.
     */
    private void sendConfig(
            PlayerMendingConfig config) {

        SmartMendingClientNetworking
                .sendConfigUpdate(config);
    }

    /**
     * Reconstruye la pantalla para reflejar
     * inmediatamente los cambios.
     */
    private void refreshScreen() {

        if (this.client != null) {

            this.client.setScreen(
                    new SmartMendingConfigScreen(
                            parent
                    )
            );
        }
    }

    @Override
    public void render(
            MatrixStack matrices,
            int mouseX,
            int mouseY,
            float delta) {

        this.renderBackground(
                matrices
        );

        boolean compact =
                isCompact();

        int centerX =
                this.width / 2;

        int columnOffset =
                compact ? 88 : 115;

        int leftCenterX =
                centerX - columnOffset;

        int rightCenterX =
                centerX + columnOffset;

        int startY =
                compact ? 60 : 72;

        /*
         * -------------------------------------------------
         * TÍTULO PRINCIPAL
         * -------------------------------------------------
         */

        drawCenteredText(
                matrices,
                this.textRenderer,
                this.title,
                centerX,
                compact ? 10 : 20,
                0xFFFFFF
        );

        /*
         * -------------------------------------------------
         * COLUMNA IZQUIERDA
         * -------------------------------------------------
         */

        drawCenteredText(
                matrices,
                this.textRenderer,
                new TranslatableText(
                        "screen.smartmendcraft.repair_priority"
                ),
                leftCenterX,
                startY - 20,
                0xAAAAAA
        );

        /*
         * -------------------------------------------------
         * COLUMNA DERECHA
         * -------------------------------------------------
         */

        PlayerMendingConfig config =
                getConfig();

        if (config != null) {

            drawCenteredText(
                    matrices,
                    this.textRenderer,
                    new TranslatableText(
                            "screen.smartmendcraft.inventory_mode"
                    ),
                    rightCenterX,
                    startY - 20,
                    0xAAAAAA
            );

            /*
             * La descripción larga solamente aparece
             * cuando hay suficiente espacio horizontal.
             */
            if (!compact) {

                drawCenteredText(
                        matrices,
                        this.textRenderer,
                        getInventoryModeDescription(
                                config
                        ),
                        rightCenterX,
                        startY + 25,
                        0x888888
                );
            }

            int hudStartY =
                    compact
                            ? startY + 52
                            : startY + 72;

            drawCenteredText(
                    matrices,
                    this.textRenderer,
                    new TranslatableText(
                            "screen.smartmendcraft.repair_notification"
                    ),
                    rightCenterX,
                    hudStartY - 18,
                    0xAAAAAA
            );
        }

        super.render(
                matrices,
                mouseX,
                mouseY,
                delta
        );
    }

    @Override
    public void onClose() {

        if (this.client != null) {

            this.client.setScreen(
                    parent
            );
        }
    }
}