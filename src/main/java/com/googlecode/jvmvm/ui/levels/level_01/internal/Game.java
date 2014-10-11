package com.googlecode.jvmvm.ui.levels.level_01.internal;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.Action;
import com.googlecode.jvmvm.ui.Vm;
import com.googlecode.jvmvm.ui.levels.level_01.CellBlockA;
import com.googlecode.jvmvm.ui.levels.level_01.Level;
import com.googlecode.jvmvm.ui.levels.level_01.Map;
import com.googlecode.jvmvm.ui.levels.level_01.Player;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Game extends com.googlecode.jvmvm.ui.Game {

    private final int START = 0;
    private final int PUSH = START + 1;
    private final int PLAY = PUSH + 1;

    private int state = START;
    private String code;

    private String secret = "secret" + Math.random();
    private Obj player = null;
    private ArrayList<Obj> objs = new ArrayList<Obj>();
    private HashMap<String, Definition> defMap = new HashMap<String, Definition>();
    private HashSet<Definition> inventory = new HashSet<Definition>();

    private Code lvlCode;

    private int pushCounter = 0;

    public Game(String code) {
        super("level_01", "CellBlockA.java");
        this.code = code;
    }

    final public String getMusic() {
        return "Yonnie_The_Green.mp3";
    }

    @Override
    public boolean validateCode(String code) {
        return lvlCode.apply(code, false);
    }

    @Override
    public List<Integer> redLines() {
        return lvlCode.getReadonlyLines();
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
            public String getType() {
                return "item";
            }

            @Override
            public Color getColor() {
                return new Color(0x99, 0x99, 0x99);
            }

            @Override
            public char getSymbol() {
                return '⌘';
            }

            @Override
            public void onPickUp(Player player) {
                writeStatus("You have picked up the computer!");
                actions.add(new Action.ShowCode());
            }

            @Override
            public void onDrop() {
                actions.add(new Action.HideCode());
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

            @Override
            public boolean impassable() {
                return true;
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

            @Override
            public void onCollision(Player player) {
                try {
                    // TODO next
                    //  load(new com.googlecode.jvmvm.ui.levels.level_02.internal.Game(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        try {
            String path = "src/main/java/";
            String lvlSrc = CellBlockA.class.getCanonicalName().replace(".", "/") + ".java";
            String baseSrc = Level.class.getCanonicalName().replace(".", "/") + ".java";
            String bootstrapSrc = Bootstrap.class.getCanonicalName().replace(".", "/") + ".java";
            lvlCode = Code.parse(FileUtils.readFileToString(new File(path + lvlSrc)));
            if (code != null) {
                lvlCode.apply(code, false);
            }

            if (code == null) {
                actions.add(new Action.LoadCode(lvlCode.toString()));
            }
            levelVm = new Project("level-vm")
                    .addFile(lvlSrc, lvlCode.toCompilationUnit(secret))
                    .addFile(baseSrc, FileUtils.readFileToString(new File(path + baseSrc)))
                    .addFile(bootstrapSrc, FileUtils.readFileToString(new File(path + bootstrapSrc)))
                    .addSystemClass(Map.class.getName())
                    .addSystemClass(Player.class.getName())
                    .addSystemClasses(Vm.bootstrap)
                    .compile()
                    .markObject("map", new Map(this))
                    .setupVM(Bootstrap.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
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
        } else if (state == PLAY) {
            int toX = player.x, toY = player.y;
            if (key == null) {
                // no action
            } else {
                // repaint on user action
                actions.add(new Action.Clear());
                for (Obj obj : objs) {
                    Definition d = defMap.get(obj.type);
                    actions.add(new Action.MoveCaret(obj.x, obj.y));
                    actions.add(new Action.Print(d.getColor(), "" + d.getSymbol()));
                }

                // move player
                if (key == KeyEvent.VK_DOWN && player.y < 49) {
                    toY++;
                } else if (key == KeyEvent.VK_UP && player.y > 0) {
                    toY--;
                } else if (key == KeyEvent.VK_RIGHT && player.y < 24) {
                    toX++;
                } else if (key == KeyEvent.VK_LEFT && player.y > 0) {
                    toX--;
                }
            }
            Obj found = findObj(toX, toY);
            if (found != null && found != player) {
                Definition d = defMap.get(found.type);
                if (d.impassable()) {
                    toX = player.x;
                    toY = player.y;
                }
                if ("item".equals(d.getType())) {
                    d.onPickUp(new Player(this));
                    objs.remove(found);
                    inventory.add(d);
                } else {
                    d.onCollision(new Player(this));
                }
            }
            actions.add(new Action.MoveCaret(player.x, player.y));
            actions.add(new Action.Print(" "));
            actions.add(new Action.MoveCaret(toX, toY));
            actions.add(new Action.Print(defMap.get("player").getColor(), "" + defMap.get(player.type).getSymbol()));
            player.x = toX;
            player.y = toY;
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

    private Obj findObj(int x, int y) {
        Obj found = null;
        for (Obj obj : objs) {
            if (obj.x == x && obj.y == y) {
                found = obj;
                break;
            }
        }
        return found;
    }

    public void placeObject(int x, int y, String type) {
        if (!defMap.containsKey(type)) {
            throw new RuntimeException("There is no type of object named " + type);
        }
        Obj found = findObj(x, y);
        if (found != null) {
            objs.remove(found);
        }
        objs.add(new Obj(x, y, type));
    }

    public int getWidth() {
        return 50;
    }

    public int getHeight() {
        return 25;
    }


    public void writeStatus(String text) {
        java.util.List<String> strings = new ArrayList<String>();
        strings.add(text);

        if (text.length() > getWidth()) {
            // split into two lines
            int minCutoff = getWidth() - 10;
            int cutoff = minCutoff + text.substring(minCutoff).indexOf(" ");
            strings.clear();
            strings.add(text.substring(0, cutoff));
            strings.add(text.substring(cutoff + 1));
        }

        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            int x = (int) Math.floor((getWidth() - str.length()) / 2);
            int y = getHeight() + i - strings.size() - 1;
            drawText(x, y, str);
        }
    }

    public boolean hasItem(String type) {
        return false;
    }
}
