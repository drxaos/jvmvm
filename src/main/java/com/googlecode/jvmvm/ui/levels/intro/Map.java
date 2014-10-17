package com.googlecode.jvmvm.ui.levels.intro;

import com.googlecode.jvmvm.ui.AbstractGame;

import java.io.Serializable;

public class Map implements Serializable {
    private AbstractGame game;

    public Map(AbstractGame game) {
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
