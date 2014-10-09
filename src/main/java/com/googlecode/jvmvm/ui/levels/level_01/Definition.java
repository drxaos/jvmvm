package com.googlecode.jvmvm.ui.levels.level_01;

import java.awt.*;

abstract public class Definition {
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
