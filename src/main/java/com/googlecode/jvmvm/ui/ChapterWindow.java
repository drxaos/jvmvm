package com.googlecode.jvmvm.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChapterWindow extends JDialog {
    JLabel label;

    public ChapterWindow(final Frame owner) {
        super(owner, "", ModalityType.MODELESS);
        label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        label.setPreferredSize(new Dimension(300, 150));
        label.setBackground(Color.WHITE);
        label.setForeground(Color.BLACK);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                ChapterWindow.this.setVisible(false);
                owner.transferFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                ChapterWindow.this.setVisible(false);
                owner.transferFocus();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ChapterWindow.this.setVisible(false);
                owner.transferFocus();
            }
        });
        label.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ChapterWindow.this.setVisible(false);
                owner.transferFocus();
            }
        });
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChapterWindow.this.setVisible(false);
                owner.transferFocus();
            }
        });
        add(label);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void displayChapter(String chapter) {
        label.setText("<html><p>" + chapter.replace("\n", "<br/>") + "</p></html>");
        this.setVisible(true);
    }

}
