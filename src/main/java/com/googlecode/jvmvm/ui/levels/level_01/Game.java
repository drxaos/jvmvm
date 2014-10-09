package com.googlecode.jvmvm.ui.levels.level_01;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.Action;
import com.googlecode.jvmvm.ui.Vm;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Game extends com.googlecode.jvmvm.ui.Game {

    private final int START = 0;
    private final int PUSH = START + 1;
    private final int PLAY = PUSH + 1;

    private int state = START;
    private String code;

    private Obj player = null;
    private ArrayList<Obj> objs = new ArrayList<Obj>();
    private HashMap<String, Definition> defMap = new HashMap<String, Definition>();

    private int pushCounter = 0;

    public Game(String code) {
        super("level_01", "CellBlockA.java");
        this.code = code;
    }

    @Override
    public void start() {

        defMap.put("player", new Definition() {

            @Override
            public Color getColor() {
                return Color.GREEN;
            }

            @Override
            public char getSymbol() {
                return '@';
            }
        });
        defMap.put("computer", new Definition() {

            @Override
            public Color getColor() {
                return Color.LIGHT_GRAY;
            }

            @Override
            public char getSymbol() {
                return '⌘';
            }
        });
        defMap.put("block", new Definition() {

            @Override
            public Color getColor() {
                return Color.LIGHT_GRAY;
            }

            @Override
            public char getSymbol() {
                return '#';
            }
        });
        defMap.put("exit", new Definition() {

            @Override
            public Color getColor() {
                return new Color(0f, 1f, 1f);
            }

            @Override
            public char getSymbol() {
                return '⎕';
            }
        });


        try {
            String path = "src/main/java/";
            String lvlSrc = CellBlockA.class.getCanonicalName().replace(".", "/") + ".java";
            String baseSrc = Level.class.getCanonicalName().replace(".", "/") + ".java";
            String lvlCode = code != null ? code : FileUtils.readFileToString(new File(path + lvlSrc));
            if (code == null) {
                actions.add(new Action.LoadCode(lvlCode));
                actions.add(new Action.ShowCode());
            }
            levelVm = new Project("level-vm")
                    .addFile(lvlSrc, lvlCode)
                    .addFile(baseSrc, FileUtils.readFileToString(new File(path + baseSrc)))
                    .addSystemClass(Map.class.getName())
                    .addSystemClass(Player.class.getName())
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
        if (state == START) {
            levelVm.run(2000);
            state = PUSH;
        } else if (state == PUSH) {
            pushLine();
            for (Obj obj : objs) {
                if (obj.y == pushCounter) {
                    Definition d = defMap.get(obj.type);
                    actions.add(new Action.MoveCaret(obj.x, 24));
                    actions.add(new Action.Print(d.getColor(), "" + d.getSymbol()));
                }
            }
            if (++pushCounter >= 25) {
                state = PLAY;
            }
        }
    }

    @Override
    public void stop() {

    }

    public void placePlayer(int x, int y) {
        if (player != null) {
            throw new RuntimeException("Can't place player twice!");
        }
        player = new Obj(x, y, "player");
        objs.add(player);
    }

    public void placeObject(int x, int y, String type) {
        if (!defMap.containsKey(type)) {
            throw new RuntimeException("There is no type of object named " + type);
        }
        Obj found = null;
        for (Obj obj : objs) {
            if (obj.x == x && obj.y == y) {
                found = obj;
                break;
            }
        }
        if (found != null) {
            objs.remove(found);
        }
        objs.add(new Obj(x, y, type));
    }
}
