package com.googlecode.jvmvm.ui.levels.intro;

import com.googlecode.jvmvm.ui.Game;

import java.io.Serializable;

public class Map implements Serializable {
    private Game game;

    public Map(Game game) {
        this.game = game;
    }

    public void displayChapter(String title) {
        game.displayChapter(title);
    }

    public void draw(int x, String text) {
        game.pushLine();
        game.drawText(x, 24, text);
    }
}
