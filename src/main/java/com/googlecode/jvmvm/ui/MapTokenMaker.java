package com.googlecode.jvmvm.ui;

import org.fife.ui.rsyntaxtextarea.AbstractJFlexTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;

import javax.swing.text.Segment;

public class MapTokenMaker extends AbstractJFlexTokenMaker {
    @Override
    public void yybegin(int newState) {
        System.out.println(newState);
    }

    @Override
    public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
        if (text.count == 0) {
            return new TokenImpl(text, 0, 0, startOffset, 3);
        }
        TokenImpl t0 = null;
        TokenImpl t1 = null;
        for (int i = 0; i < text.count; i++) {
            TokenImpl t2 = new TokenImpl(text, i, i, startOffset + i, 3);
            if (t0 == null) {
                t0 = t2;
            }
            if (t1 != null) {
                t1.setNextToken(t2);
            }
            t1 = t2;
        }
        return t0;
    }
}
