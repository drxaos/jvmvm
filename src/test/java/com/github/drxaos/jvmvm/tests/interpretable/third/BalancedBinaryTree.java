package com.github.drxaos.jvmvm.tests.interpretable.third;

/**
 * Created with IntelliJ IDEA.
 * User: oe
 * Date: 10/18/12
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class BalancedBinaryTree extends BinaryTree {
    public BalancedBinaryTree(int newData) {
        super(newData);
    }

    @Override
    public void add(int newData) {
        super.add(newData);
        this.rebalance();
    }


    public static String test() {
        BalancedBinaryTree tree = new BalancedBinaryTree(50);
        tree.add(44);
        tree.add(88);
        tree.add(2);
        tree.add(5);
        tree.add(234);
        tree.add(91);
        tree.add(51);
        tree.add(3);
        tree.add(100);
        tree.add(32);
        tree.add(23);
        tree.add(65);
        tree.add(56);
        return tree.toString();
    }
}
