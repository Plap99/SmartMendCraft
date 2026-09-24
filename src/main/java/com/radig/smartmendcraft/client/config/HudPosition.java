package com.radig.smartmendcraft.client.config;

public enum HudPosition {

    TOP_LEFT("Superior izquierda"),
    TOP_CENTER("Centro superior"),
    TOP_RIGHT("Superior derecha"),
    BOTTOM_LEFT("Inferior izquierda"),
    BOTTOM_RIGHT("Inferior derecha");

    private final String displayName;

    HudPosition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Devuelve la siguiente posición disponible.
     * Se utiliza para recorrer las posiciones desde
     * el botón de configuración.
     */
    public HudPosition next() {

        HudPosition[] positions =
                values();

        int nextIndex =
                (ordinal() + 1)
                        % positions.length;

        return positions[nextIndex];
    }
}