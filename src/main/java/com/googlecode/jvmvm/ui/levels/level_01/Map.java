package com.googlecode.jvmvm.ui.levels.level_01;

import com.googlecode.jvmvm.ui.levels.level_01.internal.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Map implements Serializable {
    private Player player;
    private Game game;

    public Map(Game game) {
        this.game = game;
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
        game.placeObject(x, y, type);
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

    public void writeStatus(String text) {
        List<String> strings = new ArrayList<String>();
        strings.add(text);

        if (text.length() > getWidth()) {
            // split into two lines
            int minCutoff = getWidth() - 10;
            int cutoff = minCutoff + text.substring(minCutoff).indexOf(" ");
            strings.clear();
            strings.add(text.substring(0, cutoff));
            strings.add(text.substring(cutoff + 1));
        }

        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            int x = (int) Math.floor((getWidth() - str.length()) / 2);
            int y = getHeight() + i - strings.size() - 1;
            game.drawText(x, y, str);
        }
    }
}
