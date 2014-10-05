package com.googlecode.jvmvm.tests.interpretable;

import java.util.HashMap;

public class HashMapExamples {

    public static String test() {
        HashMap map = new HashMap();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");

        Object e = map.entrySet().iterator().next();
        Object e1 = e;

        if (e == e1) {
            return "success";
        } else {
            return "fail";
        }
    }

}
