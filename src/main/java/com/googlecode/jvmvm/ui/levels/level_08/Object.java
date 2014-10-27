package com.googlecode.jvmvm.ui.levels.level_08;

import com.googlecode.jvmvm.ui.common.Point;
import com.googlecode.jvmvm.ui.levels.level_08.internal.Game;

import java.awt.*;

public class Object {

    private Game game;
    private String id;

    public Object(Game game, String id) {
        this.game = game;
        this.id = id;
    }

    /**
     * (For dynamic objects only.) Returns the x and y coordinates
     * of the nearest object of the given type to this object.
     */
    public Point findNearest(String type) {
        return game.findNearest(id, type);
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
        Definition d = (Definition) game.getNearObjType(id, direction);
        if (d.impassable(null, getType(), (Object) game.createObject(id))) {
            return false;
        }
        return true;
    }

    /**
     * (For dynamic objects only.) Moves the object one square in the given direction,
     * which can be "left", "right", "up", or "down".
     * An object can only move once per turn.
     */
    public void move(String direction) {
        game.move(id, direction);
    }

    /**
     * Can be "item", "dynamic", or none.
     * If "dynamic", then this object can move on turns that run each time that the player moves.
     * If "item", then this object can be picked up.
     */
    public String getType() {
        return game.getObjType(id);
    }

    /**
     * The color of the object's symbol on the map.
     */
    public Color getColor() {
        return ((Definition) game.getDefinitionOfObject(id)).color;
    }
}
