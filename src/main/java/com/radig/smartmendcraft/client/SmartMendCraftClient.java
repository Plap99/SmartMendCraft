package com.radig.smartmendcraft.client;
import com.radig.smartmendcraft.SmartMendCraft;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import com.radig.smartmendcraft.client.config.HudPosition;
import com.radig.smartmendcraft.client.config.SmartMendingClientConfig;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.network.SmartMendingNetworking;

import com.radig.smartmendcraft.network.payload.ConfigSyncPayload;
import com.radig.smartmendcraft.network.payload.MendingUpdatePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class SmartMendCraftClient implements ClientModInitializer {

    private static KeyMapping openConfigKey;

        private static final KeyMapping.Category SMART_MEND_CRAFT_CATEGORY =
                KeyMapping.Category.register(
                        SmartMendCraft.id("controls")
                );

    @Override
    public void onInitializeClient() {

        /*
         * Configuracion visual local.
         */
        SmartMendingClientConfig.load();

        /*
         * -------------------------------------------------
         * HANDSHAKE CON EL SERVIDOR
         * -------------------------------------------------
         *
         * Al entrar a un servidor/mundo, informamos que
         * este cliente tiene SmartMendCraft instalado.
         */
        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, client) -> {
                    SmartMendingClientNetworking
                            .sendClientHello();
                }
        );

        /*
         * -------------------------------------------------
         * TECLA F7
         * -------------------------------------------------
         */
        openConfigKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.smartmendcraft.open_config",
                        InputConstants.KEY_F7,
                        SMART_MEND_CRAFT_CATEGORY
                )
        );

        /*
        * -------------------------------------------------
        * ACTUALIZACION DEL HUD DESDE EL SERVIDOR
        * -------------------------------------------------
        */
        ClientPlayNetworking.registerGlobalReceiver(
                MendingUpdatePayload.TYPE,
                (payload, context) -> {

                context.client().execute(() -> {

                        SmartMendingHudData.update(
                                payload.stack(),
                                payload.repairAmount(),
                                payload.currentDamage(),
                                payload.maxDamage()
                        );
                });
                }
        );

        /*
        * -------------------------------------------------
        * SINCRONIZACION DE CONFIGURACION PERSONAL
        * -------------------------------------------------
        */
        ClientPlayNetworking.registerGlobalReceiver(
                ConfigSyncPayload.TYPE,
                (payload, context) -> {

                context.client().execute(() -> {

                        Minecraft client =
                                context.client();

                        if (client.player == null) {
                        return;
                        }

                        PlayerMendingConfig config =
                                PlayerMendingConfigs.get(
                                        client.player
                                );

                        config.setRepairMainHand(
                                payload.repairMainHand()
                        );

                        config.setRepairOffHand(
                                payload.repairOffHand()
                        );

                        config.setRepairArmor(
                                payload.repairArmor()
                        );

                        config.setRepairInventory(
                                payload.repairInventory()
                        );

                        config.setInventoryMode(
                                payload.inventoryMode()
                        );

                        config.setPriority(
                                List.copyOf(payload.priority())
                        );
                });
                }
        );

        /*
         * -------------------------------------------------
         * HUD DE REPARACION
         * -------------------------------------------------
         */
        HudElementRegistry.addLast(
                SmartMendCraft.id("mending_hud"),
                (drawContext, tickDelta) -> {

                    if (!SmartMendingClientConfig.isShowHud()) {
                        return;
                    }

                    if (!SmartMendingHudData.shouldDisplay()) {
                        return;
                    }

                    Minecraft client =
                            Minecraft.getInstance();

                    if (client.font == null) {
                        return;
                    }

                    ItemStack stack =
                            SmartMendingHudData.getLastItem();

                    if (stack == null
                            || stack.isEmpty()) {

                        return;
                    }

                    /*
                     * -------------------------------------------------
                     * DATOS DEL HUD
                     * -------------------------------------------------
                     */
                    double durability =
                            Math.max(
                                    0.0,
                                    Math.min(
                                            100.0,
                                            SmartMendingHudData
                                                    .getDurabilityPercentage()
                                    )
                            );

                    int percentage =
                            (int) Math.round(
                                    durability
                            );

                    String text =
                            percentage + "%";

                    /*
                     * Trabajamos internamente con el tamano normal
                     * de Minecraft (icono de 16x16).
                     *
                     * Despues escalamos todo el conjunto.
                     */
                    final int iconSize = 16;
                    final int spacing = 3;
                    final int barHeight = 2;

                    int textWidth =
                            client.font
                                    .width(text);

                    int textHeight =
                            client.font
                                    .lineHeight;

                    int hudWidth =
                            iconSize
                                    + spacing
                                    + textWidth;

                    int hudHeight =
                            iconSize
                                    + 4;

                    /*
                     * -------------------------------------------------
                     * ESCALA + PULSO
                     * -------------------------------------------------
                     */
                    final float baseScale =
                        SmartMendingClientConfig
                                .getHudScale();

                    final float pulseStrength =
                            0.06F;

                    float pulseProgress =
                            SmartMendingHudData
                                    .getPulseProgress();

                    /*
                     * Curva del pulso.
                     *
                     * getPulseProgress():
                     *
                     * 1 --------> 0
                     *
                     * Convertimos eso en un pulso:
                     *
                     * normal -> crece -> normal
                     */
                    float pulseWave =
                            (float) Math.sin(
                                    pulseProgress
                                            * Math.PI
                            );

                    float scale =
                            baseScale
                                    + (pulseWave
                                    * pulseStrength);

                    /*
                     * -------------------------------------------------
                     * FADE-OUT
                     * -------------------------------------------------
                     */
                    float fade =
                            SmartMendingHudData
                                    .getFadeProgress();

                    /*
                     * Alpha de 0 a 255 para texto y barra.
                     */
                    int alpha =
                            Math.max(
                                    0,
                                    Math.min(
                                            255,
                                            Math.round(
                                                    255.0F * fade
                                            )
                                    )
                            );

                    /*
                     * Tamano real que ocupara en pantalla
                     * despues de aplicar la escala.
                     */
                    int scaledHudWidth =
                            Math.round(
                                    hudWidth * scale
                            );

                    int scaledHudHeight =
                            Math.round(
                                    hudHeight * scale
                            );

                    /*
                     * -------------------------------------------------
                     * POSICION
                     * -------------------------------------------------
                     */
                    int screenWidth =
                            client.getWindow()
                                    .getGuiScaledWidth();

                    int screenHeight =
                            client.getWindow()
                                    .getGuiScaledHeight();

                    final int sideMargin = 10;
                    final int topMargin = 10;

                    /*
                     * Espacio para no invadir la hotbar.
                     */
                    final int bottomMargin = 55;

                    int screenX;
                    int screenY;

                    HudPosition position =
                            SmartMendingClientConfig
                                    .getHudPosition();

                    switch (position) {

                        case TOP_CENTER:

                            screenX =
                                    (screenWidth
                                            - scaledHudWidth)
                                            / 2;

                            screenY =
                                    topMargin;

                            break;

                        case TOP_RIGHT:

                            screenX =
                                    screenWidth
                                            - scaledHudWidth
                                            - sideMargin;

                            screenY =
                                    topMargin;

                            break;

                        case BOTTOM_LEFT:

                            screenX =
                                    sideMargin;

                            screenY =
                                    screenHeight
                                            - scaledHudHeight
                                            - bottomMargin;

                            break;

                        case BOTTOM_RIGHT:

                            screenX =
                                    screenWidth
                                            - scaledHudWidth
                                            - sideMargin;

                            screenY =
                                    screenHeight
                                            - scaledHudHeight
                                            - bottomMargin;

                            break;

                        case TOP_LEFT:
                        default:

                            screenX =
                                    sideMargin;

                            screenY =
                                    topMargin;

                            break;
                    }

                    /*
                     * -------------------------------------------------
                     * MATRIZ DEL HUD
                     * -------------------------------------------------
                     *
                     * Primero nos movemos a la posicion real.
                     * Despues escalamos.
                     *
                     * Asi x=0, y=0 representa la esquina
                     * superior izquierda del HUD.
                     */
                    Matrix3x2fStack matrixStack =
                                drawContext.pose();

                        matrixStack.pushMatrix();

                        matrixStack.translate(
                                screenX,
                                screenY
                        );

                        matrixStack.scale(
                                scale,
                                scale
                        );

                    /*
                     * -------------------------------------------------
                     * ONDAS DE REPARACION
                     * -------------------------------------------------
                     *
                     * Dos ondas convergen hacia el HUD cuando
                     * recibimos una reparacion.
                     */
                    long waveElapsed =
                            SmartMendingHudData
                                    .getTimeSinceLastUpdate();

                    final float waveDuration =
                            400.0F;

                    if (waveElapsed < waveDuration) {

                        float waveProgress =
                                waveElapsed / waveDuration;

                        /*
                         * 0 = onda alejada
                         * 1 = onda llegando al HUD
                         */
                        float waveDistance =
                                1.0F - waveProgress;

                        /*
                         * Las ondas tambien se van haciendo
                         * transparentes al acercarse.
                         */
                        int waveAlpha =
                                Math.max(
                                        0,
                                        Math.min(
                                                255,
                                                Math.round(
                                                        150.0F
                                                                * waveDistance
                                                )
                                        )
                                );

                        int waveColor =
                                (waveAlpha << 24)
                                        | 0x0055FF55;

                        /*
                         * -------------------------------------------------
                         * ONDA EXTERIOR
                         * -------------------------------------------------
                         */
                        int outerExpansion =
                                Math.round(
                                        12.0F
                                                * waveDistance
                                );

                        drawWaveRectangle(
                                drawContext,
                                -outerExpansion,
                                -outerExpansion,
                                hudWidth + outerExpansion,
                                hudHeight + outerExpansion,
                                waveColor
                        );

                        /*
                         * -------------------------------------------------
                         * ONDA INTERIOR
                         * -------------------------------------------------
                         */
                        float innerDistance =
                                Math.max(
                                        0.0F,
                                        waveDistance - 0.30F
                                );

                        int innerExpansion =
                                Math.round(
                                        8.0F
                                                * innerDistance
                                );

                        int innerAlpha =
                                Math.round(
                                        110.0F
                                                * innerDistance
                                );

                        if (innerAlpha > 0) {

                            int innerColor =
                                    (innerAlpha << 24)
                                            | 0x0055FF55;

                            drawWaveRectangle(
                                    drawContext,
                                    -innerExpansion,
                                    -innerExpansion,
                                    hudWidth + innerExpansion,
                                    hudHeight + innerExpansion,
                                    innerColor
                            );
                        }
                    }

                    /*
                     * -------------------------------------------------
                     * ICONO
                     * -------------------------------------------------
                     *
                     * DrawContext 1.20.1 se encarga internamente
                     * del modelo, iluminacion y transformacion GUI.
                     */
                    drawContext.item(
                            stack,
                            0,
                            0
                    );

                    /*
                     * -------------------------------------------------
                     * PORCENTAJE
                     * -------------------------------------------------
                     */
                    int textY =
                            (iconSize - textHeight)
                                    / 2;

                    int textColor =
                            (alpha << 24)
                                    | 0x00FFFFFF;

                    drawContext.text(
                            client.font,
                            text,
                            iconSize + spacing,
                            textY,
                            textColor
                    );

                    /*
                     * -------------------------------------------------
                     * BARRA DE DURABILIDAD
                     * -------------------------------------------------
                     */
                    int barWidth =
                            hudWidth;

                    int barY =
                            iconSize + 2;

                    int filledWidth =
                            (int) Math.round(
                                    barWidth
                                            * (durability / 100.0)
                            );

                    int backgroundAlpha =
                            Math.round(
                                    170.0F * fade
                            );

                    int backgroundColor =
                            (backgroundAlpha << 24)
                                    | 0x000000;

                    /*
                     * Fondo.
                     */
                    drawContext.fill(
                            0,
                            barY,
                            barWidth,
                            barY + barHeight,
                            backgroundColor
                    );

                    int barColor =
                            (alpha << 24)
                                    | 0x0055FF55;

                    /*
                     * Durabilidad reparada.
                     */
                    if (filledWidth > 0) {

                        drawContext.fill(
                                0,
                                barY,
                                filledWidth,
                                barY + barHeight,
                                barColor
                        );
                    }

                    matrixStack.popMatrix();
                }
        );

        /*
         * -------------------------------------------------
         * ABRIR CONFIGURACION CON F7
         * -------------------------------------------------
         */
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {

                    while (openConfigKey.consumeClick()) {

                        if (client.gui.screen() == null) {

                            client.setScreenAndShow(
                                    new SmartMendingConfigScreen(
                                            null
                                    )
                            );
                        }
                    }
                }
        );
    }

    /*
     * Dibuja un contorno rectangular de 1 pixel.
     *
     * Se utiliza para las ondas visuales
     * de reparacion del HUD.
     */
        private static void drawWaveRectangle(
                GuiGraphicsExtractor drawContext,
                int left,
                int top,
                int right,
                int bottom,
                int color) {

        // Arriba
        drawContext.fill(
                left,
                top,
                right,
                top + 1,
                color
        );

        // Abajo
        drawContext.fill(
                left,
                bottom - 1,
                right,
                bottom,
                color
        );

        // Izquierda
        drawContext.fill(
                left,
                top,
                left + 1,
                bottom,
                color
        );

        // Derecha
        drawContext.fill(
                right - 1,
                top,
                right,
                bottom,
                color
        );
        }
}