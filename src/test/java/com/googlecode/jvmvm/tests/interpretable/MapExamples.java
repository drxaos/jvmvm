package com.googlecode.jvmvm.tests.interpretable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class MapExamples {

    public static Object sameObj(Object o) {
        return o;
    }

    public static String testHashMapEntry() {
        String result = ">";

        HashMap map = new HashMap();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");

        Iterator iterator = map.entrySet().iterator();
        Object e1 = iterator.next();
        Object e2 = sameObj(e1);
        if (sameObj(e1) == sameObj(e2)) {
            result += "success:" + e1 + ":" + e2 + ";";
        } else {
            result += "fail;";
        }
        iterator.next();
        iterator.next();
        Object e3 = iterator.next();
        Object e4 = sameObj(e1);
        if (sameObj(e3) == sameObj(e4)) {
            result += "success:" + e3 + ":" + e4 + ";";
        } else {
            result += "fail;";
        }

        Object k1 = map.keySet().iterator().next();
        Object k2 = sameObj(k1);
        if (sameObj(k1) == sameObj(k2)) {
            result += "success:" + k1 + ":" + k2 + ";";
        } else {
            result += "fail;";
        }

        Object v1 = map.values().iterator().next();
        Object v2 = sameObj(v1);
        if (sameObj(v1) == sameObj(v2)) {
            result += "success:" + v1 + ":" + v2 + ";";
        } else {
            result += "fail;";
        }

        return result;
    }

    public static String testTreeMapEntry() {
        String result = ">";

        TreeMap map = new TreeMap();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");

        Iterator iterator = map.entrySet().iterator();
        Object e1 = iterator.next();
        Object e2 = sameObj(e1);
        if (sameObj(e1) == sameObj(e2)) {
            result += "success:" + e1 + ":" + e2 + ";";
        } else {
            result += "fail;";
        }
        iterator.next();
        iterator.next();
        Object e3 = iterator.next();
        Object e4 = sameObj(e1);
        if (sameObj(e3) == sameObj(e4)) {
            result += "success:" + e3 + ":" + e4 + ";";
        } else {
            result += "fail;";
        }

        Object k1 = map.keySet().iterator().next();
        Object k2 = sameObj(k1);
        if (sameObj(k1) == sameObj(k2)) {
            result += "success:" + k1 + ":" + k2 + ";";
        } else {
            result += "fail;";
        }

        Object v1 = map.values().iterator().next();
        Object v2 = sameObj(v1);
        if (sameObj(v1) == sameObj(v2)) {
            result += "success:" + v1 + ":" + v2 + ";";
        } else {
            result += "fail;";
        }

        return result;
    }

}
