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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import net.sf.jauvm.Monitor;
import net.sf.jauvm.vm.ref.MethodRef;

public final class Frame implements Cloneable, Serializable {
    private static final long serialVersionUID = 874267885800467086l;

    private final Frame parent;
    private final int ret;

    private int sp;
    private Object[] stack;

    private transient Method method;
    private transient MethodCode code;
    private transient boolean mutable;


    private Frame(Frame parent, int ret, Method method, MethodCode code) {
        this.parent = parent;
        this.ret = ret;
        this.method = method;
        this.code = code;
        this.sp = code.stackSize;
        this.stack = new Object[this.sp];
        this.mutable = true;
    }


    public Frame getParent() {
        return parent;
    }

    public int getRet() {
        return ret;
    }

    public Method getMethod() {
        return method;
    }

    public MethodCode getCode() {
        return code;
    }


    public static Frame newBootstrapFrame(Method method, MethodCode code, Object... params) {
        Frame frame = new Frame(null, 0, method, code);
        for (int i = 0; i < params.length; i++) frame.stack[i] = params[i];
        return frame;
    }

    public Frame newCallFrame(int ret, Method method, MethodCode code) {
        assert mutable;

        Frame frame = new Frame(this, ret, method, code);

        boolean isSynchronized = Modifier.isSynchronized(method.getModifiers());
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        Class<?>[] types = method.getParameterTypes();

        int i = types.length + (isStatic ? 0 : 1);
        for (Class<?> type : types) if (type == long.class || type == double.class) i++;
        for (int j = types.length; j-- > 0;) {
            Class<?> type = types[j];

            if (type == long.class || type == double.class) {
                frame.stack[i -= 2] = popBigObject();
            } else {
                frame.stack[i -= 1] = popObject();
            }
        }
        Object target;
        if (isStatic) target = method.getDeclaringClass();
        else {
            target = popObject();
            if (target == null) throw new NullPointerException();
            frame.stack[i -= 1] = target;
        }
        if (isSynchronized) Monitor.enter(target);

        return frame;
    }

    public Frame newTailCallFrame(Method method, MethodCode code) {
        assert mutable;

        Frame frame = new Frame(parent, ret, method, code);

        boolean isSynchronized = Modifier.isSynchronized(method.getModifiers());
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        Class<?>[] types = method.getParameterTypes();

        int i = types.length + (isStatic ? 0 : 1);
        for (Class<?> type : types)
            if (type == long.class || type == double.class) i++;
        for (int j = types.length; j-- > 0;) {
            Class<?> type = types[j];

            if (type == long.class || type == double.class) {
                frame.stack[i -= 2] = popBigObject();
            } else {
                frame.stack[i -= 1] = popObject();
            }
        }
        Object target;
        if (isStatic) target = method.getDeclaringClass();
        else {
            target = popObject();
            if (target == null) throw new NullPointerException();
            frame.stack[i -= 1] = target;
        }
        if (isSynchronized) Monitor.enter(target);

        return frame;
    }


    public int getInt(int var) {
        return (Integer) stack[var];
    }

    public void setInt(int var, int val) {
        stack[var] = val;
    }


    public int popInt() {
        return (Integer) popObject();
    }

    public void pushInt(int val) {
        pushObject(val);
    }

    public void pushInt(Object val) {
        if (val instanceof Byte) pushInt((int) (Byte) val);
        else if (val instanceof Short) pushInt((int) (Short) val);
        else if (val instanceof Character) pushInt((int) (Character) val);
        else if (val instanceof Boolean) pushInt((Boolean) val ? 1 : 0);
        else pushInt((int) (Integer) val);
    }

    public void storeInt(int var) {
        storeObject(var);
    }

    public void loadInt(int var) {
        loadObject(var);
    }

    public long popLong() {
        return (Long) popBigObject();
    }

    public void pushLong(long val) {
        pushBigObject(val);
    }

    public void pushLong(Object val) {
        if (val instanceof Integer) pushLong((long) (Integer) val);
        else if (val instanceof Byte) pushLong((long) (Byte) val);
        else if (val instanceof Short) pushLong((long) (Short) val);
        else if (val instanceof Character) pushLong((long) (Character) val);
        else pushLong((long) (Long) val);
    }

    public void storeLong(int var) {
        storeBigObject(var);
    }

    public void loadLong(int var) {
        loadBigObject(var);
    }

    public float popFloat() {
        return (Float) popObject();
    }

    public void pushFloat(float val) {
        pushObject(val);
    }

    public void pushFloat(Object val) {
        if (val instanceof Integer) pushFloat((float) (Integer) val);
        else if (val instanceof Long) pushFloat((float) (Long) val);
        else if (val instanceof Byte) pushFloat((float) (Byte) val);
        else if (val instanceof Short) pushFloat((float) (Short) val);
        else if (val instanceof Character) pushFloat((float) (Character) val);
        else pushFloat((float) (Float) val);
    }

    public void storeFloat(int var) {
        storeObject(var);
    }

    public void loadFloat(int var) {
        loadObject(var);
    }

    public double popDouble() {
        return (Double) popBigObject();
    }

    public void pushDouble(double val) {
        pushBigObject(val);
    }

    public void pushDouble(Object val) {
        if (val instanceof Integer) pushDouble((double) (Integer) val);
        else if (val instanceof Long) pushDouble((double) (Long) val);
        else if (val instanceof Float) pushDouble((double) (Float) val);
        else if (val instanceof Byte) pushDouble((double) (Byte) val);
        else if (val instanceof Short) pushDouble((double) (Short) val);
        else if (val instanceof Character) pushDouble((double) (Character) val);
        else pushDouble((double) (Double) val);
    }

    public void storeDouble(int var) {
        storeBigObject(var);
    }

    public void loadDouble(int var) {
        loadBigObject(var);
    }

    public Object popObject() {
        assert mutable;
        Object val = stack[sp];
        stack[sp] = null;
        sp += 1;
        return val;
    }

    public void pushObject(Object val) {
        assert mutable;
        sp -= 1;
        stack[sp] = val;
    }

    public void storeObject(int var) {
        assert mutable;
        stack[var] = stack[sp];
        stack[sp] = null;
        sp += 1;
    }

    public void loadObject(int var) {
        assert mutable;
        sp -= 1;
        stack[sp] = stack[var];
    }

    public void replaceObject(Object val) {
        Object old = popObject();
        for (int i = 0; i < stack.length; i++) if (stack[i] == old) stack[i] = val;
    }


    private Object popBigObject() {
        assert mutable;
        Object val = stack[sp];
        stack[sp] = null;
        sp += 2;
        return val;
    }

    private void pushBigObject(Object val) {
        assert mutable;
        sp -= 2;
        stack[sp] = val;
        stack[sp + 1] = null;
    }

    private void storeBigObject(int var) {
        assert mutable;
        stack[var] = stack[sp];
        stack[var + 1] = null;
        stack[sp] = null;
        sp += 2;
    }

    private void loadBigObject(int var) {
        assert mutable;
        sp -= 2;
        stack[sp] = stack[var];
        stack[sp + 1] = null;
    }


    public Object getTarget(Class<?>... types) {
        int i = types.length;
        for (Class<?> type : types) if (type == long.class || type == double.class) i++;
        return stack[sp + i];
    }

    public Object[] popParameters(Class<?>... types) {
        assert mutable;

        Object[] params = new Object[types.length];

        for (int i = types.length; i-- > 0;) {
            Class<?> type = types[i];

            if (type == long.class || type == double.class) {
                params[i] = popBigObject();
            } else if (type == byte.class) {
                params[i] = (byte) popInt();
            } else if (type == char.class) {
                params[i] = (char) popInt();
            } else if (type == short.class) {
                params[i] = (short) popInt();
            } else if (type == boolean.class) {
                params[i] = !popObject().equals(0);
            } else {
                params[i] = popObject();
            }
        }

        return params;
    }


    public void pop() {
        assert mutable;
        stack[sp] = null;
        sp += 1;
    }

    public void pop2() {
        assert mutable;
        stack[sp] = null;
        stack[sp + 1] = null;
        sp += 2;
    }

    public void popAll() {
        assert mutable;
        while (sp < stack.length) stack[sp++] = null;
    }


    public void dup() {
        assert mutable;
        sp -= 1;
        stack[sp] = stack[sp + 1];
    }

    public void dupBnth1() {
        assert mutable;
        sp -= 1;
        Object tmp = stack[sp] = stack[sp + 1];
        stack[sp + 1] = stack[sp + 2];
        stack[sp + 2] = tmp;
    }

    public void dupBnth2() {
        assert mutable;
        sp -= 1;
        Object tmp = stack[sp] = stack[sp + 1];
        stack[sp + 1] = stack[sp + 2];
        stack[sp + 2] = stack[sp + 3];
        stack[sp + 3] = tmp;
    }

    public void dup2() {
        assert mutable;
        sp -= 2;
        stack[sp] = stack[sp + 2];
        stack[sp + 1] = stack[sp + 3];
    }

    public void dup2Bnth1() {
        assert mutable;
        sp -= 2;
        Object tmp1 = stack[sp] = stack[sp + 2];
        Object tmp2 = stack[sp + 1] = stack[sp + 3];
        stack[sp + 2] = stack[sp + 4];
        stack[sp + 3] = tmp1;
        stack[sp + 4] = tmp2;
    }

    public void dup2Bnth2() {
        assert mutable;
        sp += 2;
        Object tmp1 = stack[sp] = stack[sp + 2];
        Object tmp2 = stack[sp + 1] = stack[sp + 3];
        stack[sp + 2] = stack[sp + 4];
        stack[sp + 3] = stack[sp + 5];
        stack[sp + 4] = tmp1;
        stack[sp + 5] = tmp2;
    }


    public void swap() {
        assert mutable;
        Object tmp = stack[sp];
        stack[sp] = stack[sp + 1];
        stack[sp + 1] = tmp;
    }


    public void makeParentImmutable() {
        for (Frame frame = parent; frame != null && frame.mutable; frame = frame.parent) frame.mutable = false;
    }

    public Frame getMutableCopy() {
        if (mutable) return this;
        try {
            Frame frame = (Frame) super.clone();
            frame.stack = stack.clone();
            frame.mutable = true;
            return frame;
        } catch (CloneNotSupportedException e) {
            throw (InternalError) new InternalError().initCause(e);
        }
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(method.getDeclaringClass());
        out.writeUTF(method.getName());
        out.writeUTF(Types.getDescriptor(method));

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Class<?> cls = (Class) in.readObject();
        String name = in.readUTF();
        String desc = in.readUTF();

        method = MethodRef.get(cls, name, desc);
        code = MethodRef.getCode(cls, name, desc);

        in.defaultReadObject();
    }


    public String toString() {
        return Types.getInternalName(method) + ' ' + Arrays.asList(stack).toString();
    }
}
