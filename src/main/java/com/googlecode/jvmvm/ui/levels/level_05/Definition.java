package com.googlecode.jvmvm.ui.levels.level_05;

import java.awt.*;

public abstract class Definition {
    public Color color = Color.LIGHT_GRAY;

    public String type = null;

    public char symbol = ' ';

    public void onCollision(Player player) {
    }

    public void onPickUp(Player player) {
    }

    public void onDrop() {
    }

    public void behavior(Me me) {
    }

    public boolean impassable = false;
}
