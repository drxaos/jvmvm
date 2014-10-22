package com.googlecode.jvmvm.ui.common;

class Obj {
    private static long counter = 0;

    int x, y;
    int nextX, nextY;
    String type;
    String id;

    public Obj(int x, int y, String type) {
        this.id = (++counter) + "$" + Math.random();
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
