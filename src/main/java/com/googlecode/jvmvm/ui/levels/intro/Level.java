package com.googlecode.jvmvm.ui.levels.intro;

public abstract class Level {
    final public String getMusic() {
        return "";
    }

    public abstract void startLevel(Map map);

    private static void execute(Map map) {
        Level level = new Initialize();
        level.startLevel(map);
    }
}
