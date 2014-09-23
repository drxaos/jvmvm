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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.sf.jauvm.vm.AccessControl;
import net.sf.jauvm.vm.Types;

public final class FieldRef extends SymbolicRef<Field> {
    private static final Reference<Field> nil = new WeakReference<Field>(null);
    private volatile Reference<Field> field = nil;

    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean expectsStatic;
    private final boolean expectsPuttable;
    private final Reference<Class<?>> referrer;


    public FieldRef(String owner, String name, String descriptor, Class<?> referrer, boolean expectsStatic,
                    boolean expectsPuttable) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.expectsStatic = expectsStatic;
        this.expectsPuttable = expectsPuttable;
        this.referrer = new WeakReference<Class<?>>(referrer);
    }

    public Field get() {
        if (field.get() == null) resolve();
        return field.get();
    }


    private synchronized void resolve() {
        if (field.get() != null) return;

        Class<?> cls = ClassRef.get(owner, referrer.get());

        Field f = findField(cls, name, descriptor);

        if (expectsStatic != Modifier.isStatic(f.getModifiers()))
            throw new IncompatibleClassChangeError(Types.getInternalName(cls));

        if (expectsPuttable && Modifier.isFinal(f.getModifiers()))
            throw new IllegalAccessError(Types.getInternalName(f));

        AccessControl.checkPermission(f, referrer.get());
        AccessControl.makeAccessible(f);

        field = new SoftReference<Field>(f);
    }

    private static Field findField(Class<?> cls, String name, String descriptor) {
        assert cls != null;
        for (; cls != null; cls = cls.getSuperclass()) {
            for (Field f : cls.getDeclaredFields()) {
                if (fieldMatches(f, name, descriptor)) return f;
            }
            for (Class<?> c : cls.getInterfaces()) {
                Field f = findFieldHelper(c, name, descriptor);
                if (f != null) return f;
            }
        }
        throw new NoSuchFieldError(fieldInternalName(cls, name, descriptor));
    }

    private static Field findFieldHelper(Class<?> cls, String name, String descriptor) {
        for (Field f : cls.getDeclaredFields()) {
            if (fieldMatches(f, name, descriptor)) return f;
        }
        for (Class<?> c : cls.getInterfaces()) {
            Field f = findFieldHelper(c, name, descriptor);
            if (f != null) return f;
        }
        return null;
    }

    private static boolean fieldMatches(Field f, String name, String descriptor) {
        return name.equals(f.getName()) && descriptor.equals(Types.getDescriptor(f));
    }

    private static String fieldInternalName(Class<?> cls, String name, String descriptor) {
        return Types.getInternalName(cls) + '/' + name + ' ' + descriptor;
    }
}