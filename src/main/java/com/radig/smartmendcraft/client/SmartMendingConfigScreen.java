package com.radig.smartmendcraft.client;

import java.util.List;

import com.radig.smartmendcraft.client.config.HudPosition;
import com.radig.smartmendcraft.client.config.SmartMendingClientConfig;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class SmartMendingConfigScreen extends Screen {

    private final Screen parent;

    public SmartMendingConfigScreen(Screen parent) {
        super(
                Text.translatable(
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
    private List<OrderedText> getInventoryDescriptionLines(
            PlayerMendingConfig config,
            boolean compact) {

        return this.textRenderer.wrapLines(
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
            this.addDrawableChild(
                    ButtonWidget.builder(
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
                    .dimensions(
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

            ButtonWidget upButton =
                    ButtonWidget.builder(
                            Text.literal("↑"),
                            button -> {

                                config.movePriorityUp(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    )
                    .dimensions(
                            upX,
                            y,
                            arrowWidth,
                            20
                    )
                    .build();

            upButton.active =
                    index > 0;

            this.addDrawableChild(
                    upButton
            );

            /*
             * Bajar prioridad.
             */
            int downX =
                    upX + arrowWidth + 2;

            ButtonWidget downButton =
                    ButtonWidget.builder(
                            Text.literal("↓"),
                            button -> {

                                config.movePriorityDown(
                                        target
                                );

                                sendConfig(config);
                                refreshScreen();
                            }
                    )
                    .dimensions(
                            downX,
                            y,
                            arrowWidth,
                            20
                    )
                    .build();

            downButton.active =
                    index < priority.size() - 1;

            this.addDrawableChild(
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

        this.addDrawableChild(
                ButtonWidget.builder(
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
                .dimensions(
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

        this.addDrawableChild(
                ButtonWidget.builder(
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
                .dimensions(
                        rightCenterX
                                - (rightButtonWidth / 2),
                        hudStartY,
                        rightButtonWidth,
                        20
                )
                .build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(
                        getHudPositionText(
                                compact
                        ),
                        button -> {

                            SmartMendingClientConfig
                                    .nextHudPosition();

                            refreshScreen();
                        }
                )
                .dimensions(
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

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable(
                                "screen.smartmendcraft.done"
                        ),
                        button -> close()
                )
                .dimensions(
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

    private Text getTargetText(
            MendingTarget target,
            PlayerMendingConfig config,
            boolean compact) {

        switch (target) {

            case MAIN_HAND:

                return Text.translatable(
                        compact
                                ? "screen.smartmendcraft.main_hand_short"
                                : "screen.smartmendcraft.main_hand",
                        getOnOff(
                                config.isRepairMainHand()
                        )
                );

            case OFF_HAND:

                return Text.translatable(
                        compact
                                ? "screen.smartmendcraft.off_hand_short"
                                : "screen.smartmendcraft.off_hand",
                        getOnOff(
                                config.isRepairOffHand()
                        )
                );

            case ARMOR:

                return Text.translatable(
                        "screen.smartmendcraft.armor",
                        getOnOff(
                                config.isRepairArmor()
                        )
                );

            case INVENTORY:

                return Text.translatable(
                        compact
                                ? "screen.smartmendcraft.inventory_short"
                                : "screen.smartmendcraft.inventory",
                        getOnOff(
                                config.isRepairInventory()
                        )
                );

            default:

                return Text.literal(
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

    private Text getInventoryModeText(
            PlayerMendingConfig config,
            boolean compact) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return Text.translatable(
                    compact
                            ? "screen.smartmendcraft.mode.balance_short"
                            : "screen.smartmendcraft.mode.balance"
            );
        }

        return Text.translatable(
                compact
                        ? "screen.smartmendcraft.mode.finish_short"
                        : "screen.smartmendcraft.mode.finish"
        );
    }

    private Text getInventoryModeDescription(
            PlayerMendingConfig config) {

        if (config.getInventoryMode()
                == InventoryMendingMode.BALANCE) {

            return Text.translatable(
                    "screen.smartmendcraft.mode.balance_description"
            );
        }

        return Text.translatable(
                "screen.smartmendcraft.mode.finish_description"
        );
    }

    private Text getHudEnabledText(
            boolean compact) {

        return Text.translatable(
                compact
                        ? "screen.smartmendcraft.hud_short"
                        : "screen.smartmendcraft.hud",
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

        return Text.translatable(
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

        return Text.translatable(
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
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta) {

        this.renderBackground(
                context
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
         * Título principal.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                centerX,
                compact ? 10 : 20,
                0xFFFFFF
        );

        /*
         * Columna izquierda.
         */
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable(
                        "screen.smartmendcraft.repair_priority"
                ),
                leftCenterX,
                startY - 20,
                0xAAAAAA
        );

        /*
         * Columna derecha.
         */
        PlayerMendingConfig config =
                getConfig();

        if (config != null) {

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable(
                            "screen.smartmendcraft.inventory_mode"
                    ),
                    rightCenterX,
                    startY - 20,
                    0xAAAAAA
            );

            /*
             * Descripción del modo de inventario.
             *
             * Se divide automáticamente en varias líneas
             * para evitar que invada la columna izquierda.
             */
            List<OrderedText> descriptionLines =
                    getInventoryDescriptionLines(
                            config,
                            compact
                    );

            int descriptionY =
                    startY + 25;

            for (OrderedText line : descriptionLines) {

                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        line,
                        rightCenterX,
                        descriptionY,
                        0x888888
                );

                descriptionY += 10;
            }

            /*
             * Título del HUD.
             *
             * Su posición depende ahora del número de líneas
             * utilizadas por la descripción anterior.
             */
            int hudTitleY =
                    getHudTitleY(
                            startY,
                            config,
                            compact
                    );

            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.translatable(
                            "screen.smartmendcraft.hud_title"
                    ),
                    rightCenterX,
                    hudTitleY,
                    0xAAAAAA
            );
        }

        super.render(
                context,
                mouseX,
                mouseY,
                delta
        );
    }

    @Override
    public void close() {

        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}