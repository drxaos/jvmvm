package com.googlecode.jvmvm.ui.levels.level_08;


import com.googlecode.jvmvm.ui.levels.level_08.internal.Game;

import java.awt.*;
import java.io.Serializable;

final public class Map implements Serializable {
    private Player player;
    private Game game;

    private Map(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    /**
     * Displays the given chapter name.
     */
    public void displayChapter(String title) {
        game.displayChapter(title);
    }

    /**
     * Places the player at the given coordinates.
     */
    public void placePlayer(int x, int y) {
        game.placePlayer(x, y);
    }

    /**
     * Places an object of the given type at the given coordinates.
     */
    public void placeObject(int x, int y, String type) {
        game.replaceObject(x, y, type);
    }

    /**
     * Returns the width of the map, in cells.
     */
    public int getWidth() {
        return 50;
    }

    /**
     * Returns the height of the map, in cells.
     */
    public int getHeight() {
        return 25;
    }

    /**
     * Returns the Player object.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Raises an exception if there are not at least num objects of type objectType on the map.
     */
    public void validateAtLeastXObjects(int num, String objectType) {
        game.validateAtLeastXObjects(num, objectType);
    }

    /**
     * Raises an exception if there are not exactly num objects of type objectType on the map.
     */
    public void validateExactlyXManyObjects(int num, String objectType) {
        game.validateExactlyXManyObjects(num, objectType);
    }

    /**
     * Sets the background color of the given square.
     */
    public void setSquareColor(int x, int y, Color color) {
        game.setSquareColor(x, y, color);
    }

    public void writeStatus(String text) {
        game.writeStatus(text);
    }

    void __auth(String command, String secret) {
        game.__auth(command, secret);
    }

    /**
     * Defines a new type of object with the given properties.
     * Note that type definitions created with map.defineObject only persist in the scope of the level.
     */
    public void defineObject(String type, Definition properties) {
        game.defineObject(type, properties);
    }

    /**
     * Returns the type of the object at the given coordinates (or "empty" if there is no object there).
     */
    public String getObjectTypeAt(int x, int y) {
        return game.getTypeAt(x, y);
    }
}
