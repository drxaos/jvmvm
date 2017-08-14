package com.github.drxaos.jvmvm.tests.interpretable;

public class Closure {

    interface IntegerMath {
        int operation(int a, int b);
    }

    public static void main(String... args) {

        int x = 3;

        IntegerMath addition = (a, b) -> (a + b + x);

        int res = addition.operation(1, 2);

        if (res != 6) {
            throw new RuntimeException();
        }
    }

}