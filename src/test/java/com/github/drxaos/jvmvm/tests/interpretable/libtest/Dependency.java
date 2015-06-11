package com.github.drxaos.jvmvm.tests.interpretable.libtest;

import java.util.ArrayList;
import java.util.List;

public class Dependency {

    private List<String> strings = new ArrayList<String>();

    public void addWord(String s) {
        strings.add(s);
    }

    public String join() {
        String res = "";
        for (String s : strings) {
            res += s + " ";
        }
        return res + "Dependency";
    }

}
