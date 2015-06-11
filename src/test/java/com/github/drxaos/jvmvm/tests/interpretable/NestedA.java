package com.github.drxaos.jvmvm.tests.interpretable;


public class NestedA {

    NestedB n1;

    class X {
        X() {
            NestedA.this.toString();
        }

        public String toString() {
            return "X";
        }
    }

    private class NestedB extends X {
        private String text;
        private int number;

        private NestedB() {
            NestedA.this.hashCode();
        }

        public String toString() {
            return super.toString() + "B" + NestedA.this.toString();
        }
    }

    public void test1() {
        NestedB n = new NestedB();
        n.text = "asd";
        n1 = n;
    }

    public String test2() {
        return new NestedB().toString();
    }

    public static String test3() {
        return new NestedA().test2();
    }

    public String toString() {
        return "A";
    }
}
