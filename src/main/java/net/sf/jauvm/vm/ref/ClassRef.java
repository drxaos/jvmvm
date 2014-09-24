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

package net.sf.jauvm.vm.ref;

import net.sf.jauvm.vm.AccessControl;
import net.sf.jauvm.vm.GlobalCodeCache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class ClassRef extends SymbolicRef<Class<?>> implements Serializable {
    private static final Reference<Class<?>> nil = new WeakReference<Class<?>>(null);
    private transient volatile Reference<Class<?>> cls = nil;

    private final String name;
    private transient Reference<Class<?>> referrer;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        referrer = new WeakReference<Class<?>>((Class<?>) in.readObject());
        in.defaultReadObject();
        cls = nil;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(referrer.get());
        out.defaultWriteObject();
    }

    public ClassRef(String name, Class<?> referrer) {
        this.name = name;
        this.referrer = new WeakReference<Class<?>>(referrer);
    }

    public Class<?> get() {
        if (cls.get() == null) resolve();
        return cls.get();
    }

    public static Class<?> get(String name, Class<?> referrer) {
        Class<?> c = findClass(name, referrer.getClassLoader());
        AccessControl.checkPermision(c, referrer);
        return c;
    }


    private synchronized void resolve() {
        if (cls.get() != null) return;
        Class<?> c = get(name, referrer.get());
        cls = new WeakReference<Class<?>>(c);

        GlobalCodeCache.checkAccess(c);
    }

    public static final Map<String, Class> primitives;

    static {
        primitives = new HashMap<String, Class>();

        Class[] types = {
                Boolean.TYPE,
                Byte.TYPE,
                Character.TYPE,
                Double.TYPE,
                Float.TYPE,
                Integer.TYPE,
                Long.TYPE,
                Short.TYPE,
                Void.TYPE
        };

        for (Class type : types) {
            primitives.put(type.getName(), type);
        }
    }

    private static Class<?> findClass(String name, ClassLoader classLoader) {
        try {
            if (primitives.keySet().contains(name)) {
                return primitives.get(name);
            }
            return Class.forName(name.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(name);
        }
    }
}
