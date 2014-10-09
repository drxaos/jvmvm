package com.googlecode.jvmvm.ui.levels.level_01;

public abstract class Level {
    final public String getMusic() {
        return "The Green";
    }

    public abstract void startLevel(Map map);

    public abstract boolean onExit(Map map);

    private static void execute(Map map) {
        Level level = new CellBlockA();
        level.startLevel(map);
    }
}
