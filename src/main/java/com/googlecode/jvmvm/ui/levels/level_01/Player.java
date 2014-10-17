package com.googlecode.jvmvm.ui.levels.level_01;

import com.googlecode.jvmvm.ui.levels.level_01.internal.Game;

final public class Player {
    private Game game;

    private Player(Game game) {
        this.game = game;
    }

    /**
     * Returns true if and only if the player has the given item.
     */
    public boolean hasItem(String type) {
        return game.hasItem(type);
    }
}
