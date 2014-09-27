package com.googlecode.jvmvm.tests.interpretable;

public class NestedA {

    NestedB n1;

    private class NestedB {
        private String text;
        private int number;
    }

    public void test1() {
        NestedB n = new NestedB();
        n.text = "asd";
        n1 = n;
    }

    public String test2() {
        return new NestedB().toString();
    }
}
