package com.googlecode.jvmvm.ui.levels.level_04;


import com.googlecode.jvmvm.ui.levels.level_04.internal.Game;

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

    /**
     * Returns true if and only if the player is at the given location.
     */
    public boolean atLocation(int x, int y) {
        return game.isPlayerAtLocation(x, y);
    }
}
