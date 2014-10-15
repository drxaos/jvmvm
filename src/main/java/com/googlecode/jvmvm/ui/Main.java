package com.googlecode.jvmvm.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Main implements ActionListener {

    Game game;
    Editor editor;
    HashMap saveState = new HashMap();

    public Main() {
        editor = new Editor();
        editor.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
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
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("./savegame.dat"));
            saveState = (HashMap) in.readObject();
            if (saveState.containsKey("maxLevel")) {
                try {
                    String lvl = (String) saveState.get("maxLevel");
                    String code = (String) saveState.get("code" + lvl);
                    game = (Game) Class.forName("" + saveState.get("class" + lvl)).getConstructor(String.class).newInstance(code);
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
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    class PartlyReadOnly extends DocumentFilter {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            Document document = fb.getDocument();
            String text = document.getText(0, document.getLength());
            String edit = text.substring(0, offset) + text.substring(offset + length, text.length());
            if (game.validateCode(edit)) {
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
            Document document = fb.getDocument();
            String text = document.getText(0, document.getLength());
            String edit = new StringBuilder(text).insert(offset, string).toString();
            if (game.validateCode(edit)) {
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
            Document document = fb.getDocument();
            String text = document.getText(0, document.getLength());
            String edit = text.substring(0, offset) + text.substring(offset + length, text.length());
            edit = new StringBuilder(edit).insert(offset, string).toString();
            if (game.validateCode(edit)) {
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
        editor.getCodeEditor().addVetoableChangeListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                if (game != null) {
                    if (!game.validateCode(evt.getNewValue().toString())) {
                        throw new PropertyVetoException("readonly", evt);
                    }
                }
            }
        });

        if (game == null) {
            try {
                game = new com.googlecode.jvmvm.ui.levels.intro.Game();
                game.start();
                editor.playMusic(game.getMusic());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        game.setKey(editor.getKeyCode());
        editor.resetKeyCode();

        if (editor.hasResetRequest()) {
            try {
                game.stop();
                game = game.getClass().getConstructor(String.class).newInstance(editor.getResetCode());
                game.start();
                editor.playMusic(game.getMusic());
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

        game.step();
        editor.execute(game.getActions());
        if (game.getNextLevel() != null) {
            saveState.put("code" + game.getLevelNumber(), editor.getCodeEditor().getText());

            game.stop();
            game = game.getNextLevel();
            game.start();
            editor.playMusic(game.getMusic());

            saveState.put("class" + game.getLevelNumber(), game.getClass().getName());
            saveState.put("maxLevel", game.getLevelNumber());
        }
    }


    public static void main(String[] a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    new Timer(15, new Main()).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
