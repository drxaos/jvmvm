package com.googlecode.jvmvm.ui.levels.level_01;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.Vm;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class Game extends com.googlecode.jvmvm.ui.Game {
    public Game() throws Exception {
        super("level_01", "CellBlockA.java");
        String path = "src/main/java/";
        String lvlSrc = CellBlockA.class.getCanonicalName().replace(".", "/") + ".java";
        String baseSrc = Level.class.getCanonicalName().replace(".", "/") + ".java";
        levelVm = new Project("level-vm")
                .addFile(lvlSrc, FileUtils.readFileToString(new File(path + lvlSrc)))
                .addFile(baseSrc, FileUtils.readFileToString(new File(path + baseSrc)))
                .addSystemClass(Map.class.getName())
                .addSystemClasses(Vm.bootstrap)
                .compile()
                .markObject("map", new Map(this))
                .setupVM(Level.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
    }

    @Override
    public void step() {
        for (int i = 0; i < 100; i++) {
            levelVm.step();
        }
    }
}
