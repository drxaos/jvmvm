package com.googlecode.jvmvm.tests.interpretable;

public class Parity {
    public static final int N = 10000;

    public static void main(String... args) {
        boolean even = isEven(N);
        boolean odd = isOdd(N);
        System.out.printf("isEven(%d) = %b\n", N, even);
        System.out.printf("isOdd(%d) = %b\n", N, odd);

        even = isEven(N + 1);
        odd = isOdd(N + 1);
        System.out.printf("isEven(%d) = %b\n", N + 1, even);
        System.out.printf("isOdd(%d) = %b\n", N + 1, odd);
    }

    public static boolean isEven(int n) {
        if (n == 0) return true;
        return isOdd(n - 1);
    }

    public static boolean isOdd(int n) {
        if (n == 0) return false;
        return isEven(n - 1);
    }
}
