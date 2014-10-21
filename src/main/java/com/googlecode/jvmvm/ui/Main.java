package com.googlecode.jvmvm.ui;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;

public class Main implements ActionListener {

    AbstractGame game;
    Editor editor;
    HashMap saveState = new HashMap();

    public Main() {
        editor = new Editor();
        editor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    // On exit - save game
                    saveState.put("notepad", editor.getNotepadText());
                    saveState.put("code" + game.getLevelNumber(), editor.getCodeEditor().getText());
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("./savegame.dat"));
                    out.writeObject(saveState);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        editor.setVisible(true);
        ((AbstractDocument) editor.getCodeEditor().getDocument()).setDocumentFilter(new PartlyReadOnly());

        try {
            // On start - load saved game
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("./savegame.dat"));
            saveState = (HashMap) in.readObject();
            if (saveState.containsKey("maxLevel")) {
                try {
                    String lvl = (String) saveState.get("maxLevel");
                    String code = (String) saveState.get("code" + lvl);
                    game = (AbstractGame) Class.forName("" + saveState.get("class" + lvl)).getConstructor(String.class).newInstance(code);
                    game.start();
                    editor.playMusic(game.getMusic());
                    editor.setNotepadText((String) saveState.get("notepad"));
                    editor.setText(code);
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
            editor.displaySaveGames(saveState);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Validating document filter
     */
    class PartlyReadOnly extends DocumentFilter {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (game.applyEdits(Collections.singletonList(new Code.Remove(length, offset)))) {
                super.remove(fb, offset, length);
            }
            try {
                editor.getCodeEditor().removeAllLineHighlights();
                for (Integer line : game.redLines()) {
                    editor.getCodeEditor().addLineHighlight(line, new Color(0x36, 0x1B, 0x15));
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (game.applyEdits(Collections.singletonList(new Code.Insert(string, offset)))) {
                super.insertString(fb, offset, string, attr);
            }
            try {
                editor.getCodeEditor().removeAllLineHighlights();
                for (Integer line : game.redLines()) {
                    editor.getCodeEditor().addLineHighlight(line, new Color(0x36, 0x1B, 0x15));
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            if (game.applyEdits(Collections.singletonList(new Code.Replace(string, offset, length)))) {
                super.replace(fb, offset, length, string, attrs);
            }
            try {
                editor.getCodeEditor().removeAllLineHighlights();
                for (Integer line : game.redLines()) {
                    editor.getCodeEditor().addLineHighlight(line, new Color(0x36, 0x1B, 0x15));
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // if no game - start intro
            if (game == null) {
                try {
                    game = new com.googlecode.jvmvm.ui.levels.intro.Game();
                    game.start();
                    editor.playMusic(game.getMusic());
                    saveState.put("class" + game.getLevelNumber(), game.getClass().getName());
                    saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                    saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                    if (!saveState.containsKey("maxLevel")) {
                        saveState.put("maxLevel", game.getLevelNumber());
                    }
                    editor.displaySaveGames(saveState);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            // controls
            game.setKey(editor.getKeyCode());
            editor.resetKeyCode();

            // reset
            if (editor.hasResetRequest()) {
                try {
                    game.stop();
                    game = game.getClass().getConstructor(String.class).newInstance(editor.getResetCode());
                    game.start();
                    editor.playMusic(game.getMusic());
                    editor.displaySaveGames(saveState);
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                editor.resetResetRequest();
            }

            // process step
            game.step();
            editor.execute(game.getActions());

            // next level game event
            if (game.getNextLevel() != null) {
                saveState.put("code" + game.getLevelNumber(), editor.getCodeEditor().getText());

                game.stop();
                game = game.getNextLevel();
                game.start();
                editor.playMusic(game.getMusic());

                saveState.put("class" + game.getLevelNumber(), game.getClass().getName());
                saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                saveState.put("maxLevel", game.getLevelNumber());

                editor.displaySaveGames(saveState);
            }

            // load level menu event
            if (editor.getLoadLevelRequest() != null) {
                try {
                    saveState.put("code" + game.getLevelNumber(), editor.getCodeEditor().getText());
                    game.stop();
                    game = null;
                    String lvl = editor.getLoadLevelRequest();
                    editor.resetLoadLevelRequest();
                    String code = (String) saveState.get("code" + lvl);
                    game = (AbstractGame) Class.forName("" + saveState.get("class" + lvl)).getConstructor(String.class).newInstance(code);
                    game.start();
                    editor.playMusic(game.getMusic());
                    editor.setText(code);

                    saveState.put("name" + game.getLevelNumber(), game.getLevelName());
                    saveState.put("dir" + game.getLevelNumber(), game.getLevelFolder());
                    editor.displaySaveGames(saveState);
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    new Timer(15, new Main()).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
