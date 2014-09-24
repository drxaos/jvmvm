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

package net.sf.jauvm.vm;

import org.objectweb.asm.ClassReader;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class GlobalCodeCache {
    private static final GlobalCodeCache instance = new GlobalCodeCache();

    private final Map<Class<?>, Map<String, MethodCode>> cache;
    private static CodeLoader codeLoader = new CodeLoader();

    private GlobalCodeCache() {
        this.cache = new WeakHashMap<Class<?>, Map<String, MethodCode>>();
    }


    public static synchronized MethodCode get(Class<?> cls, String methodId) {
        Map<String, MethodCode> code = instance.cache.get(cls);
        if (code == null) code = instance.loadCode(cls);
        return code.get(methodId);
    }

    private Map<String, MethodCode> loadCode(Class<?> cls) {
        if (cls == null) return Collections.emptyMap();
        assert Thread.holdsLock(GlobalCodeCache.class);

        Map<String, MethodCode> code = cache.get(cls.getSuperclass());
        if (code == null) code = loadCode(cls.getSuperclass());

//        for (Method m : cls.getDeclaredMethods()) {
//            if (!Modifier.isAbstract(m.getModifiers()) && !Modifier.isNative(m.getModifiers()) /*&&
//                    m.getAnnotation(interpretable.class) != null*/) {
        code = new HashMap<String, MethodCode>(code);
        readCode(cls, code);
//                break;
//            }
//        }

        cache.put(cls, code);
        return code;
    }

    public static void setCodeLoader(CodeLoader codeLoader) {
        if (codeLoader == null) {
            GlobalCodeCache.codeLoader = new CodeLoader();
        } else {
            GlobalCodeCache.codeLoader = codeLoader;
        }
    }

    private static void readCode(Class<?> cls, Map<String, MethodCode> code) {
        try {
            InputStream stream = codeLoader.getBytecodeStream(cls);
            if (stream != null) {
                new ClassReader(stream).accept(new CodeVisitor(cls, code), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class CodeLoader {
        public InputStream getBytecodeStream(Class<?> cls) {
            ClassLoader classLoader = cls.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            return classLoader.getResourceAsStream(Types.getInternalName(cls) + ".class");
        }

        public boolean checkAccess(Class cls) {
            return true;
        }
    }

    public static void checkAccess(Class cls) {
        if (!codeLoader.checkAccess(cls)) {
            throw new VirtualMachineException("Illegal class access: " + cls.getCanonicalName());
        }
    }
}