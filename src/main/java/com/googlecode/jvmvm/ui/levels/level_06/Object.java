package com.googlecode.jvmvm.ui.levels.level_06;

import com.googlecode.jvmvm.ui.levels.level_06.internal.Game;

final public class Object {

    private Game game;
    private String id;

    public Object(Game game,String id) {
        this.game = game;
    }

    /**
     * (For dynamic objects only.) Returns the x and y coordinates
     * of the nearest object of the given type to this object.
     */
    public Point findNearest(String type) {
        return null;
    }

    /**
     * (For dynamic objects only.) Returns the x-coordinate of the object.
     */
    public int getX() {
        return game.getObjX(id);
    }

    /**
     * (For dynamic objects only.) Returns the y-coordinate of the object.
     */
    public int getY() {
        return game.getObjY(id);
    }

    /**
     * (For dynamic objects only.) Returns true if (and only if) the object is able to move
     * one square in the given direction, which can be "left", "right", "up", or "down".
     */
    public boolean canMove(String direction) {
        // TODO
        return false;
    }

    public void move(String direction) {
        // TODO
    }
}
