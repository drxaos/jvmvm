package com.googlecode.jvmvm.ui.common;

import java.util.HashSet;

public class Obj {
    private static long counter = 0;

    public int x, y;
    public int nextX = -1, nextY = -1;
    public String type;
    public String id;
    public HashSet<String> inventory = new HashSet<String>();

    public Obj(int x, int y, String type) {
        this.id = (++counter) + "$" + Math.random();
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
