package com.radig.smartmendcraft.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.radig.smartmendcraft.client.config.HudPosition;
import com.radig.smartmendcraft.client.config.SmartMendingClientConfig;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;

public class SmartMendingConfigScreen extends Screen {

    private final Screen parent;

    public SmartMendingConfigScreen(Screen parent) {
        super(
                Component.translatable(
                        "screen.smartmendcraft.title"
                )
        );

        this.parent = parent;
    }

    /**
     * Obtiene la configuración personal del jugador local.
     */
    private PlayerMendingConfig getConfig() {

        if (this.minecraft == null
                || this.minecraft.player == null) {

            return null;
        }

        return PlayerMendingConfigs.get(
                this.minecraft.player
        );
    }

    /**
     * Indica si debemos utilizar el diseño compacto.
     */
    private boolean isCompact() {
        return this.width < 600;
    }

    /**
     * Ancho máximo permitido para la descripción
     * del modo de reparación del inventario.
     */
    private int getDescriptionMaxWidth(
            boolean compact) {

        return compact ? 150 : 190;
    }

    /**
     * Devuelve las líneas ya ajustadas al ancho
     * disponible en la columna derecha.
     */
    private List<FormattedCharSequence> getInventoryDescriptionLines(
            PlayerMendingConfig config,
            boolean compact) {

        return this.font.split(
                getInventoryModeDescription(config),
                getDescriptionMaxWidth(compact)
        );
    }

    /**
     * Calcula la posición vertical del título del HUD
     * dependiendo de cuántas líneas ocupe la descripción.
     */
    private int getHudTitleY(
            int startY,
            PlayerMendingConfig config,
            boolean compact) {

        int lineCount =
                getInventoryDescriptionLines(
                        config,
                        compact
                ).size();

        int extraHeight =
                Math.max(
                        0,
                        lineCount - 1
                ) * 10;

        return startY
                + 48
                + extraHeight;
    }

    /**
     * Calcula dónde comienzan los botones del HUD.
     */
    private int getHudStartY(
            int startY,
            PlayerMendingConfig config,
            boolean compact) {

        return getHudTitleY(
                startY,
                config,
                compact
        ) + 18;
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
                compact ? 150 : 190;

        List<MendingTarget> priority =
                config.getPriority();

        /*
         * -------------------------------------------------
         * COLUMNA IZQUIERDA
         * Prioridades de reparación
         * -------------------------------------------------
         */

        for (int index = 0;
                index < priority.size();
                index++) {

            MendingTarget target =
                    priority.get(index);

            int y =
                    startY + (index * rowSpacing);

            /*
             * Activa/desactiva la categoría.
             */
            this.addRenderableWidget(
                    Button.builder(
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
                    .bounds(
                            leftCenterX
                                    - (categoryWidth / 2)
                                    - arrowWidth
                                    - 4,
                            y,
                            categoryWidth,
                            20
                    )
                    .build()
            );

            /*
             * Subir prioridad.
             */
            int upX =
                    leftCenterX
                            + (categoryWidth / 2)
                            - arrowWidth;

            Button upButton =
                    Button.builder(
                            Component.literal("↑"),
                            button -> {

                                config.movePriorityUp(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    )
                    .bounds(
                            upX,
                            y,
                            arrowWidth,
                            20
                    )
                    .build();

            upButton.active =
                    index > 0;

            this.addRenderableWidget(
                    upButton
            );

            /*
             * Bajar prioridad.
             */
            int downX =
                    upX + arrowWidth + 2;

            Button downButton =
                    Button.builder(
                            Component.literal("↓"),
                            button -> {

                                config.movePriorityDown(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    )
                    .bounds(
                            downX,
                            y,
                            arrowWidth,
                            20
                    )
                    .build();

            downButton.active =
                    index < priority.size() - 1;

            this.addRenderableWidget(
                    downButton
            );
        }

        /*
         * -------------------------------------------------
         * COLUMNA DERECHA
         * Modo de reparación del inventario
         * -------------------------------------------------
         */

        int inventoryModeY =
                startY;

        this.addRenderableWidget(
                Button.builder(
                        getInventoryModeText(
                                config,
                                compact
                        ),
                        button -> {

                            toggleInventoryMode(
                                    config
                            );

                            sendConfig(config);
                            refreshScreen();
                        }
                )
                .bounds(
                        rightCenterX
                                - (rightButtonWidth / 2),
                        inventoryModeY,
                        rightButtonWidth,
                        20
                )
                .build()
        );

        /*
         * -------------------------------------------------
         * HUD
         * -------------------------------------------------
         */

        int hudStartY =
                getHudStartY(
                        startY,
                        config,
                        compact
                );

        this.addRenderableWidget(
                Button.builder(
                        getHudEnabledText(
                                compact
                        ),
                        button -> {

                            SmartMendingClientConfig
                                    .setShowHud(
                                            !SmartMendingClientConfig
                                                    .isShowHud()
                                    );

                            refreshScreen();
                        }
                )
                .bounds(
                        rightCenterX
                                - (rightButtonWidth / 2),
                        hudStartY,
                        rightButtonWidth,
                        20
                )
                .build()
        );

        this.addRenderableWidget(
                Button.builder(
                        getHudPositionText(
                                compact
                        ),
                        button -> {

                            SmartMendingClientConfig
                                    .nextHudPosition();

                            refreshScreen();
                        }
                )
                .bounds(
                        rightCenterX
                                - (rightButtonWidth / 2),
                        hudStartY + 24,
                        rightButtonWidth,
                        20
                )
                .build()
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

        this.addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "screen.smartmendcraft.done"
                        ),
                        button -> onClose()
                )
                .bounds(
                        centerX - 50,
                        doneY,
                        100,
                        20
                )
                .build()
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

    private Component getTargetText(
            MendingTarget target,
            PlayerMendingConfig config,
            boolean compact) {

        switch (target) {

            case MAIN_HAND:

                return Component.translatable(
                        compact
                                ? "screen.smartmendcraft.main_hand_short"
                                : "screen.smartmendcraft.main_hand",
                        getOnOff(
                                config.isRepairMainHand()
                        )
                );

            case OFF_HAND:

                return Component.translatable(
                        compact
                                ? "screen.smartmendcraft.off_hand_short"
                                : "screen.smartmendcraft.off_hand",
                        getOnOff(
                                config.isRepairOffHand()
                        )
                );

            case ARMOR:

                return Component.translatable(
                        "screen.smartmendcraft.armor",
                        getOnOff(
                                config.isRepairArmor()
                        )
                );

            case INVENTORY:

                return Component.translatable(
                        compact
                                ? "screen.smartmendcraft.inventory_short"
                                : "screen.smartmendcraft.inventory",
                        getOnOff(
                                config.isRepairInventory()
                        )
                );

            default:

                return Component.literal(
                        target.name()
                );
        }
    }

    private String getOnOff(
            boolean enabled) {

        return enabled
                ? "ON"
                : "OFF";
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

    private Component getInventoryModeText(
            PlayerMendingConfig config,
            boolean compact) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return Component.translatable(
                    compact
                            ? "screen.smartmendcraft.mode.balance_short"
                            : "screen.smartmendcraft.mode.balance"
            );
        }

        return Component.translatable(
                compact
                        ? "screen.smartmendcraft.mode.finish_short"
                        : "screen.smartmendcraft.mode.finish"
        );
    }

    private Component getInventoryModeDescription(
            PlayerMendingConfig config) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return Component.translatable(
                    "screen.smartmendcraft.mode.balance_description"
            );
        }

        return Component.translatable(
                "screen.smartmendcraft.mode.finish_description"
        );
    }

    private Component getHudEnabledText(
            boolean compact) {

        return Component.translatable(
                compact
                        ? "screen.smartmendcraft.hud_short"
                        : "screen.smartmendcraft.hud",
                getOnOff(
                        SmartMendingClientConfig
                                .isShowHud()
                )
        );
    }

    private Component getHudPositionText(
            boolean compact) {

        HudPosition position =
                SmartMendingClientConfig
                        .getHudPosition();

        return Component.translatable(
                compact
                        ? "screen.smartmendcraft.position_short"
                        : "screen.smartmendcraft.position",
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
    private Component getHudPositionName(
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

        return Component.translatable(
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

        if (this.minecraft != null) {

            this.minecraft.setScreenAndShow(
                    new SmartMendingConfigScreen(
                            parent
                    )
            );
        }
    }

    @Override
        public void extractRenderState(
                GuiGraphicsExtractor context,
                int mouseX,
                int mouseY,
                float delta) {

        /*
        * Extract the base screen and widgets first.
        * Our custom text is added afterwards so it
        * remains visible above the screen background.
        */
        super.extractRenderState(
                context,
                mouseX,
                mouseY,
                delta
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
        * Main title.
        */
        context.centeredText(
                this.font,
                this.title,
                centerX,
                compact ? 10 : 20,
                0xFFFFFFFF
        );

        /*
        * Left column.
        */
        context.centeredText(
                this.font,
                Component.translatable(
                        "screen.smartmendcraft.repair_priority"
                ),
                leftCenterX,
                startY - 20,
                0xFFAAAAAA
        );

        /*
        * Right column.
        */
        PlayerMendingConfig config =
                getConfig();

        if (config != null) {

                context.centeredText(
                        this.font,
                        Component.translatable(
                                "screen.smartmendcraft.inventory_mode"
                        ),
                        rightCenterX,
                        startY - 20,
                        0xFFAAAAAA
                );

                /*
                * Inventory mode description.
                */
                List<FormattedCharSequence> descriptionLines =
                        getInventoryDescriptionLines(
                                config,
                                compact
                        );

                int descriptionY =
                        startY + 25;

                for (FormattedCharSequence line : descriptionLines) {

                context.centeredText(
                        this.font,
                        line,
                        rightCenterX,
                        descriptionY,
                        0xFF888888
                );

                descriptionY += 10;
                }

                /*
                * HUD title.
                */
                int hudTitleY =
                        getHudTitleY(
                                startY,
                                config,
                                compact
                        );

                context.centeredText(
                        this.font,
                        Component.translatable(
                                "screen.smartmendcraft.hud_title"
                        ),
                        rightCenterX,
                        hudTitleY,
                        0xFFAAAAAA
                );
        }
        }

    @Override
    public void onClose() {

        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}