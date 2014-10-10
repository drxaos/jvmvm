package com.googlecode.jvmvm.ui.levels.level_01.internal;

import com.googlecode.jvmvm.ui.levels.level_01.Player;

import java.awt.*;

abstract class Definition {
    abstract public Color getColor();

    public String getType() {
        return "dynamic";
    }

    public abstract char getSymbol();

    public void onCollision(Player player) {
    }

    public void behavior(Me me) {
    }
}
