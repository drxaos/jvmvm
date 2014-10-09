package com.googlecode.jvmvm.ui.levels.level_01;

import com.googlecode.jvmvm.ui.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Map implements Serializable {
    private Player player;
    private Game game;

    public Map(Game game) {
        this.game = game;
    }

    public void displayChapter(String title) {

    }

    public void placePlayer(int x, int y) {

    }

    public void placeObject(int x, int y, String type) {

    }

    public int getWidth() {
        return 50;
    }

    public int getHeight() {
        return 25;
    }

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
