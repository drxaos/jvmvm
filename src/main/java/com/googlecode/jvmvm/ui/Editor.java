package com.googlecode.jvmvm.ui;

import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchDialogSearchContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

public class Editor extends JFrame implements ActionListener {

    private RSyntaxTextArea textArea;
    private RTextScrollPane textScroll;
    private JConsole playArea;
    private FindDialog findDialog;
    private ReplaceDialog replaceDialog;
    private JMenu menuSerach;

    public Editor() {

        initSearchDialogs();

        setJMenuBar(createMenuBar());
        JPanel cp = new JPanel(new BorderLayout());
        setContentPane(cp);

        playArea = new JConsole(50, 25);
        playArea.setFocusable(true);
        playArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                playArea.grabFocus();
            }
        });
        playArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'a') {

                }
            }
        });
        cp.add(playArea, BorderLayout.WEST);

        textArea = new RSyntaxTextArea();
        textArea.setFont(JConsole.DEFAULT_FONT);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAnimateBracketMatching(true);
        textArea.setTabsEmulated(true);
        textArea.setAntiAliasingEnabled(true);
        textScroll = new RTextScrollPane(textArea);
        textScroll.setPreferredSize(playArea.getPreferredSize());
        textScroll.setVisible(false);
        menuSerach.setEnabled(false);
        cp.add(textScroll, BorderLayout.CENTER);

        textArea.addVetoableChangeListener(new VetoableChangeListener() {
            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

            }
        });

        setTitle("J-Untrusted");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        setLocationRelativeTo(null);

        setResizable(false);
    }


    public void setText(String text) {
        textArea.setText(text);
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
        menuSerach.setEnabled(true);
        pack();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void hideCode() {
        textScroll.setVisible(false);
        menuSerach.setEnabled(false);
        pack();
        EventQueue.invokeLater(new Runnable() {
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

                    final Game game = new com.googlecode.jvmvm.ui.levels.intro.Game();
                    new Timer(15, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            game.step();
                            editor.execute(game.getActions());
                        }
                    }).start();

                    editor.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
