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
import net.sf.jauvm.vm.MethodCode;
import net.sf.jauvm.vm.Types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class MethodRef extends SymbolicRef<Method> implements Serializable {
    private static final Reference<Method> nil = new WeakReference<Method>(null);
    private transient volatile Reference<Method> method = nil;

    private final String id;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean expectsStatic;
    private final boolean expectsInterface;
    private transient Reference<Class<?>> referrer;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        referrer = new WeakReference<Class<?>>((Class<?>) in.readObject());
        in.defaultReadObject();
        method = nil;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(referrer.get());
        out.defaultWriteObject();
    }

    public MethodRef(String owner, String name, String descriptor, Class<?> referrer, boolean expectsStatic,
                     boolean expectsInterface) {
        this.id = (name + descriptor).intern();
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.expectsStatic = expectsStatic;
        this.expectsInterface = expectsInterface;
        this.referrer = new WeakReference<Class<?>>(referrer);
    }

    public Method get() {
        if (method.get() == null) resolve();
        return method.get();
    }

    public Method get(Class<?> cls) {
        Method m = findMethod(cls, name, descriptor);
        AccessControl.makeAccessible(m);
        return m;
    }

    public static Method get(Class<?> cls, String name, String descriptor) {
        Method m = findMethod(cls, name, descriptor);
        AccessControl.makeAccessible(m);
        return m;
    }

    public MethodCode getCode(Class<?> cls) {
        return GlobalCodeCache.get(cls, id);
    }

    public static MethodCode getCode(Class<?> cls, String name, String descriptor) {
        return GlobalCodeCache.get(cls, (name + descriptor).intern());
    }


    private synchronized void resolve() {
        if (method.get() != null) return;

        Class<?> cls = ClassRef.get(owner, referrer.get());

        if (expectsInterface != cls.isInterface())
            throw new IncompatibleClassChangeError(Types.getInternalName(cls));

        Method m = expectsInterface ?
                findInterfaceMethod(cls, name, descriptor) :
                findMethod(cls, name, descriptor);

        if (expectsStatic != Modifier.isStatic(m.getModifiers()))
            throw new IncompatibleClassChangeError(Types.getInternalName(cls));

        if (expectsInterface && !Modifier.isPublic(m.getModifiers()))
            throw new IllegalAccessError(Types.getInternalName(m));

        if (!Modifier.isAbstract(cls.getModifiers()) && Modifier.isAbstract(m.getModifiers()))
            throw new AbstractMethodError(Types.getInternalName(m));

        AccessControl.checkPermission(m, referrer.get());
        AccessControl.makeAccessible(m);

        method = new SoftReference<Method>(m);

        GlobalCodeCache.checkAccess(m.getDeclaringClass());
    }

    private static Method findMethod(Class<?> cls, String name, String descriptor) {
        assert cls != null;
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (methodMatches(m, name, descriptor)) return m;
            }
        }
        for (Method m : cls.getMethods()) {
            if (methodMatches(m, name, descriptor)) return m;
        }
        throw new NoSuchMethodError(methodInternalName(cls, name, descriptor));
    }

    private static Method findInterfaceMethod(Class<?> cls, String name, String descriptor) {
        for (Method m : cls.getMethods()) {
            if (methodMatches(m, name, descriptor)) return m;
        }
        for (Method m : Object.class.getDeclaredMethods()) {
            if (methodMatches(m, name, descriptor)) return m;
        }
        throw new NoSuchMethodError(methodInternalName(cls, name, descriptor));
    }

    private static boolean methodMatches(Method method, String name, String descriptor) {
        return name.equals(method.getName()) && descriptor.equals(Types.getDescriptor(method));
    }

    private static String methodInternalName(Class<?> cls, String name, String descriptor) {
        return Types.getInternalName(cls) + '/' + name + descriptor;
    }
}