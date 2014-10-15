package com.googlecode.jvmvm.ui.levels.level_01.internal;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import com.googlecode.jvmvm.loader.ProjectExecutionException;
import com.googlecode.jvmvm.ui.Action;
import com.googlecode.jvmvm.ui.Editor;
import com.googlecode.jvmvm.ui.SrcUtil;
import com.googlecode.jvmvm.ui.Vm;
import com.googlecode.jvmvm.ui.levels.level_01.CellBlockA;
import com.googlecode.jvmvm.ui.levels.level_01.Level;
import com.googlecode.jvmvm.ui.levels.level_01.Map;
import com.googlecode.jvmvm.ui.levels.level_01.Player;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Game extends com.googlecode.jvmvm.ui.Game {

    private final int START = 0;
    private final int PUSH = START + 1;
    private final int PLAY = PUSH + 1;
    private final int STOP = PLAY + 1;

    private int state = START;
    private String code;
    HttpServer server;

    private String secret = "secret" + Math.random();
    private Obj player = null;
    private ArrayList<Obj> objs = new ArrayList<Obj>();
    private HashMap<String, Definition> defMap = new HashMap<String, Definition>();
    private HashSet<String> inventory = new HashSet<String>();

    private Code lvlCode;

    private int pushCounter = 0;

    private String status;

    public Game(String code) {
        super("level_01", "CellBlockA.java");
        this.code = code;
    }

    public Game() {
        this(null);
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

        try {
            server = HttpServer.create(new InetSocketAddress(Editor.API_PORT), 0);
            server.createContext("/", new ApiHandler());
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String path = "src/main/java";
            String lvlSrc = CellBlockA.class.getCanonicalName().replace(".", "/") + ".java";
            String baseSrc = Level.class.getCanonicalName().replace(".", "/") + ".java";
            String bootstrapSrc = Bootstrap.class.getCanonicalName().replace(".", "/") + ".java";
            lvlCode = Code.parse(SrcUtil.loadSrc(path, lvlSrc));
            if (code != null) {
                lvlCode.apply(code, false);
            }

            if (code == null) {
                actions.add(new Action.LoadCode(lvlCode.toString()));
            }
            try {
                levelVm = new Project("level-vm")
                        .addFile(lvlSrc, lvlCode.toCompilationUnit(secret))
                        .addFile(baseSrc, SrcUtil.loadSrc(path, baseSrc))
                        .addFile(bootstrapSrc, SrcUtil.loadSrc(path, bootstrapSrc))
                        .addSystemClass(Me.class.getName())
                        .addSystemClass(Definition.class.getName())
                        .addSystemClass(Map.class.getName())
                        .addSystemClass(Player.class.getName())
                        .addSystemClasses(Vm.bootstrap)
                        .compile()
                        .markObject("map", new Map(this));
                actions.add(new Action.HideCode());
            } catch (ProjectCompilerException e) {
                actions.add(new Action.MoveCaretToBottomRight());
                actions.add(new Action.Print("\n" + e.getMessage()));
                actions.add(new Action.ShowCode());
                state = STOP;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String baseSrc = "src/main/resources";
            byte[] response = SrcUtil.loadData(baseSrc, "docs/level_01/" + t.getRequestURI().getPath());
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Override
    public void step() {
        if (state == START) {
            try {
                levelVm.setupVM(Bootstrap.class.getCanonicalName(), "definitions", null, new Class[]{java.util.Map.class}, new Object[]{defMap});
                levelVm.run(2000);
                levelVm.setupVM(Bootstrap.class.getCanonicalName(), "execute", null, new Class[]{Map.class}, new Object[]{Project.Marker.byName("map")});
                levelVm.run(2000);
            } catch (ProjectExecutionException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    cause = e;
                }
                actions.add(new Action.MoveCaretToBottomRight());
                actions.add(new Action.Print("\n" + cause.getMessage()));
                actions.add(new Action.ShowCode());
                state = STOP;
                return;
            }
            state = PUSH;
        } else if (state == PUSH) {
            pushLine();
            for (Obj obj : objs) {
                if (obj.y == pushCounter) {
                    Definition d = defMap.get(obj.type);
                    actions.add(new Action.MoveCaret(obj.x, 24));
                    Color color = d.color;
                    char symbol = d.symbol;
                    actions.add(new Action.Print(color, "" + symbol));
                }
            }
            if (++pushCounter >= 25) {
                state = PLAY;
            }
        } else if (state == PLAY) {
            int toX = player.x, toY = player.y;
            if (key != null) {
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
                if (d.impassable) {
                    toX = player.x;
                    toY = player.y;
                }
                if ("item".equals(d.type)) {
                    new DefinitionExecutor(d, levelVm).onPickUp(new Player(this));
                    objs.remove(found);
                    inventory.add(found.type);
                } else {
                    new DefinitionExecutor(d, levelVm).onCollision(new Player(this));
                }
            }
            player.x = toX;
            player.y = toY;

            if (key != null) {
                // repaint on user action
                actions.add(new Action.Clear());
                for (Obj obj : objs) {
                    Definition d = defMap.get(obj.type);
                    Color color = d.color;
                    char symbol = d.symbol;
                    actions.add(new Action.MoveCaret(obj.x, obj.y));
                    actions.add(new Action.Print(color, "" + symbol));
                }
                Definition d = defMap.get("player");
                Color color = d.color;
                char symbol = d.symbol;
                actions.add(new Action.MoveCaret(toX, toY));
                actions.add(new Action.Print(color, "" + symbol));

                if (status != null) {
                    displayStatus(status);
                    status = null;
                }
                if (inventory.contains("computer")) {
                    actions.add(new Action.ShowCode());
                } else {
                    actions.add(new Action.HideCode());
                }
                String inv = "";
                for (String t : inventory) {
                    inv += defMap.get(t).symbol;
                }
                actions.add(new Action.Inventory(inv));
            }
        }
    }

    @Override
    public void stop() {
        server.stop(0);
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
        status = text;
    }

    public void displayStatus(String text) {
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
        return inventory.contains(type);
    }
}
