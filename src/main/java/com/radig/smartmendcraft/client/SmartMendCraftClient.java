package com.radig.smartmendcraft.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.radig.smartmendcraft.client.config.HudPosition;
import com.radig.smartmendcraft.client.config.SmartMendingClientConfig;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.network.SmartMendingNetworking;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;

import net.minecraft.client.render.item.ItemRenderer;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;

public class SmartMendCraftClient implements ClientModInitializer {

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {

        /*
         * Configuración visual local.
         */
        SmartMendingClientConfig.load();

        /*
         * -------------------------------------------------
         * TECLA F7
         * -------------------------------------------------
         */
        openConfigKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.smartmendcraft.open_config",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_F7,
                        "category.smartmendcraft"
                )
        );

        /*
         * -------------------------------------------------
         * ACTUALIZACIÓN DEL HUD DESDE EL SERVIDOR
         * -------------------------------------------------
         */
        ClientPlayNetworking.registerGlobalReceiver(
                SmartMendingNetworking.MENDING_UPDATE,
                (client, handler, buffer, responseSender) -> {

                    ItemStack stack =
                            buffer.readItemStack();

                    int repairAmount =
                            buffer.readInt();

                    int currentDamage =
                            buffer.readInt();

                    int maxDamage =
                            buffer.readInt();

                    client.execute(() -> {

                        SmartMendingHudData.update(
                                stack,
                                repairAmount,
                                currentDamage,
                                maxDamage
                        );
                    });
                }
        );

        /*
         * -------------------------------------------------
         * SINCRONIZACIÓN DE CONFIGURACIÓN PERSONAL
         * -------------------------------------------------
         */
        ClientPlayNetworking.registerGlobalReceiver(
                SmartMendingNetworking.CONFIG_SYNC,
                (client, handler, buffer, responseSender) -> {

                    boolean repairMainHand =
                            buffer.readBoolean();

                    boolean repairOffHand =
                            buffer.readBoolean();

                    boolean repairArmor =
                            buffer.readBoolean();

                    boolean repairInventory =
                            buffer.readBoolean();

                    int priorityCount =
                            buffer.readInt();

                    MendingTarget[] priority =
                            new MendingTarget[priorityCount];

                    for (int i = 0;
                            i < priorityCount;
                            i++) {

                        int ordinal =
                                buffer.readInt();

                        priority[i] =
                                MendingTarget.values()[ordinal];
                    }

                    int inventoryModeOrdinal =
                            buffer.readInt();

                    InventoryMendingMode inventoryMode =
                            InventoryMendingMode.values()[
                                    inventoryModeOrdinal
                            ];

                    client.execute(() -> {

                        if (client.player == null) {
                            return;
                        }

                        PlayerMendingConfig config =
                                PlayerMendingConfigs.get(
                                        client.player
                                );

                        config.setRepairMainHand(
                                repairMainHand
                        );

                        config.setRepairOffHand(
                                repairOffHand
                        );

                        config.setRepairArmor(
                                repairArmor
                        );

                        config.setRepairInventory(
                                repairInventory
                        );

                        config.setInventoryMode(
                                inventoryMode
                        );

                        config.setPriority(
                                List.of(priority)
                        );
                    });
                }
        );

        /*
         * -------------------------------------------------
         * HUD DE REPARACIÓN
         * -------------------------------------------------
         */
        HudRenderCallback.EVENT.register(
                (matrixStack, tickDelta) -> {

                    if (!SmartMendingClientConfig.isShowHud()) {
                        return;
                    }

                    if (!SmartMendingHudData.shouldDisplay()) {
                        return;
                    }

                    MinecraftClient client =
                            MinecraftClient.getInstance();

                    if (client.textRenderer == null) {
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
                    * Trabajamos internamente con el tamaño normal
                    * de Minecraft (icono de 16x16).
                    *
                    * Después escalamos todo el conjunto.
                    */
                    final int iconSize = 16;
                    final int spacing = 3;
                    final int barHeight = 2;

                    int textWidth =
                            client.textRenderer
                                    .getWidth(text);

                    int textHeight =
                            client.textRenderer
                                    .fontHeight;

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
                    *
                    * Tamaño normal: 82%
                    *
                    * Al recibir reparación:
                    * pequeño pulso hasta aproximadamente 90%.
                    */

                    final float baseScale =
                            0.50F;

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
                    * Convertimos eso en un pulso que:
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
                    *
                    * Durante los últimos 350 ms el HUD pasa
                    * suavemente de opacidad completa a invisible.
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
                                            Math.round(255.0F * fade)
                                    )
                            );
                                        
                    /*
                    * Tamaño REAL que ocupará en pantalla
                    * después de aplicar la escala.
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
                    * POSICIÓN
                    * -------------------------------------------------
                    */

                    int screenWidth =
                            client.getWindow()
                                    .getScaledWidth();

                    int screenHeight =
                            client.getWindow()
                                    .getScaledHeight();

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
                    * Primero nos movemos a la posición real.
                    * Después escalamos.
                    *
                    * Así x=0, y=0 representa la esquina
                    * superior izquierda del HUD.
                    */

                    matrixStack.push();

                    matrixStack.translate(
                            screenX,
                            screenY,
                            0
                    );

                    matrixStack.scale(
                            scale,
                            scale,
                            1.0F
                    );

                    /*
                    * -------------------------------------------------
                    * ONDAS DE REPARACIÓN
                    * -------------------------------------------------
                    *
                    * Dos ondas convergen hacia el HUD cuando
                    * recibimos una reparación.
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
                        * Las ondas también se van haciendo
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
                                matrixStack,
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
                        *
                        * Va un poco adelantada respecto
                        * a la onda exterior.
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
                                    matrixStack,
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
                    */

                    ItemRenderer itemRenderer =
                            client.getItemRenderer();

                    BakedModel model =
                            itemRenderer.getHeldItemModel(
                                    stack,
                                    null,
                                    null,
                                    0
                            );

                    /*
                    * ItemRenderer 1.17.1 utiliza la matriz
                    * ModelView de RenderSystem para los iconos GUI.
                    *
                    * La posicion screenX/screenY ya está calculada
                    * utilizando el tamaño escalado del HUD.
                    */
                    MatrixStack itemMatrix =
                            RenderSystem.getModelViewStack();

                    itemMatrix.push();

                    /*
                    * Nos colocamos en el centro del icono.
                    *
                    * El icono original mide 16x16.
                    * Después aplicamos nuestra escala.
                    */
                    itemMatrix.translate(
                            screenX + (8.0F * scale),
                            screenY + (8.0F * scale),
                            100.0F + itemRenderer.zOffset
                    );

                    /*
                    * Equivalente a la transformación GUI de Minecraft,
                    * pero multiplicada por nuestra escala.
                    */
                    itemMatrix.scale(
                            scale,
                            -scale,
                            scale
                    );

                    itemMatrix.scale(
                            16.0F,
                            16.0F,
                            16.0F
                    );

                    RenderSystem.applyModelViewMatrix();

                    MatrixStack itemRenderMatrix =
                            new MatrixStack();

                    VertexConsumerProvider.Immediate vertexConsumers =
                            client.getBufferBuilders()
                                    .getEntityVertexConsumers();

                    boolean disableLighting =
                            !model.isSideLit();

                    if (disableLighting) {
                        DiffuseLighting.disableGuiDepthLighting();
                    }

                    /*
                    * Aplicamos el fade también al modelo del objeto.
                    */
                    RenderSystem.setShaderColor(
                            1.0F,
                            1.0F,
                            1.0F,
                            fade
                    );

                    itemRenderer.renderItem(
                            stack,
                            ModelTransformation.Mode.GUI,
                            false,
                            itemRenderMatrix,
                            vertexConsumers,
                            LightmapTextureManager.MAX_LIGHT_COORDINATE,
                            OverlayTexture.DEFAULT_UV,
                            model
                    );

                    vertexConsumers.draw();

                    /*
                    * Restauramos inmediatamente el color global
                    * para no afectar al resto del HUD o Minecraft.
                    */
                    RenderSystem.setShaderColor(
                            1.0F,
                            1.0F,
                            1.0F,
                            1.0F
                    );

                    RenderSystem.enableDepthTest();

                    if (disableLighting) {
                        DiffuseLighting.enableGuiDepthLighting();
                    }

                    itemMatrix.pop();

                    RenderSystem.applyModelViewMatrix();

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

                    client.textRenderer.drawWithShadow(
                            matrixStack,
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
                    net.minecraft.client.gui.DrawableHelper.fill(
                            matrixStack,
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

                        net.minecraft.client.gui.DrawableHelper.fill(
                                matrixStack,
                                0,
                                barY,
                                filledWidth,
                                barY + barHeight,
                                barColor
                        );
                    }

                    matrixStack.pop();
                }
        );

        /*
         * -------------------------------------------------
         * ABRIR CONFIGURACIÓN CON F7
         * -------------------------------------------------
         */
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {

                    while (openConfigKey.wasPressed()) {

                        if (client.currentScreen == null) {

                            client.setScreen(
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
    * Dibuja un contorno rectangular de 1 píxel.
    *
    * Se utiliza para las ondas visuales
    * de reparación del HUD.
    */
    private static void drawWaveRectangle(
            MatrixStack matrixStack,
            int left,
            int top,
            int right,
            int bottom,
            int color) {

        /*
        * Arriba
        */
        net.minecraft.client.gui.DrawableHelper.fill(
                matrixStack,
                left,
                top,
                right,
                top + 1,
                color
        );

        /*
        * Abajo
        */
        net.minecraft.client.gui.DrawableHelper.fill(
                matrixStack,
                left,
                bottom - 1,
                right,
                bottom,
                color
        );

        /*
        * Izquierda
        */
        net.minecraft.client.gui.DrawableHelper.fill(
                matrixStack,
                left,
                top,
                left + 1,
                bottom,
                color
        );

        /*
        * Derecha
        */
        net.minecraft.client.gui.DrawableHelper.fill(
                matrixStack,
                right - 1,
                top,
                right,
                bottom,
                color
        );
    }
}