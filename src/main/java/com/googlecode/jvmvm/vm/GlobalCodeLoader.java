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
 * 3. Neither the name of the copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
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

package com.googlecode.jvmvm.vm;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import org.objectweb.asm.ClassReader;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GlobalCodeLoader {

    public static synchronized MethodCode get(Class<?> cls, String methodId) {
        Map<String, MethodCode> code = loadCode(cls);
        return code.get(methodId);
    }

    public static synchronized Map<String, MethodCode> getAll(Class<?> cls) {
        return loadCode(cls);
    }

    private static Map<String, MethodCode> loadCode(Class<?> cls) {
        if (cls == null) return Collections.emptyMap();
        assert Thread.holdsLock(GlobalCodeLoader.class);
        Map<String, MethodCode> code = loadCode(cls.getSuperclass());
        code = new HashMap<String, MethodCode>(code);
        readCode(cls, code);
        return code;
    }

    private static void readCode(Class<?> cls, Map<String, MethodCode> code) {
        try {
            InputStream stream = null;
            if (cls.getClassLoader() instanceof MemoryClassLoader) {
                stream = ((MemoryClassLoader) cls.getClassLoader()).getBytecodeStream(cls);
            }
            if (stream != null) {
                new ClassReader(stream).accept(new CodeVisitor(cls, code), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}