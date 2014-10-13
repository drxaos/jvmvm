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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        Font codeFont = new Font(JConsole.DEFAULT_FONT.getName(), Font.PLAIN, 12);
        Font btnFont = new Font(JConsole.DEFAULT_FONT.getName(), Font.PLAIN, 10);
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

        GridBagLayout layout = new GridBagLayout();
        bottomPanel = new JPanel(layout);
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setPreferredSize(new Dimension(100, 35));
        bottomPanel.setVisible(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        {
            JLabel inventory = new JLabel("Inventory: #$%");
            inventory.setPreferredSize(new Dimension((int) playArea.getPreferredSize().getWidth(), 35));
            inventory.setFont(JConsole.DEFAULT_FONT);
            //layout.setConstraints(inventory, new GridBagConstraints(0,0,));
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[F1]API");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[F2]Toggle Focus");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[F3]Notepad");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[F4]Reset");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[F5]Execute");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JButton inventory = new JButton("[Q]Phone");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }
        {
            JLabel span = new JLabel("        ");
            bottomPanel.add(span);
        }
        {
            JButton inventory = new JButton("[Esc]Menu");
            inventory.setForeground(Color.WHITE);
            inventory.setBackground(Color.BLACK);
            inventory.setFocusable(false);
            Border line = new LineBorder(Color.BLACK);
            Border margin = new EmptyBorder(5, 3, 5, 3);
            Border compound = new CompoundBorder(line, margin);
            inventory.setBorder(compound);
            inventory.setFont(btnFont);
            bottomPanel.add(inventory);
        }

        cp.add(bottomPanel, BorderLayout.SOUTH);

        setTitle("J-Untrusted");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();

        textArea.setFont(codeFont);

        setResizable(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public Integer getKeyCode() {
        return keyCode;
    }

    public void resetKeyCode() {
        keyCode = null;
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


    public void execute(java.util.List<Action> actions) {
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

    public RSyntaxTextArea getCodeEditor() {
        return textArea;
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

    private Player player;

    public void playMusic(String music) {
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
