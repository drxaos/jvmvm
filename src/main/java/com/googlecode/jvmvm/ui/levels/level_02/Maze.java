package com.googlecode.jvmvm.ui.levels.level_02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recursively divided maze, http://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_division_method
 */
public class Maze {
    private int _width, _height;
    private List<List<Integer>> _map;
    private List<List<Integer>> _stack;

    public Maze(int width, int height) {
        this._width = width;
        this._height = height;
    }

    public static class Callback {
        public void cell(int x, int y, int mapValue) {
        }
    }

    /**
     * Instantiates a Maze object of given width and height.
     * The Maze object can create a maze by calling maze.create(callback),
     * where the callback is a function that accepts (x, y, mapValue) and performs
     * some action for each point in a grid, where mapValue is a boolean
     * that is true if and only if the given point is part of the maze.
     */
    public void create(Callback callback) {
        int w = this._width;
        int h = this._height;

        this._map = new ArrayList<List<Integer>>();

        for (int i = 0; i < w; i++) {
            this._map.add(new ArrayList<Integer>());
            for (int j = 0; j < h; j++) {
                boolean border = (i == 0 || j == 0 || i + 1 == w || j + 1 == h);
                this._map.get(i).add(border ? 1 : 0);
            }
        }

        this._stack = new ArrayList<List<Integer>>();
        this._stack.add(new ArrayList<Integer>(Arrays.asList(1, 1, w - 2, h - 2)));

        this._process();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                callback.cell(i, j, this._map.get(i).get(j));
            }
        }
        this._map = null;
    }

    private void _process() {
        while (this._stack.size() > 0) {
            List<Integer> room = this._stack.remove(0); // [left, top, right, bottom]
            this._partitionRoom(room);
        }
    }

    private void _partitionRoom(List<Integer> room) {
        List<Integer> availX = new ArrayList<Integer>();
        List<Integer> availY = new ArrayList<Integer>();

        for (int i = room.get(0) + 1; i < room.get(2); i++) {
            int top = this._map.get(i).get(room.get(1) - 1);
            int bottom = this._map.get(i).get(room.get(3) + 1);
            if (top != 0 && bottom != 0 && (i % 2) == 0) {
                availX.add(i);
            }
        }

        for (int j = room.get(1) + 1; j < room.get(3); j++) {
            int left = this._map.get(room.get(0) - 1).get(j);
            int right = this._map.get(room.get(2) + 1).get(j);
            if (left != 0 && right != 0 && (j % 2) == 0) {
                availY.add(j);
            }
        }

        if (availX.size() == 0 || availY.size() == 0) {
            return;
        }

        int x = random(availX);
        int y = random(availY);

        this._map.get(x).set(y, 1);

        List<List<List<Integer>>> walls = new ArrayList<List<List<Integer>>>();

        List<List<Integer>> w = new ArrayList<List<Integer>>();
        walls.add(w); // left part
        for (int i = room.get(0); i < x; i++) {
            this._map.get(i).set(y, 1);
            w.add(new ArrayList<Integer>(Arrays.asList(i, y)));
        }

        w = new ArrayList<List<Integer>>();
        walls.add(w); // right part
        for (int i = x + 1; i <= room.get(2); i++) {
            this._map.get(i).set(y, 1);
            w.add(new ArrayList<Integer>(Arrays.asList(i, y)));
        }

        w = new ArrayList<List<Integer>>();
        walls.add(w); // top part
        for (int j = room.get(1); j < y; j++) {
            this._map.get(x).set(j, 1);
            w.add(new ArrayList<Integer>(Arrays.asList(x, j)));
        }

        w = new ArrayList<List<Integer>>();
        walls.add(w); // bottom part
        for (int j = y + 1; j <= room.get(3); j++) {
            this._map.get(x).set(j, 1);
            w.add(new ArrayList<Integer>(Arrays.asList(x, j)));
        }

        List<List<Integer>> solid = random(walls);
        for (int i = 0; i < walls.size(); i++) {
            List<List<Integer>> w1 = walls.get(i);
            if (w1 == solid) {
                continue;
            }

            List<Integer> hole = random(w1);
            this._map.get(hole.get(0)).set(hole.get(1), 0);
        }

        this._stack.add(new ArrayList<Integer>(Arrays.asList(room.get(0), room.get(1), x - 1, y - 1))); // left top
        this._stack.add(new ArrayList<Integer>(Arrays.asList(x + 1, room.get(1), room.get(2), y - 1))); // right top
        this._stack.add(new ArrayList<Integer>(Arrays.asList(room.get(0), y + 1, x - 1, room.get(3)))); // left bottom
        this._stack.add(new ArrayList<Integer>(Arrays.asList(x + 1, y + 1, room.get(2), room.get(3)))); // right bottom
    }

    private <T> T random(List<T> list) {
        if (list.size() == 0) {
            return null;
        }
        return list.get((int) (Math.random() * list.size()));
    }
}
