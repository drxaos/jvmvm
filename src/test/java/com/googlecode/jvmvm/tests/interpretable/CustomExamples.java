package com.googlecode.jvmvm.tests.interpretable;

import java.io.UnsupportedEncodingException;

public class CustomExamples {

    public static String test() {

        String res = "";

        String[] split = "1,2,3".split(",");
        for (String s : split) {
            try {
                byte[] bytes = s.getBytes("UTF-8");
                byte[] bytes1 = bytes.clone();
                for (byte b : bytes1) {
                    double d = new Byte(b).doubleValue();
                    int i = new Double(d).hashCode();
                    String s1 = Integer.toString(i, 20);
                    char[] chars = s1.toCharArray();
                    for (char aChar : chars) {
                        boolean alphabetic = Character.isAlphabetic(aChar);
                        res += alphabetic ? "1" : "0";
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

}
