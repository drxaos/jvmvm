package com.googlecode.jvmvm.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class MenuWindow extends JDialog {
    JTree tree;
    DefaultTreeModel model;
    DefaultMutableTreeNode root;

    public MenuWindow(final Frame owner) {
        super(owner, "Main Menu", ModalityType.APPLICATION_MODAL);

        final Icon rootIcon = new ImageIcon(this.getClass().getResource("/img/root.png"));
        final Icon dirIcon = new ImageIcon(this.getClass().getResource("/img/dir.png"));
        final Icon rootClosedIcon = new ImageIcon(this.getClass().getResource("/img/root-c.png"));
        final Icon dirClosedIcon = new ImageIcon(this.getClass().getResource("/img/dir-c.gif"));
        final Icon fileIcon = new ImageIcon(this.getClass().getResource("/img/class.png"));

        root = new DefaultMutableTreeNode("Project \"J-Untrusted\"", true);
        tree = new JTree(model = new DefaultTreeModel(root));
        tree.setBorder(new EmptyBorder(10, 10, 10, 10));
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
                if (tree.getModel().getRoot().equals(nodo)) {
                    if (tree.isExpanded(row)) {
                        setIcon(rootIcon);
                    } else {
                        setIcon(rootClosedIcon);
                    }
                } else if (nodo.getChildCount() > 0) {
                    if (tree.isExpanded(row)) {
                        setIcon(dirIcon);
                    } else {
                        setIcon(dirClosedIcon);
                    }
                } else {
                    setIcon(fileIcon);
                }
                return this;
            }
        });
        tree.setPreferredSize(new Dimension(300, 500));
        tree.setFocusable(false);
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    MenuWindow.this.setVisible(false);
                    owner.transferFocus();
                }
            }
        });
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 2 && selPath != null) {
                        Object userObject = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject();
                        if (userObject instanceof Level) {
                            String lvl = ((Level) userObject).getLvl();
                            ((Editor) owner).setLoadLevelRequest(lvl);
                            MenuWindow.this.setVisible(false);
                            owner.transferFocus();
                        }
                    }
                }
            }
        };
        tree.addMouseListener(ml);
        add(tree);
        pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationRelativeTo(null);
            }
        });
    }

    public void updateLevels(HashMap saveState) {
        root.removeAllChildren();

        Map<String, DefaultMutableTreeNode> nodes = new HashMap<String, DefaultMutableTreeNode>();

        for (Object k : saveState.keySet()) {
            if (k.toString().startsWith("name")) {
                String lvl = k.toString().substring(4);
                String name = saveState.get("name" + lvl).toString();
                String folder = saveState.get("dir" + lvl).toString();

                DefaultMutableTreeNode dirNode = nodes.get(folder);
                if (dirNode == null) {
                    dirNode = new DefaultMutableTreeNode(folder, true);
                    nodes.put(folder, dirNode);
                    root.add(dirNode);
                }
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode();
                fileNode.setUserObject(new Level(name, lvl));
                dirNode.add(fileNode);
            }
        }

        sort(root);
        model.reload(root);
        expandAllNodes(tree, 0, tree.getRowCount());

    }

    private class Level {
        String name, lvl;

        private Level(String name, String lvl) {
            this.name = name;
            this.lvl = lvl;
        }

        public String getLvl() {
            return lvl;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public DefaultMutableTreeNode sort(DefaultMutableTreeNode node) {

        //sort alphabetically
        for (int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            String nt = child.getUserObject().toString();

            for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
                String np = prevNode.getUserObject().toString();
                if (nt.compareToIgnoreCase(np) > 0) {
                    node.insert(child, j);
                    node.insert(prevNode, i);
                }
            }
            if (child.getChildCount() > 0) {
                sort(child);
            }
        }

        //put folders first - normal on Windows and some flavors of Linux but not on Mac OS X.
        for (int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
                if (!prevNode.isLeaf() && child.isLeaf()) {
                    node.insert(child, j);
                    node.insert(prevNode, i);
                }
            }
        }

        return node;

    }
}
