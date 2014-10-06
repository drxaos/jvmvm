package com.googlecode.jvmvm.tests.interpretable;

import java.io.File;

public class SystemExamples {

    public static String test() {
        return x();
    }

    public static String x() {
        String x = "qwerty";
        x += System.currentTimeMillis();
        System.out.println("hello out!");
        System.out.println(x);
        System.err.println("hello err!");
        System.err.println(x);
        return x;
    }

    public static String test1() {
        String res = "";
        File file = new File("file1.bin");
        res += (file.delete() ? "deleted;" : "not deleted;");
        res += (file instanceof File ? "type ok;" : "type fail;");
        res += (file.getName().equals("file1.bin") ? "name ok;" : "name fail;");
        res += (file.exists() ? "file exists;" : "no file;");
        return res;
    }
}
