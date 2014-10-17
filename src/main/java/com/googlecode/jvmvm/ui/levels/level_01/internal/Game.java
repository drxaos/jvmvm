package com.googlecode.jvmvm.ui.levels.level_01.internal;


import com.googlecode.jvmvm.ui.SrcUtil;
import com.googlecode.jvmvm.ui.common.GameBase;
import com.googlecode.jvmvm.ui.levels.level_01.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class Game extends GameBase {
    public Game(String code) {
        super(code);
    }

    public Game() {
    }

    @Override
    public HttpHandler getApiHandler() {
        return new ApiHandler();
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
        return CellBlockA.class;
    }

    @Override
    public Class getMeClass() {
        return Me.class;
    }

    @Override
    public Object getMe() {
        return new Me();
    }

    @Override
    public Class getPlayerClass() {
        return Player.class;
    }

    @Override
    public Object getPlayer() {
        return new Player(this);
    }

    @Override
    public Class getMapClass() {
        return Map.class;
    }

    @Override
    public Object getMap() {
        return new Map(this);
    }

    @Override
    public String getMusic() {
        return "Yonnie_The_Green.mp3";
    }

    @Override
    public String getLevelNumber() {
        return "01";
    }

    @Override
    public Object getLevelName() {
        return "CellBlockA.java";
    }

    @Override
    public Object getLevelFolder() {
        return "level_01";
    }
}
