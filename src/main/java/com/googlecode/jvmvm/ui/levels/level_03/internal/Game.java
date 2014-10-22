package com.googlecode.jvmvm.ui.levels.level_03.internal;


import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.ui.Action;
import com.googlecode.jvmvm.ui.Code;
import com.googlecode.jvmvm.ui.SrcUtil;
import com.googlecode.jvmvm.ui.common.GameBase;
import com.googlecode.jvmvm.ui.levels.level_03.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Game extends GameBase {
    public Game(Code code) {
        super(code);
    }

    public Game() {
        this(null);
    }

    @Override
    protected void configureVm(Project vm) throws IOException {
    }

    @Override
    public HttpHandler getApiHandler() {
        return new ApiHandler();
    }

    static class ApiHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String baseSrc = "src/main/resources";
            byte[] response = SrcUtil.loadData(baseSrc, "docs/level_03/" + t.getRequestURI().getPath().replace("..", "").replaceFirst("^/", ""));
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    @Override
    protected boolean configureLevel() {
        actions.add(new Action.ShowCode());
        inventory.add("computer");
        return super.configureLevel();
    }

    @Override
    protected Object createObject(String id) {
        return null;
    }

    @Override
    public Class getBootstrapClass() {
        return Bootstrap.class;
    }

    @Override
    public Class getDefinitionClass() {
        return Definition.class;
    }

    @Override
    public Class getLevelClass() {
        return Level.class;
    }

    @Override
    public Class getSourceClass() {
        return ValidationEngaged.class;
    }

    @Override
    public Class getObjectClass() {
        return Me.class;
    }

    @Override
    public Class getPlayerClass() {
        return Player.class;
    }

    Player player;

    @Override
    public Object getPlayer() {
        if (player == null) {
            try {
                Constructor<Player> c = Player.class.getDeclaredConstructor(new Class[]{Game.class});
                c.setAccessible(true);
                player = c.newInstance(this);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return player;
    }

    @Override
    public Class getMapClass() {
        return Map.class;
    }

    Map map;

    @Override
    public Object getMap() {
        if (map == null) {
            try {
                Constructor<Map> c = Map.class.getDeclaredConstructor(new Class[]{Game.class, Player.class});
                c.setAccessible(true);
                map = c.newInstance(this, getPlayer());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public String getMusic() {
        return "Revolution_Void_-_08_-_Obscure_Terrain.mp3";
    }

    @Override
    public String getLevelNumber() {
        return "03";
    }

    @Override
    public String getLevelName() {
        return "ValidationEngaged.java";
    }

    @Override
    public String getLevelFolder() {
        return "level_03";
    }
}
