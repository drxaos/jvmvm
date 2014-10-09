package com.googlecode.jvmvm.ui.levels.intro;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.Vm;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class Game extends com.googlecode.jvmvm.ui.Game {
    public Game() throws Exception {
        super("intro", "Initialize.java");
        String path = "src/main/java/";
        String src = Initialize.class.getCanonicalName().replace(".", "/") + ".java";
        String src1 = Level.class.getCanonicalName().replace(".", "/") + ".java";
        levelVm = new Project("intro")
                .addFile(src, FileUtils.readFileToString(new File(path + src)))
                .addFile(src1, FileUtils.readFileToString(new File(path + src1)))
                .addSystemClass(Map.class.getName())
                .addSystemClasses(Vm.bootstrap)
                .compile()
                .markObject("map", new Map(this))
                .setupVM(Level.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
    }

    @Override
    public void step() {
        for (int i = 0; i < 1; i++) {
            levelVm.step();
        }
    }
}
