package com.googlecode.jvmvm.ui;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchDialogSearchContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.CustomLineHighlightManager;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class Editor extends JFrame implements ActionListener {

    private RSyntaxTextArea textArea;
    private RTextScrollPane textScroll;
    private JConsole playArea;
    private FindDialog findDialog;
    private ReplaceDialog replaceDialog;
    private JMenu menuSerach;
    private JPanel bottomPanel;

    private Integer keyCode;

    public Editor() {

        initSearchDialogs();

        //setJMenuBar(createMenuBar());
        JPanel cp = new JPanel(new BorderLayout());
        setContentPane(cp);

        playArea = new JConsole(50, 25);

        playArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // called multiple times during long key press
                keyCode = e.getKeyCode();
            }
        });
        cp.add(playArea, BorderLayout.WEST);

        textArea = new RSyntaxTextArea();
        try {
            Field lhmField = RTextArea.class.getDeclaredField("lineHighlightManager");
            lhmField.setAccessible(true);
            lhmField.set(textArea, new CustomLineHighlightManager(textArea));
        } catch (Exception e) {
            e.printStackTrace();
        }

        textArea.setFont(JConsole.DEFAULT_FONT);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        textArea.setTabsEmulated(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCodeFoldingEnabled(false);
        textArea.setHighlightCurrentLine(false);

        InputStream in = getClass().getResourceAsStream("/dark.xml");
        try {
            Theme theme = Theme.load(in);
            theme.apply(textArea);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        textScroll = new RTextScrollPane(textArea);
        textScroll.setPreferredSize(playArea.getPreferredSize());
        textScroll.setVisible(false);
        cp.add(textScroll, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setPreferredSize(new Dimension(100, 25));
        bottomPanel.setVisible(false);
        cp.add(bottomPanel, BorderLayout.SOUTH);

        setTitle("J-Untrusted");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        setLocationRelativeTo(null);

        setResizable(false);
    }

    public void setText(String text) {
        boolean editable = false;
        int count = 0;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.substring(i).startsWith("/*EDITABLE START*/")) {
                editable = true;
                i += "/*EDITABLE START*/".length();
            } else if (text.substring(i).startsWith("/*EDITABLE END*/")) {
                editable = false;
                i += "/*EDITABLE END*/".length();
            } else {
                b.append(text.charAt(i));
                count++;
            }
        }

        textArea.setText(b.toString());
        try {
            textArea.addLineHighlight(3, Color.RED);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        textArea.setCaretPosition(0);
    }

    public void setMap(char[][] map) {

        playArea.setBackground(Color.BLACK);
        playArea.setForeground(Color.GRAY);
        playArea.setCursorPos(49, 24);
        playArea.write('\n');

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                char c = map[y][x];
                if (c == 0) {
                    playArea.setBackground(Color.BLACK);
                    playArea.setForeground(Color.GRAY);
                    playArea.write(' ');
                } else {
                    playArea.setBackground(Color.BLACK);
                    playArea.setForeground(Color.GRAY);
                    playArea.write(c);
                }
            }
        }
    }


    private void execute(java.util.List<Action> actions) {
        if (actions != null) {
            for (Action action : actions) {
                action.execute(this);
            }
        }
        playArea.repaint();
    }


    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        {
            JMenu menu = new JMenu("API");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Toggle Focus");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Notepad");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Reset");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Execute");
            mb.add(menu);
        }
        {
            JMenu menu = new JMenu("Menu");
            mb.add(menu);
        }
        {
            menuSerach = new JMenu("Search");
            menuSerach.add(new JMenuItem(new ShowFindDialogAction()));
            menuSerach.add(new JMenuItem(new ShowReplaceDialogAction()));
            menuSerach.add(new JMenuItem(new GoToLineAction()));
            mb.add(menuSerach);
        }
        return mb;
    }


    /**
     * Creates our Find and Replace dialogs.
     */
    public void initSearchDialogs() {

        findDialog = new FindDialog(this, this);
        replaceDialog = new ReplaceDialog(this, this);

        // This ties the properties of the two dialogs together (match
        // case, regex, etc.).
        replaceDialog.setSearchContext(findDialog.getSearchContext());

    }


    /**
     * Listens for events from our search dialogs and actually does the dirty
     * work.
     */
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();
        SearchDialogSearchContext context = findDialog.getSearchContext();

        if (FindDialog.ACTION_FIND.equals(command)) {
            if (!SearchEngine.find(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        } else if (ReplaceDialog.ACTION_REPLACE.equals(command)) {
            if (!SearchEngine.replace(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        } else if (ReplaceDialog.ACTION_REPLACE_ALL.equals(command)) {
            int count = SearchEngine.replaceAll(textArea, context).getCount();
            JOptionPane.showMessageDialog(null, count
                    + " occurrences replaced.");
        }

    }

    public JConsole getConsole() {
        return playArea;
    }

    public void showCode() {
        textScroll.setVisible(true);
        bottomPanel.setVisible(true);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void hideCode() {
        textScroll.setVisible(false);
        bottomPanel.setVisible(false);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void loadCode(String code) {
        setText(code);
    }

    private class GoToLineAction extends AbstractAction {

        public GoToLineAction() {
            super("Go To Line...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            GoToDialog dialog = new GoToDialog(Editor.this);
            dialog.setMaxLineNumberAllowed(textArea.getLineCount());
            dialog.setVisible(true);
            int line = dialog.getLineNumber();
            if (line > 0) {
                try {
                    textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    ble.printStackTrace();
                }
            }
        }

    }


    private class ShowFindDialogAction extends AbstractAction {

        public ShowFindDialogAction() {
            super("Find...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            findDialog.setVisible(true);
        }

    }


    private class ShowReplaceDialogAction extends AbstractAction {

        public ShowReplaceDialogAction() {
            super("Replace...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
        }

        public void actionPerformed(ActionEvent e) {
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            replaceDialog.setVisible(true);
        }

    }


    public static void main(String[] a) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    final Editor editor = new Editor();

                    new Timer(15, new ActionListener() {
                        Game game;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            BigClip play = null;


                            editor.textArea.addVetoableChangeListener(new VetoableChangeListener() {
                                @Override
                                public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                                    if (game != null) {
                                        if (!game.validateCode(evt.getNewValue().toString())) {
                                            throw new PropertyVetoException("readonly", evt);
                                        }
                                    }
                                }
                            });

                            ((AbstractDocument)editor.textArea.getDocument()).setDocumentFilter(
                                    new DocumentFilter(){
                                        @Override
                                        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                                            super.remove(fb, offset, length);
                                        }

                                        @Override
                                        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                                            super.insertString(fb, offset, string, attr);
                                        }

                                        @Override
                                        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                                            super.replace(fb, offset, length, text, attrs);
                                        }
                                    }
                            );

                            if (game == null) {
                                try {
                                    game = new com.googlecode.jvmvm.ui.levels.intro.Game();
//                                    game = new com.googlecode.jvmvm.ui.levels.level_01.internal.Game(null);
                                    game.start();
                                    editor.playMusic(game.getMusic());
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }

                            game.setKey(editor.keyCode);
                            editor.keyCode = null;

                            game.step();
                            editor.execute(game.getActions());
                            if (game.getNextLevel() != null) {
                                game.stop();
                                editor.playArea.clear();
                                game = game.getNextLevel();
                                game.start();
                                editor.playMusic(game.getMusic());
                            }
                        }
                    }).start();

                    editor.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Player player;

    private void playMusic(String music) {
        if (player != null) {
            player.requestStop();
        }
        if (music != null) {

            try {
                player = new Player(
                        new BufferedInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream("music/" + music))
                );
                player.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
