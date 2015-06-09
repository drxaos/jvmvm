package com.googlecode.jvmvm.ui.levels.level_09;

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

    public boolean impassable = false;

    public boolean transport = false;

    /**
     * The function that is executed when this object touches the player.
     */
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

    /**
     * (For non-dynamic objects only.) The function that determines whether or not the player (or object type) can pass through this object.
     */
    public boolean impassable(Player player, String type, Object me) {
        return impassable;
    }
}
