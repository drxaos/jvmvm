package com.googlecode.jvmvm.tests.interpretable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

    public static void saveUrl(final String filename, final String urlString) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }

    public static void test2() {
        try {
            saveUrl("C:\\download\\file.zip", "http://example.com/file.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}