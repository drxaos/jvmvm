package com.github.drxaos.jvmvm.tests.interpretable;

public class Closure {

    interface Op {
        int eval(int a, int b);
    }

    static int y = 4;

    public static void main(String... args) {

        int x = 3;

        Op addition = (a, b) -> (a + b + x + y);

        int res = addition.eval(1, 2);

        if (res != 10) {
            throw new RuntimeException();
        }
    }

}