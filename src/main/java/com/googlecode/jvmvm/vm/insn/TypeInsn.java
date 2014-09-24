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

package com.googlecode.jvmvm.vm.insn;

import com.googlecode.jvmvm.SilentObjectCreator;
import com.googlecode.jvmvm.vm.Frame;
import com.googlecode.jvmvm.vm.VirtualMachine;
import com.googlecode.jvmvm.vm.ref.ClassRef;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import static org.objectweb.asm.Opcodes.*;

public final class TypeInsn extends Insn {
    public static Insn getInsn(int opcode, String name, Class<?> cls) {
        return new TypeInsn(opcode, name, cls);
    }

    public final static class LazyNewObject implements Serializable {
        private Class type;
        private String signature;

        public LazyNewObject(Class type) {
            this.type = type;
            this.signature = "lazy-" + type.getCanonicalName() + "-" + System.identityHashCode(this);
        }

        public void init(Class constructorClass, VirtualMachine vm) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Object object = SilentObjectCreator.create(type, constructorClass);
            vm.getFrame().replaceAllRecursive(this, object);
        }

        public void init(Class constructorClass, Class<?>[] paramTypes, Object[] paramValues, VirtualMachine vm) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            Object object = SilentObjectCreator.create(type, constructorClass, paramTypes, paramValues);
            vm.getFrame().replaceAllRecursive(this, object);
        }

        public Class getType() {
            return type;
        }

        public String getSignature() {
            return signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LazyNewObject that = (LazyNewObject) o;
            return !(signature != null ? !signature.equals(that.signature) : that.signature != null);
        }

        @Override
        public int hashCode() {
            return signature != null ? signature.hashCode() : 0;
        }
    }

    private final int opcode;
    private final ClassRef c;

    public Class getClazz() {
        return c.get();
    }

    TypeInsn(int opcode, String name, Class<?> cls) {
        this.opcode = opcode;
        this.c = new ClassRef(name, cls);
    }

    public void execute(VirtualMachine vm) {
        Frame frame = vm.getFrame();
        Class<?> cls = c.get();
        switch (opcode) {
            case NEW:
                frame.pushObject(new LazyNewObject(this.getClazz()));
                return;
            case CHECKCAST:
                frame.pushObject(cls.cast(frame.popObject()));
                return;
            case INSTANCEOF:
                frame.pushInt(cls.isInstance(frame.popObject()) ? 1 : 0);
                return;
            case ANEWARRAY:
                frame.pushObject(Array.newInstance(cls, frame.popInt()));
                return;
            default:
                assert false;
        }
    }

    @Override
    public String toString() {
        return getOpcodeName(opcode) + " " + c.get().getCanonicalName();
    }
}
