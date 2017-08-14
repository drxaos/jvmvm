package com.github.drxaos.jvmvm.tests.interpretable;

public class Calculator {

    interface IntegerMath {
        int operation(int a, int b);
    }

    public int operateBinary(int a, int b, IntegerMath op) {
        return op.operation(a, b);
    }

    static int multiply(int a, int b) {
        return a * b;
    }

    static IntegerMath subtraction = (a, b) -> a - b;

    public static void main(String... args) {

        Calculator myApp = new Calculator();
        IntegerMath addition = (a, b) -> a + b;

        System.out.println("40 + 2 = " + myApp.operateBinary(40, 2, addition));
        System.out.println("20 - 10 = " + myApp.operateBinary(20, 10, subtraction));
        System.out.println("5 * 8 = " + myApp.operateBinary(5, 8, Calculator::multiply));
    }
}