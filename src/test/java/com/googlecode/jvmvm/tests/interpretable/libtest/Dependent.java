package com.googlecode.jvmvm.tests.interpretable.libtest;

public class Dependent {

    public static String test() {
        Dependency d = new Dependency();
        d.addWord("Hello");
        d.addWord("World");
        d.addWord("!");
        return "Dependent " + d.join();
    }

}
