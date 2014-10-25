package com.googlecode.jvmvm.ui.common;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import com.googlecode.jvmvm.loader.ProjectExecutionException;
import com.googlecode.jvmvm.ui.*;
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

public abstract class GameBase extends AbstractGame {

    static HttpServer apiServer;

    private final int TIMEOUT = 2000;// * 1000;

    private final int START = 0;
    private final int PUSH = START + 1;
    private final int PLAY_INIT = PUSH + 1;
    private final int PLAY = PLAY_INIT + 1;
    private final int STOP = PLAY + 1;

    private int state = START;

    private String secret = "secret" + Math.random();
    private Obj player = null;
    private ArrayList<Obj> objs = new ArrayList<Obj>();
    private HashMap<String, Object> defMap = new HashMap<String, Object>();
    protected HashSet<String> inventory = new HashSet<String>();

    final Color defaultBg = Color.BLACK;
    protected Color[] mapBg = new Color[getWidth() * getHeight()];

    private int pushCounter = 0;

    private String status;
    private boolean startOfStart;
    private boolean endOfStart;

    private Object phoneCallback;

    public GameBase(Code code) {
        super();
        printLevelName(getLevelName());
        this.lvlCode = code;
    }

    @Override
    public boolean applyEdit(Code.Edit edit) {
        return lvlCode.apply(edit);
    }

    @Override
    public List<Integer> redLines() {
        return lvlCode.getReadonlyLines();
    }

    public void __auth(String command, String secret) {
        if (this.secret.equals(secret)) {
            if ("startOfStartLevel".equals(command)) {
                startOfStart = true;
            } else if ("endOfStartLevel".equals(command)) {
                endOfStart = true;
            }
        }
    }

    public Obj getPlayerObj() {
        return player;
    }

    abstract public HttpHandler getApiHandler();

    abstract public Class getBootstrapClass();

    abstract public Class getDefinitionClass();

    abstract public Class getLevelClass();

    abstract public Class getSourceClass();

    abstract public Class getObjectClass();

    abstract public Class getPlayerClass();

    abstract public Object getPlayer();

    abstract public Class getMapClass();

    abstract public Object getMap();

    public boolean isPlayerAtLocation(int x, int y) {
        return (player.x == x) && (player.y == y);
    }

    public void setSquareColor(int x, int y, Color color) {
        if (x < getWidth() && y < getHeight()) {
            mapBg[y * getWidth() + x] = color;
        }
    }

    public Color getSquareColor(int x, int y) {
        if (x < getWidth() && y < getHeight()) {
            return mapBg[y * getWidth() + x];
        }
        return Color.BLACK;
    }

    public int getObjX(String id) {
        Obj obj = findObj(id);
        return (obj != null) ? obj.x : 0;
    }

    public int getObjY(String id) {
        Obj obj = findObj(id);
        return (obj != null) ? obj.y : 0;
    }

    public void defineObject(String type, Object properties) {
        if (type == null || type.isEmpty()) {
            return;
        }
        if (defMap.containsKey(type)) {
            throw new RuntimeException("There is already a type of object named " + type + "!");
        }
        defMap.put(type, properties);
    }

    public Point findNearest(String fromObjId, String toObjType) {
        Obj from = findObj(fromObjId);
        Obj found = null;
        double dist = Double.MAX_VALUE;
        for (Obj obj : objs) {
            if (obj != from && obj.type.equals(toObjType)) {
                double d = (Math.sqrt(Math.pow(from.x - obj.x, 2) + Math.pow(from.y - obj.y, 2)));
                if (dist > d) {
                    dist = d;
                    found = obj;
                }
            }
        }
        if (found != null) {
            return new Point(found.x, found.y);
        } else {
            return null;
        }
    }

    private int getDx(String direction) {
        if ("right".equals(direction)) {
            return 1;
        } else if ("left".equals(direction)) {
            return -1;
        } else {
            return 0;
        }
    }

    private int getDy(String direction) {
        if ("down".equals(direction)) {
            return 1;
        } else if ("up".equals(direction)) {
            return -1;
        } else {
            return 0;
        }
    }

    public String getObjType(String id) {
        return findObj(id).type;
    }

    public Object getNearObjType(String id, String direction) {
        Obj obj = findObj(id);
        Obj near = findObj(obj.x + getDx(direction), obj.y + getDy(direction));
        if (near == null) {
            return defMap.get("empty");
        } else {
            return defMap.get(near.type);
        }
    }

    public void move(String id, String direction) {
        Obj obj = findObj(id);
        if (obj.nextX != -1 && obj.nextY != -1) {
            writeStatus("Can't move when it isn't your turn!");
        } else {
            obj.nextX = obj.x + getDx(direction);
            obj.nextY = obj.y + getDy(direction);
        }
    }

    public void setPhoneCallback(Object phoneCallback) {
        this.phoneCallback = phoneCallback;
    }

    public Object getDefinitionOfObject(String id) {
        return defMap.get(findObj(id).type);
    }

    public Object getDefinitionOfPlayer() {
        return defMap.get("player");
    }

    class DefinitionExecutor {
        Object definition;

        public DefinitionExecutor(Object definition) {
            this.definition = definition;
        }

        char getSymbol() {
            try {
                return definition.getClass().getField("symbol").getChar(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return ' ';
        }

        String getType() {
            try {
                return (String) definition.getClass().getField("type").get(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }

        Color getColor() {
            try {
                return (Color) definition.getClass().getField("color").get(definition);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return Color.LIGHT_GRAY;
        }

        public Boolean impassable(Object player, String type, Object me) {
            return (Boolean) levelVm.setupVM(getBootstrapClass().getName(), "impassable", null, new Class[]{getDefinitionClass(), getPlayerClass(), String.class, getObjectClass()}, new Object[]{definition, player, type, me}).run(TIMEOUT);
        }

        public void onCollision(Object player) {
            levelVm.setupVM(getBootstrapClass().getName(), "onCollision", null, new Class[]{getDefinitionClass(), getPlayerClass()}, new Object[]{definition, player}).run(TIMEOUT);
        }

        public void onPickUp(Object player) {
            levelVm.setupVM(getBootstrapClass().getName(), "onPickUp", null, new Class[]{getDefinitionClass(), getPlayerClass()}, new Object[]{definition, player}).run(TIMEOUT);
        }

        public void behavior(Object me) {
            levelVm.setupVM(getBootstrapClass().getName(), "behavior", null, new Class[]{getDefinitionClass(), getObjectClass()}, new Object[]{definition, me}).run(TIMEOUT);
        }

        public void onDrop() {
            levelVm.setupVM(getBootstrapClass().getName(), "onDrop", null, new Class[]{getDefinitionClass()}, new Object[]{definition}).run(TIMEOUT);
        }

    }

    @Override
    public void start() {
        actions.add(new Action.DisplayTitle(getLevelFolder() + "/" + getLevelName()));
        startApiServer();
        configureLevel();
    }

    protected void configureVm(Project vm) throws IOException {
    }

    protected boolean configureLevel() {
        Exception error = null;
        try {
            String path = "src/main/java";
            String lvlSrc = getSourceClass().getCanonicalName().replace(".", "/") + ".java";
            String baseSrc = getLevelClass().getCanonicalName().replace(".", "/") + ".java";
            String bootstrapSrc = getBootstrapClass().getCanonicalName().replace(".", "/") + ".java";
            if (lvlCode == null) {
                lvlCode = Code.parse(SrcUtil.loadSrc(path, lvlSrc));
            }
            actions.add(new Action.LoadCode(lvlCode.toString()));
            levelVm = new Project("level-vm")
                    .addFile(lvlSrc, lvlCode.toCompilationUnit(secret))
                    .addFile(baseSrc, SrcUtil.loadSrc(path, baseSrc))
                    .addFile(bootstrapSrc, SrcUtil.loadSrc(path, bootstrapSrc))
                    .addSystemClass(getObjectClass().getName())
                    .addSystemClass(getDefinitionClass().getName())
                    .addSystemClass(getMapClass().getName())
                    .addSystemClass(getPlayerClass().getName())
                    .addSystemClass(getPhoneCallbackClass().getName())
                    .addSystemClass(Point.class.getName())
                    .addSystemClasses(Vm.bootstrap);
            configureVm(levelVm);
            levelVm.compile()
                    .markObject("map", (java.io.Serializable) getMap());
        } catch (ProjectCompilerException e) {
            e.printStackTrace();
            error = e;
        } catch (IOException e) {
            e.printStackTrace();
            error = e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            error = e;
        }
        if (error != null) {
            actions.add(new Action.MoveCaretToBottomRight());
            actions.add(new Action.Print("\n" + error.toString()));
            actions.add(new Action.ShowCode());
            state = STOP;
            return false;
        } else {
            return true;
        }
    }

    protected abstract Class getPhoneCallbackClass();

    private void startApiServer() {
        try {
            if (apiServer == null) {
                apiServer = HttpServer.create(new InetSocketAddress(Editor.API_PORT), 0);
                apiServer.setExecutor(null);
                apiServer.start();
            }
            apiServer.createContext("/", getApiHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String baseSrc = "src/main/resources";
            byte[] response = SrcUtil.loadData(baseSrc, "docs/level_01/" + t.getRequestURI().getPath().replace("..", "").replaceFirst("^/", ""));
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Override
    public void step() {
        try {
            if (state == START) {
                // show parts of UI
                configureUi();

                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "definitions", null, new Class[]{java.util.Map.class}, new Object[]{defMap});
                levelVm.run(TIMEOUT);

                startOfStart = false;
                endOfStart = false;
                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "execute", null, new Class[]{getMapClass()}, new Object[]{Project.Marker.byName("map")});
                levelVm.run(TIMEOUT);
                if (!startOfStart || !endOfStart) {
                    actions.add(new Action.MoveCaretToBottomRight());
                    if (!startOfStart) {
                        actions.add(new Action.Print("\nstartLevel() has been tampered with!"));
                    } else if (!endOfStart) {
                        actions.add(new Action.Print("\nstartLevel() returned prematurely!"));
                    }
                    actions.add(new Action.ShowCode());
                    state = STOP;
                    return;
                }

                state = PUSH;
            } else if (state == PUSH) {
                pushLine();
                for (int x = 0; x < getWidth(); x++) {
                    Color bg = getSquareColor(x, 24);
                    actions.add(new Action.MoveCaret(x, 24));
                    actions.add(new Action.PutChar(Color.BLACK, bg != null ? bg : defaultBg, ' '));
                }
                for (Obj obj : objs) {
                    if (obj.y == pushCounter) {
                        Object d = defMap.get(obj.type);
                        actions.add(new Action.MoveCaret(obj.x, 24));
                        Color color = new DefinitionExecutor(d).getColor();
                        char symbol = new DefinitionExecutor(d).getSymbol();
                        Color bg = getSquareColor(obj.x, obj.y);
                        actions.add(new Action.PutChar(color, bg != null ? bg : defaultBg, symbol));
                    }
                }
                if (++pushCounter >= 25) {
                    state = PLAY_INIT;
                }
            } else if (state == PLAY || state == PLAY_INIT) {
                boolean shouldRedraw = false;
                boolean isDynamicsTurn = false;
                boolean phone = false;

                int toX = player.x, toY = player.y;

                if (key != null) {
                    // move player
                    if (key == KeyEvent.VK_DOWN && player.y < 24) {
                        toY++;
                    } else if (key == KeyEvent.VK_UP && player.y > 0) {
                        toY--;
                    } else if (key == KeyEvent.VK_RIGHT && player.x < 49) {
                        toX++;
                    } else if (key == KeyEvent.VK_LEFT && player.x > 0) {
                        toX--;
                    } else if (key == KeyEvent.VK_Q) {
                        phone = true;
                    }
                }

                if (state == PLAY_INIT) {
                    shouldRedraw = true;
                }

                // process player collisions
                Obj found = findObj(toX, toY);
                if (found != null && found != player) {
                    Object d = defMap.get(found.type);
                    if (new DefinitionExecutor(d).impassable(getPlayer(), "player", createObject(found.id))) {
                        toX = player.x;
                        toY = player.y;
                    }
                    if ("item".equals(new DefinitionExecutor(d).getType())) {
                        new DefinitionExecutor(d).onPickUp(getPlayer());
                        objs.remove(found);
                        inventory.add(found.type);
                    } else {
                        new DefinitionExecutor(d).onCollision(getPlayer());
                    }
                }
                if (player.x != toX || player.y != toY) {
                    player.x = toX;
                    player.y = toY;
                    isDynamicsTurn = true;
                    shouldRedraw = true;
                }

                // phone function
                if (phone && inventory.contains("phone")) {
                    if (phoneCallback == null) {
                        writeStatus("Your function phone isn't bound to any function!");
                        shouldRedraw = true;
                    } else {
                        levelVm.setupVM(getBootstrapClass().getName(), "phone", null, new Class[]{getPhoneCallbackClass()}, new Object[]{phoneCallback}).run(TIMEOUT);
                        shouldRedraw = true;
                    }
                }

                if (isDynamicsTurn) {
                    // dynamic objects behavior
                    for (Obj obj : objs) {
                        Object d = defMap.get(obj.type);
                        if ("dynamic".equals(new DefinitionExecutor(d).getType())) {
                            new DefinitionExecutor(d).behavior(createObject(obj.id));
                        }
                        // move
                        if (obj.nextX >= 0 && obj.nextY >= 0) {
                            Obj toObj = findObj(obj.nextX, obj.nextY);
                            if (toObj != null) {
                                // process collisions
                                Object toD = defMap.get(toObj.type);
                                if ("player".equals(toObj.type)) {
                                    new DefinitionExecutor(d).onCollision(getPlayer());
                                }
                                if (!new DefinitionExecutor(toD).impassable(null, obj.type, createObject(toObj.id))) {
                                    obj.x = obj.nextX;
                                    obj.y = obj.nextY;
                                }
                                if ("item".equals(new DefinitionExecutor(toD).getType())) {
                                    objs.remove(toObj);
                                    obj.inventory.add(toObj.type);
                                }
                            } else {
                                obj.x = obj.nextX;
                                obj.y = obj.nextY;
                            }
                            obj.nextX = -1;
                            obj.nextY = -1;
                            shouldRedraw = true;
                        }
                    }
                }

                if (shouldRedraw) {
                    // repaint screen
                    actions.add(new Action.Clear());
                    for (int x = 0; x < getWidth(); x++) {
                        for (int y = 0; y < getHeight(); y++) {
                            Color bg = getSquareColor(x, y);
                            actions.add(new Action.MoveCaret(x, y));
                            actions.add(new Action.PutChar(Color.BLACK, bg != null ? bg : defaultBg, ' '));
                        }
                    }
                    for (Obj obj : objs) {
                        Object d = defMap.get(obj.type);
                        Color color = new DefinitionExecutor(d).getColor();
                        char symbol = new DefinitionExecutor(d).getSymbol();
                        actions.add(new Action.MoveCaret(obj.x, obj.y));
                        Color bg = getSquareColor(obj.x, obj.y);
                        actions.add(new Action.PutChar(color, bg != null ? bg : defaultBg, symbol));
                    }

                    // draw player on the top of other objects
                    Object d = defMap.get("player");
                    Color color = new DefinitionExecutor(d).getColor();
                    char symbol = new DefinitionExecutor(d).getSymbol();
                    Color bg = getSquareColor(player.x, player.y);
                    actions.add(new Action.MoveCaret(player.x, player.y));
                    actions.add(new Action.PutChar(color, bg != null ? bg : defaultBg, symbol));

                    // display status if exists
                    if (status != null) {
                        displayStatus(status);
                        status = null;
                    }

                    // show parts of UI
                    configureUi();

                    // display inventory
                    String inv = "";
                    for (String t : inventory) {
                        inv += new DefinitionExecutor(defMap.get(t)).getSymbol();
                    }
                    actions.add(new Action.Inventory(inv));
                }

                // load next level on current level request
                levelVm.setupVM(getBootstrapClass().getCanonicalName(), "getNext", null, new Class[]{}, new Object[]{});
                Object next = levelVm.run(TIMEOUT);
                if (next != null) {
                    load((AbstractGame) Class.forName(next.toString()).newInstance());
                }

                state = PLAY;
            }
        } catch (ProjectExecutionException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            actions.add(new Action.MoveCaretToBottomRight());
            actions.add(new Action.Print("\n" + cause.toString()));
            actions.add(new Action.ShowCode());
            state = STOP;
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureUi() {
        // hide code if no computer
        if (inventory.contains("computer")) {
            actions.add(new Action.ShowCode());
        } else {
            actions.add(new Action.HideCode());
        }

        // hide phone button if no phone
        if (inventory.contains("phone")) {
            actions.add(new Action.ShowPhone());
        } else {
            actions.add(new Action.HidePhone());
        }
    }

    protected abstract Object createObject(String id);

    @Override
    public void stop() {
        if (apiServer != null) {
            try {
                apiServer.removeContext("/");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private Obj findObj(String id) {
        Obj found = null;
        for (Obj obj : objs) {
            if (obj.id.equals(id)) {
                found = obj;
                break;
            }
        }
        return found;
    }

    public void placeObject(int x, int y, String type) {
        if (x < 0 || x >= getWidth()) {
            return;
        }
        if (y < 0 || y >= getHeight()) {
            return;
        }
        Obj found = findObj(x, y);
        if (found != null) {
            if (found.type.equals(type)) {
                return;
            }
            throw new RuntimeException("There is already an object at (" + x + ", " + y + ")");
        }
        if ("empty".equals(type)) {
            return;
        }
        if (!defMap.containsKey(type)) {
            throw new RuntimeException("There is no type of object named " + type);
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
        List<String> strings = new ArrayList<String>();
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


    public int countObjects(String objectType) {
        int count = 0;
        for (Obj obj : objs) {
            if (obj.type.equals(objectType)) {
                count++;
            }
        }
        return count;
    }

    public void validateAtLeastXObjects(int num, String objectType) {
        int count = countObjects(objectType);
        if (count < num) {
            throw new RuntimeException("Not enough blocks on the map! Expected: " + num + ", found: " + count);
        }
    }

    public void validateExactlyXManyObjects(int num, String objectType) {
        int count = countObjects(objectType);
        if (count != num) {
            throw new RuntimeException("Wrong number of " + objectType + "s on the map! Expected: " + num + ", found: " + count);
        }
    }
}
