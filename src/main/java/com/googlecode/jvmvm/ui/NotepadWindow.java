package com.googlecode.jvmvm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class NotepadWindow extends JDialog {
    JEditorPane editor;

    public NotepadWindow(Frame owner) {
        super(owner, "Notepad", ModalityType.MODELESS);
        editor = new JEditorPane();
        editor.setPreferredSize(new Dimension(400, 400));
        editor.setBackground(Color.BLACK);
        editor.setForeground(Color.WHITE);
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    NotepadWindow.this.setVisible(false);
                }
            }
        });
        add(editor);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

}
