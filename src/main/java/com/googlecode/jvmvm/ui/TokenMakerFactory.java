package com.googlecode.jvmvm.ui;

import org.fife.ui.rsyntaxtextarea.TokenMaker;

import java.util.Set;

public class TokenMakerFactory extends org.fife.ui.rsyntaxtextarea.TokenMakerFactory{
    @Override
    protected TokenMaker getTokenMakerImpl(String key) {
        return new MapTokenMaker();
    }

    @Override
    public Set<String> keySet() {
        return null;
    }
}
