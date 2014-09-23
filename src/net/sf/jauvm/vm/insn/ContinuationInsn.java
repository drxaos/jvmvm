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

package net.sf.jauvm.vm.insn;

import java.lang.reflect.Method;
import net.sf.jauvm.Continuation;
import net.sf.jauvm.Continuation$friend;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.StackTracedException;
import net.sf.jauvm.vm.VirtualMachine;
import net.sf.jauvm.vm.ref.ClassRef;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public abstract class ContinuationInsn extends Insn {
    private static final StackTraceElement returnTo = new StackTraceElement(Continuation.class.getName(), "returnTo", null, -1);
    private static final StackTraceElement throwTo = new StackTraceElement(Continuation.class.getName(), "throwTo", null, -1);

    public static Insn getInsn(int opcode, String owner, String name, String desc, Class<?> cls) {
        assert Continuation.class == ClassRef.get(owner, cls);

        if (opcode == INVOKESPECIAL && "<init>".equals(name) && "()V".equals(desc))
            return new NewContinuationInsn();
        if (opcode == INVOKEVIRTUAL && "returnTo".equals(name)) {
            if ("()V".equals(desc)) return new ReturnToContinuation();
            if ("(Ljava/lang/Object;)V".equals(desc)) return new AReturnToContinuation();
        }
        if (opcode == INVOKEVIRTUAL && "throwTo".equals(name) && "(Ljava/lang/Throwable;)V".equals(desc))
            return new AThrowToContinuation();

        return null;
    }


    static final class NewContinuationInsn extends ContinuationInsn {
        NewContinuationInsn() {
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            frame.makeParentImmutable();
            frame.replaceObject(Continuation$friend.newInstance(frame.getMethod(), frame.getParent(), frame.getRet()));
        }
    }

    static final class ReturnToContinuation extends ContinuationInsn {
        ReturnToContinuation() {
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Continuation cont = (Continuation) frame.popObject();
            Frame target = Continuation$friend.getFrame(cont);
            int ret = Continuation$friend.getRet(cont);

            if (cont.getReturnType() != void.class)
                throw new StackTracedException(new IllegalArgumentException("return type mismatch"), returnTo);

            target = target == null ? null : target.getMutableCopy();

            vm.setCp(ret);
            vm.setFrame(target);
        }
    }

    static final class AReturnToContinuation extends ContinuationInsn {
        AReturnToContinuation() {
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Object obj = frame.popObject();
            Continuation cont = (Continuation) frame.popObject();
            Frame target = Continuation$friend.getFrame(cont);
            int ret = Continuation$friend.getRet(cont);
            Class<?> type = cont.getReturnType();

            if (!isAssignable(obj, type))
                throw new StackTracedException(new IllegalArgumentException("return type mismatch"), returnTo);

            target = target.getMutableCopy();

            if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
                target.pushInt(obj);
            } else if (type == long.class) {
                target.pushLong(obj);
            } else if (type == float.class) {
                target.pushFloat(obj);
            } else if (type == double.class) {
                target.pushDouble(obj);
            } else if (type != void.class) {
                target.pushObject(obj);
            }

            vm.setCp(ret);
            vm.setFrame(target);
        }

        private static boolean isAssignable(Object obj, Class<?> type) {
            if (type.isPrimitive()) {
                if (type == int.class) return isInt(obj);
                if (type == long.class) return isLong(obj);
                if (type == float.class) return isFloat(obj);
                if (type == double.class) return isDouble(obj);
                if (type == char.class) return isChar(obj);
                if (type == byte.class) return isByte(obj);
                if (type == short.class) return isShort(obj);
                if (type == boolean.class) return isBoolean(obj);
            }
            return obj == null || type.isInstance(obj);
        }

        private static boolean isBoolean(Object obj) {
            return obj instanceof Boolean;
        }

        private static boolean isChar(Object obj) {
            return obj instanceof Character;
        }

        private static boolean isByte(Object obj) {
            return obj instanceof Byte;
        }

        private static boolean isShort(Object obj) {
            return obj instanceof Short || isByte(obj);
        }

        private static boolean isInt(Object obj) {
            return obj instanceof Integer || isChar(obj) || isShort(obj);
        }

        private static boolean isLong(Object obj) {
            return obj instanceof Long || isInt(obj);
        }

        private static boolean isFloat(Object obj) {
            return obj instanceof Float || isLong(obj);
        }

        private static boolean isDouble(Object obj) {
            return obj instanceof Double || isFloat(obj);
        }
    }

    static final class AThrowToContinuation extends ContinuationInsn {
        AThrowToContinuation() {
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Throwable thrwbl = (Throwable) frame.popObject();
            Continuation cont = (Continuation) frame.popObject();
            Method method = Continuation$friend.getMethod(cont);
            Frame target = Continuation$friend.getFrame(cont);
            int ret = Continuation$friend.getRet(cont);

            if (thrwbl == null)
                throw new StackTracedException(new NullPointerException(), throwTo);
            if (!isDeclared(thrwbl, cont))
                throw new StackTracedException(new IllegalArgumentException("undeclared throwable"), throwTo);

            target = target == null ? null : target.getMutableCopy();

            vm.setCp(ret);
            vm.setFrame(target);

            throw new StackTracedException(thrwbl, new StackTraceElement(method.getDeclaringClass().getName(),
                                                                         method.getName(), null, -1));
        }

        private static boolean isDeclared(Throwable t, Continuation c) {
            if (t instanceof Error || t instanceof RuntimeException) return true;
            for (Class<?> type : c.getExceptionTypes()) if (type.isInstance(t)) return true;
            return false;
        }
    }
}
