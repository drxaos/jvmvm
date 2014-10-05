package com.googlecode.jvmvm.tests.interpretable;

public class SystemExamples {

    public static String test() {
        return x();
    }

    public static String x() {
        String x = "qwerty";
        x += "12345";
        System.out.println(x);
        System.err.println(x);
        return x;
    }

}
