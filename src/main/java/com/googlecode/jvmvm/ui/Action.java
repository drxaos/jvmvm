package com.googlecode.jvmvm.ui;

import java.awt.*;

public abstract class Action {
    Exception point;

    protected Action() {
        point = new Exception();
    }

    abstract public void execute(Editor editor);

    public static class Nop extends Action {
        @Override
        public void execute(Editor editor) {
        }
    }

    public static class ShowApi extends Action {
        @Override
        public void execute(Editor editor) {
            editor.apiAction.actionPerformed(null);
        }
    }

    public static class LoadCode extends Action {
        String code;

        public LoadCode(String code) {
            this.code = code;
        }

        @Override
        public void execute(Editor editor) {
            editor.loadCode(code);
        }
    }

    public static class ShowCode extends Action {
        @Override
        public void execute(Editor editor) {
            editor.showCode();
        }
    }

    public static class HideCode extends Action {
        @Override
        public void execute(Editor editor) {
            editor.hideCode();
        }
    }

    public static class DisplayTitle extends Action {
        String title;

        public DisplayTitle(String title) {
            this.title = title;
        }

        @Override
        public void execute(Editor editor) {
            editor.setTitle("J-Untrusted:   " + title);
        }
    }

    public static class DisplayChapter extends Action {
        String chapter;

        public DisplayChapter(String chapter) {
            this.chapter = chapter;
        }

        @Override
        public void execute(Editor editor) {
            editor.displayChapter(chapter);
        }
    }


    public static class MoveCaret extends Action {
        int x, y;

        public MoveCaret(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void execute(Editor editor) {
            editor.getConsole().setCursorPos(x, y);
        }
    }

    public static class MoveCaretToBottomRight extends MoveCaret {
        public MoveCaretToBottomRight() {
            super(49, 24);
        }
    }

    public static class MoveCaretToBottomLeft extends MoveCaret {
        public MoveCaretToBottomLeft() {
            super(0, 24);
        }
    }

    public static class MoveCaretToTopLEft extends MoveCaret {
        public MoveCaretToTopLEft() {
            super(0, 0);
        }
    }

    public static class Clear extends Action {

        @Override
        public void execute(Editor editor) {
            editor.getConsole().clear();
        }
    }

    public static class Inventory extends Action {
        String inv;

        public Inventory(String inv) {
            this.inv = inv;
        }

        @Override
        public void execute(Editor editor) {
            editor.setInventory(inv);
        }
    }

    public static class Print extends Action {
        Color fg, bg;
        String text;

        public Print(Color fg, Color bg, String text) {
            this.fg = fg;
            this.bg = bg;
            this.text = text;
        }

        public Print(Color fg, String text) {
            this.fg = fg;
            this.bg = Color.BLACK;
            this.text = text;
        }

        public Print(String text) {
            this.text = text;
        }

        @Override
        public void execute(Editor editor) {
            if (bg != null) {
                editor.getConsole().setBackground(bg);
            }
            if (fg != null) {
                editor.getConsole().setForeground(fg);
            }
            editor.getConsole().write(text);
        }
    }

    public static class PutChar extends Action {
        Color fg, bg;
        char ch;

        public PutChar(Color fg, Color bg, char ch) {
            this.fg = fg;
            this.bg = bg;
            this.ch = ch;
        }

        public PutChar(Color fg, char ch) {
            this.fg = fg;
            this.bg = Color.BLACK;
            this.ch = ch;
        }

        public PutChar(String text) {
            this.ch = ch;
        }

        @Override
        public void execute(Editor editor) {
            if (bg != null) {
                editor.getConsole().setBackground(bg);
            }
            if (fg != null) {
                editor.getConsole().setForeground(fg);
            }
            editor.getConsole().setCycle(true);
            editor.getConsole().write(ch);
            editor.getConsole().setCycle(false);
        }
    }


}
