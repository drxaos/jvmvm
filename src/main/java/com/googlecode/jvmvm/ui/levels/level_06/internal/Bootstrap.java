package com.googlecode.jvmvm.ui.levels.level_06.internal;

import com.googlecode.jvmvm.ui.levels.level_06.*;
import com.googlecode.jvmvm.ui.levels.level_06.Object;

import java.awt.*;

class Bootstrap {

    private static Level level;
    private static Map map;
    private static String next;

    private static void execute(Map map) {
        Bootstrap.map = map;
        level = new Drones101();
        level.startLevel(map);
        if (!validateLevel(map)) {
            throw new RuntimeException("validation error");
        }
    }

    private static boolean validateLevel(Map map) {
        return level.validateLevel(map);
    }

    public static Boolean impassable(Definition d, Player player, String type, Object me) {
        try {
            return d.impassable(player, type, me);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void onCollision(Definition d, Player player) {
        d.onCollision(player);
    }

    public static void onPickUp(Definition d, Player player) {
        d.onPickUp(player);
    }

    public static void onDrop(Definition d) {
        d.onDrop();
    }

    public static void behavior(Definition d, Object me) {
        d.behavior(me);
    }

    public static void definitions(java.util.Map defMap) {

        defMap.put("empty", new Definition() {

            {
                color = Color.BLACK;
                symbol = ' ';
            }
        });
        defMap.put("player", new Definition() {

            {
                color = Color.GREEN;
                symbol = '@';
            }
        });
        defMap.put("computer", new Definition() {
            {
                type = "item";
                color = new Color(0x99, 0x99, 0x99);
                symbol = '⌘';
            }

            @Override
            public void onPickUp(Player player) {
                map.writeStatus("You have picked up the computer!");
                // show code
            }

            @Override
            public void onDrop() {
                // hide code
            }
        });
        defMap.put("block", new Definition() {
            {
                color = Color.LIGHT_GRAY;
                symbol = '#';
            }

            @Override
            public boolean impassable(Player player, String type, Object object) {
                return true;
            }

            @Override
            public void onCollision(Player player) {
                //map.writeStatus("Can't go thru walls");
            }
        });
        defMap.put("exit", new Definition() {
            {
                color = new Color(0f, 1f, 1f);
                symbol = '⎕';

            }

            @Override
            public void onCollision(Player player) {
                //next = "com.googlecode.jvmvm.ui.levels.level_07.internal.Game";
            }
        });

    }

    public static String getNext() {
        return next;
    }
}
