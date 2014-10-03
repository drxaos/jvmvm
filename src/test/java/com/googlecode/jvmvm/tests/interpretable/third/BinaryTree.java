package com.googlecode.jvmvm.tests.interpretable.third;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: oe
 * Date: 10/17/12
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class BinaryTree extends Object
{
    public int value;
    public BinaryTree left;
    public BinaryTree right;

    public BinaryTree(int _value)
    {
        this(_value, null, null);
    }

    public BinaryTree(int _value, BinaryTree _left, BinaryTree _right)
    {
        value = _value;
        left = _left;
        right = _right;
    }

    public void add(int newValue)
    {
        if (this.value > newValue)
        {
            if (this.left == null)
            {
                 this.left = new BinaryTree(newValue, null, null);
            }
            else
            {
                this.left.add(newValue);
            }
        }
        else
        {
            if (this.right == null)
            {
                this.right = new BinaryTree(newValue, null, null);
            }
            else
            {
                this.right.add(newValue);
            }
        }
    }

    @Override public String toString() {
        return "Value:" + this.value + "\nLeft:" + this.left + " Right:" + this.right + "\n";
    }

    public int size()
    {
        int leftSize = (this.left == null) ? 0 : this.left.size();
        int rightSize = (this.right == null) ? 0 : this.right.size();
        return 1 + leftSize + rightSize;
    }

    public int height()
    {
        int leftHeight = (this.left == null) ? 0 : this.left.height();
        int rightHeight = (this.right == null) ? 0 : this.right.height();
        return 1 + Math.max(leftHeight, rightHeight);
    }

    public int emptyChilds()
    {
        int leftEmptyChilds = (this.left == null) ? 1 : this.left.height();
        int rightEmptyChilds = (this.right == null) ? 1 : this.right.height();
        return leftEmptyChilds + rightEmptyChilds;
    }

    protected void rebalance()
    {
        int leftHeight = (this.left == null) ? 0 : this.left.height();
        int rightHeight = (this.right == null) ? 0 : this.right.height();
        int heightDiff = leftHeight - rightHeight;
        if (heightDiff > 1 || heightDiff < -1)
        {
            if (heightDiff > 1)
            {
                BinaryTree temp = new BinaryTree(this.value);
                temp.right = this.right;
                temp.left = mergeBinaryTrees(temp.left, this.left.right);
                this.value = this.left.value;
                this.left = this.left.left;
                this.right = temp;
            }
            else if (heightDiff < -1)
            {
                BinaryTree temp = new BinaryTree(this.value);
                temp.left = this.left;
                temp.right = mergeBinaryTrees(temp.right, this.right.left);
                this.value = this.right.value;
                this.right = this.right.right;
                this.left = temp;
            }
            if (this.left != null)
            {
                this.left.rebalance();
            }
            if (this.right != null)
            {
                this.right.rebalance();
            }
        }
    }

    public static BinaryTree mergeBinaryTrees(BinaryTree rootTree, BinaryTree anotherTree)
    {
        if (anotherTree == null)
        {
            return rootTree;
        }
        if (rootTree == null)
        {
            return anotherTree;
        }
        rootTree.add(anotherTree.value);
        rootTree.mergeBinaryTrees(rootTree, anotherTree.left);
        rootTree.mergeBinaryTrees(rootTree, anotherTree.right);
        return rootTree;
    }

    /*
        A binary tree is a binary search tree, if and only if:
        - The left subtree of a node contains only nodes with keys less than the node's key.
        - The right subtree of a node contains only nodes with keys greater than the node's key.
        - Both the left and right subtrees must also be binary search trees.
        - There must be no duplicate nodes.
    */
    public boolean isBinarySearchTree()
    {
        HashMap<Integer, Object> map = new HashMap<Integer, Object>();
        return !hasDuplicateItem(map) && this.isBinarySearchTreeRecursive();
    }

    private boolean isBinarySearchTreeRecursive()
    {
        if (this.left != null)
        {
            if ((this.left.value > this.value) || (this.left.max() > this.value))
            {
                return false;
            }
            if (!this.left.isBinarySearchTreeRecursive())
            {
                return false;
            }
        }
        if (this.right != null)
        {
            if ((this.right.value < this.value) || (this.right.min() < this.value))
            {
                return false;
            }
            if (!this.right.isBinarySearchTreeRecursive())
            {
                return false;
            }
        }
        return true;
    }

    public int max()
    {
        int leftMax = (this.left == null) ? Integer.MIN_VALUE : this.left.max();
        int rightMax = (this.right == null) ? Integer.MIN_VALUE : this.right.max();
        return Math.max(this.value, Math.max(leftMax, rightMax));
    }

    public int min()
    {
        int leftMin = (this.left == null) ? Integer.MAX_VALUE : this.left.min();
        int rightMin = (this.right == null) ? Integer.MAX_VALUE : this.right.min();
        return Math.min(this.value, Math.max(leftMin, rightMin));
    }

    public boolean hasDuplicateItem(HashMap<Integer, Object> map)
    {
        if (map.containsKey(this.value))
        {
            return true;
        }
        map.put(this.value, null);
        return ((this.left != null && this.left.hasDuplicateItem(map)) ||
                (this.right != null && this.right.hasDuplicateItem(map)));
    }

    public String InOrder()
    {
        StringBuilder b = new StringBuilder();
        if (this.left != null)
        {
            b.append(this.left.InOrder());
        }
        b.append(this.value).append(" ");
        if (this.right != null)
        {
            b.append(this.right.InOrder());
        }
        return b.toString();
    }

    public String PreOrder()
    {
        StringBuilder b = new StringBuilder();
        b.append(this.value).append(" ");
        if (this.left != null)
        {
            b.append(this.left.PreOrder());
        }
        if (this.right != null)
        {
            b.append(this.right.PreOrder());
        }
        return b.toString();
    }

    public String PostOrder()
    {
        StringBuilder b = new StringBuilder();
        if (this.left != null)
        {
            b.append(this.left.PostOrder());
        }
        if (this.right != null)
        {
            b.append(this.right.PostOrder());
        }
        b.append(this.value).append(" ");
        return b.toString();
    }

    public String LevelOrder()
    {
        ArrayList<BinaryTree> b = new ArrayList<BinaryTree>();
        b.add(this);
        return LevelOrder(b, this.height()).toString();
    }

    private StringBuilder LevelOrder(ArrayList<BinaryTree> n, int height)
    {
        StringBuilder b = new StringBuilder();
        ArrayList<BinaryTree> next = new ArrayList<BinaryTree>();
        for (BinaryTree t : n) {
            if (t != null) {

                b.append(t.value);
                b.append("[");
                if (t.left == null)
                {
                    b.append("_");
                }
                else
                {
                    b.append("L");
                }
                if (t.right == null)
                {
                    b.append("_");
                }
                else
                {
                    b.append("R");
                }
                b.append("] ");
                next.add(t.left);
                next.add(t.right);
            }
        }
        b.append("\n");
        if(next.size() > 0)
        {
            b.append(LevelOrder(next, height - 1));
        }
        return b;
    }

    private ArrayList<BinaryTree>findPathToValue(int v) throws Exception
    {
        ArrayList<BinaryTree> path = new ArrayList<BinaryTree>();
        BinaryTree temp = this;
        while (temp != null)
        {
            path.add(temp);
            if (v < temp.value)
            {
                temp = temp.left;
            }
            else if (v < temp.value)
            {
                temp = temp.right;
            }
            else
            {
                break;
            }
        }
        if (temp == null)
        {
            throw new Exception(v + " does not exist in the tree.");
        }
        return path;
    }

    public BinaryTree findLeastCommonAncestor(int value1, int value2) throws Exception
    {
        ArrayList<BinaryTree> node1LCA = this.findPathToValue(value1);
        ArrayList<BinaryTree> node2LCA = this.findPathToValue(value2);
        BinaryTree temp = null;
        while (true)
        {
            if (node1LCA.size() == 0 || node2LCA.size() == 0)
            {
                return temp;
            }
            BinaryTree b1 = node1LCA.remove(0);
            if (b1.value != node2LCA.remove(0).value)
            {
                return temp;
            }
            temp = b1;
        }
    }
}
