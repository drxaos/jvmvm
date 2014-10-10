package com.googlecode.jvmvm.ui.levels.level_01.internal;

import com.googlecode.jvmvm.ui.levels.level_01.CellBlockA;
import com.googlecode.jvmvm.ui.levels.level_01.Level;
import com.googlecode.jvmvm.ui.levels.level_01.Map;

class Bootstrap {

    private static Level level;

    private static void execute(Map map) {
        level = new CellBlockA();
        level.startLevel(map);
    }
}
