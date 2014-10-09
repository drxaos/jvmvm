package com.googlecode.jvmvm.ui;

import com.googlecode.jvmvm.loader.Project;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {
    protected List<Action> actions = new ArrayList<Action>();
    protected Project levelVm;



    public Game(String level, String code) {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.GREEN, "\n> " + code));
    }

    public void step() {

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
        actions.add(new Action.Print(str));
    }

    public void pushLine() {
        actions.add(new Action.MoveCaretToBottomLeft());
        actions.add(new Action.Print(Color.LIGHT_GRAY, "\n"));
    }

    public void displayChapter(String title) {
        actions.add(new Action.DisplayTitle(title));
    }


}
