package com.googlecode.jvmvm.ui.levels.level_07;


import com.googlecode.jvmvm.ui.levels.level_07.internal.Game;

import java.awt.*;

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

    /**
     * Kills the player and displays the given text as the cause of death.
     */
    public void killedBy(String cause) {
        throw new RuntimeException("You have been killed by " + cause);
    }

    /**
     * Sets the function that is executed when the player uses the function phone.
     */
    public void setPhoneCallback(PhoneCallback phoneCallback) {
        game.setPhoneCallback(phoneCallback);
    }

    /**
     * Sets the color of the player.
     */
    public void setColor(Color color) {
        ((Definition) game.getDefinitionOfPlayer()).color = color;
    }

    /**
     * Returns the color of the player.
     */
    public Color getColor() {
        return ((Definition) game.getDefinitionOfPlayer()).color;
    }
}
