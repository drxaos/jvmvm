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
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import net.sf.jauvm.vm.AccessControl;
import net.sf.jauvm.vm.Types;

public final class ConstructorRef extends SymbolicRef<Constructor<?>> {
    private static final Reference<Constructor<?>> nil = new WeakReference<Constructor<?>>(null);
    private volatile Reference<Constructor<?>> constructor = nil;

    private final String owner;
    private final String descriptor;
    private final Reference<Class<?>> referrer;


    public ConstructorRef(String owner, String descriptor, Class<?> referrer) {
        this.owner = owner;
        this.descriptor = descriptor;
        this.referrer = new WeakReference<Class<?>>(referrer);
    }

    public Constructor<?> get() {
        if (constructor.get() == null) resolve();
        return constructor.get();
    }


    private synchronized void resolve() {
        if (constructor.get() != null) return;

        Class<?> cls = ClassRef.get(owner, referrer.get());

        if (cls.isInterface())
            throw new IncompatibleClassChangeError(Types.getInternalName(cls));

        Constructor<?> c = findConstructor(cls, descriptor);

        if (Modifier.isAbstract(c.getModifiers()))
            throw new AbstractMethodError(Types.getInternalName(c));

        AccessControl.checkPermission(c, referrer.get());
        AccessControl.makeAccessible(c);

        constructor = new SoftReference<Constructor<?>>(c);
    }

    private static Constructor<?> findConstructor(Class<?> cls, String descriptor) {
        assert cls != null;
        for (Constructor<?> c : cls.getDeclaredConstructors()) {
            if (constructorMatches(c, descriptor)) return c;
        }
        throw new NoSuchMethodError(constructorInternalName(cls, descriptor));
    }

    private static boolean constructorMatches(Constructor<?> constructor, String descriptor) {
        return descriptor.equals(Types.getDescriptor(constructor));
    }

    private static String constructorInternalName(Class<?> cls, String descriptor) {
        return Types.getInternalName(cls) + "/<init>" + descriptor;
    }
}