package com.googlecode.jvmvm.ui;

import com.googlecode.jvmvm.loader.Project;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Game {
    protected List<Action> actions = new ArrayList<Action>();
    protected Project levelVm;
    protected Integer key;
    protected Game loadLevel;

    public Game(String level, String code) {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.GREEN, "\n> " + code));
    }

    public List<Action> getActions() {
        List<Action> res = new ArrayList<Action>();
        while (actions.size() > 0) {
            Action action = actions.remove(0);
            if (action instanceof Action.Nop) {
                break;
            } else {
                res.add(action);
            }
        }
        return res;
    }

    public void drawText(int x, int y, String str) {
        if (y >= 0 && y < 25 && x < 50) {
            while (x < 0 && str.length() > 0) {
                str = str.substring(1);
                x++;
            }
            if (str.length() + x > 50) {
                str.substring(0, 50 - x);
            }
        }
        actions.add(new Action.MoveCaret(x, y));
        actions.add(new Action.Print(Color.LIGHT_GRAY, str));
    }

    public void pushLine() {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.LIGHT_GRAY, "\n"));
    }

    public void displayChapter(String title) {
        actions.add(new Action.DisplayTitle(title));
    }


    public void setKey(Integer key) {
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    public Game getNextLevel() {
        return loadLevel;
    }

    public void load(Game game) {
        loadLevel = game;
    }

    public abstract void start();

    public abstract void step();

    public abstract void stop();

    public abstract String getMusic();

    public abstract boolean validateCode(String code);

    public abstract List<Integer> redLines();

    public abstract String getLevelNumber();

    public abstract Object getLevelName();
    public abstract Object getLevelFolder();
}
