package com.googlecode.jvmvm.tests.interpretable;

import java.io.File;

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

class FileStub {
    String name;

    FileStub(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean delete() {
        return true;
    }

    public boolean exists() {
        return false;
    }
}