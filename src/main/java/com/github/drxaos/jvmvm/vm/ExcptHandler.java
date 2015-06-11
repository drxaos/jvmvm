/**
 * Copyright (c) 2005 Nuno Cruces
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package com.github.drxaos.jvmvm.vm;

import com.github.drxaos.jvmvm.vm.ref.ClassRef;
import org.objectweb.asm.Label;

import java.util.Map;

public final class ExcptHandler {
    public static final ExcptHandler[] arrayType = new ExcptHandler[0];

    private Label startLbl;
    private Label endLbl;
    private Label handlerLbl;

    public int start;
    public int end;
    public int handler;
    public final ClassRef cls;

    public ExcptHandler(Label start, Label end, Label handler, ClassRef cls) {
        this.startLbl = start;
        this.endLbl = end;
        this.handlerLbl = handler;
        this.cls = cls;
    }

    public void resolve(Map<Label, Integer> map) {
        start = map.get(startLbl);
        end = map.get(endLbl);
        handler = map.get(handlerLbl);
        startLbl = null;
        endLbl = null;
        handlerLbl = null;
    }
}