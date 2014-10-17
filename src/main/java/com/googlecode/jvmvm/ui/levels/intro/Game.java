package com.googlecode.jvmvm.ui.levels.intro;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.AbstractGame;
import com.googlecode.jvmvm.ui.Action;
import com.googlecode.jvmvm.ui.SrcUtil;
import com.googlecode.jvmvm.ui.Vm;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Game extends AbstractGame {


    public Game() {
        super("intro", "Initialize.java");
    }

    public Game(String s) {
        this();
    }

    @Override
    public void start() {
        try {
            actions.add(new Action.HideCode());
            String path = "src/main/java";
            String src = Initialize.class.getCanonicalName().replace(".", "/") + ".java";
            String src1 = Level.class.getCanonicalName().replace(".", "/") + ".java";
            levelVm = new Project("intro")
                    .addFile(src, SrcUtil.loadSrc(path, src))
                    .addFile(src1, SrcUtil.loadSrc(path, src1))
                    .addSystemClass(Map.class.getName())
                    .addSystemClasses(Vm.bootstrap)
                    .compile()
                    .markObject("map", new Map(this))
                    .setupVM(Level.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void step() {
        if (levelVm.isActive()) {
            for (int i = 0; i < 1; i++) {
                levelVm.step();
            }
        } else {
            if (key != null) {
                try {
                    load(new com.googlecode.jvmvm.ui.levels.level_01.internal.Game());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public String getMusic() {
        return null;
    }

    @Override
    public boolean validateCode(String code) {
        return true;
    }

    @Override
    public List<Integer> redLines() {
        return Collections.emptyList();
    }

    @Override
    public String getLevelNumber() {
        return "00";
    }

    @Override
    public Object getLevelName() {
        return "Initialize.java";
    }

    @Override
    public Object getLevelFolder() {
        return "intro";
    }
}
