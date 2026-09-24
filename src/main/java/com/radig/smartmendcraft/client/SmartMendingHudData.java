package com.radig.smartmendcraft.client;

import net.minecraft.item.ItemStack;

public final class SmartMendingHudData {

    private static ItemStack lastItem = ItemStack.EMPTY;
    private static int repairAmount = 0;
    private static int currentDamage = 0;
    private static int maxDamage = 0;
    private static long lastUpdateTime = 0;

    /*
     * Tiempo durante el cual permanece visible
     * la notificación.
     */
    private static final long DISPLAY_DURATION = 3000;

    /*
     * Duración del pequeño pulso que aparece
     * después de cada reparación.
     */
    private static final long PULSE_DURATION = 250;

    /*
    * Duración del desvanecimiento antes
    * de ocultar completamente el HUD.
    */
    private static final long FADE_DURATION = 350;

    private SmartMendingHudData() {
    }

    public static void update(
            ItemStack stack,
            int repaired,
            int damage,
            int max) {

        lastItem = stack.copy();
        repairAmount = repaired;
        currentDamage = damage;
        maxDamage = max;

        /*
         * Cada reparación reinicia tanto el tiempo
         * de visualización como el pulso.
         */
        lastUpdateTime = System.currentTimeMillis();
    }

    public static ItemStack getLastItem() {
        return lastItem;
    }

    public static int getRepairAmount() {
        return repairAmount;
    }

    public static int getCurrentDamage() {
        return currentDamage;
    }

    public static int getMaxDamage() {
        return maxDamage;
    }

    public static double getDurabilityPercentage() {

        if (maxDamage <= 0) {
            return 0.0;
        }

        int durability =
                maxDamage - currentDamage;

        return ((double) durability / maxDamage) * 100.0;
    }

    /*
     * Milisegundos transcurridos desde la
     * reparación más reciente.
     */
    public static long getTimeSinceLastUpdate() {

        if (lastUpdateTime <= 0) {
            return Long.MAX_VALUE;
        }

        return System.currentTimeMillis()
                - lastUpdateTime;
    }

    /*
     * Devuelve un valor entre 0.0 y 1.0.
     *
     * 1.0 = acaba de repararse.
     * 0.0 = el pulso terminó.
     */
    public static float getPulseProgress() {

        long elapsed =
                getTimeSinceLastUpdate();

        if (elapsed >= PULSE_DURATION) {
            return 0.0F;
        }

        return 1.0F
                - ((float) elapsed
                / (float) PULSE_DURATION);
    }

    /*
    * Opacidad del HUD.
    *
    * 1.0 = completamente visible.
    * 0.0 = completamente invisible.
    *
    * El HUD permanece sólido casi todo el tiempo
    * y solamente se desvanece al final.
    */
    public static float getFadeProgress() {

        long elapsed =
                getTimeSinceLastUpdate();

        long fadeStart =
                DISPLAY_DURATION - FADE_DURATION;

        if (elapsed <= fadeStart) {
            return 1.0F;
        }

        if (elapsed >= DISPLAY_DURATION) {
            return 0.0F;
        }

        float fadeElapsed =
                elapsed - fadeStart;

        return 1.0F
                - (fadeElapsed / FADE_DURATION);
    }

    public static boolean shouldDisplay() {

        return !lastItem.isEmpty()
                && getTimeSinceLastUpdate()
                < DISPLAY_DURATION;
    }
}