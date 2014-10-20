package com.googlecode.jvmvm.ui.levels.level_06;

import java.awt.*;

public abstract class Definition {
    public Color color = Color.LIGHT_GRAY;

    /**
     * Can be "item", "dynamic", or none. If "dynamic",
     * then this object can move on turns that run each time that the player moves.
     * If "item", then this object can be picked up.
     */
    public String type = null;

    public char symbol = ' ';

    public void onCollision(Player player) {
    }

    public void onPickUp(Player player) {
    }

    public void onDrop() {
    }

    /**
     * (For dynamic objects only.) The function that is executed each time it is this object's turn.
     */
    public void behavior(Object me) {
    }

    public boolean impassable(Player player, Object object) {
        return false;
    }
}
