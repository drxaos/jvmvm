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

import com.googlecode.jvmvm.vm.Frame;
import com.googlecode.jvmvm.vm.VirtualMachine;
import com.googlecode.jvmvm.vm.ref.FieldRef;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.objectweb.asm.Opcodes.*;

public abstract class FieldInsn extends Insn {
    public static Insn getInsn(int opcode, String owner, String name, String desc, Class<?> cls) {
        switch (opcode) {
            case GETSTATIC:
                return new GetStaticInsn(owner, name, desc, cls);
            case GETFIELD:
                return new GetFieldInsn(owner, name, desc, cls);
            case PUTSTATIC:
                return new PutStaticInsn(owner, name, desc, cls);
            case PUTFIELD:
                return new PutFieldInsn(owner, name, desc, cls);
            default:
                assert false;
                return null;
        }
    }

    static final class GetStaticInsn extends FieldInsn {
        private final FieldRef f;

        GetStaticInsn(String owner, String name, String desc, Class<?> cls) {
            this.f = new FieldRef(owner, name, desc, cls, true, false);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            try {
                Field field = f.get();

                // enable access
                // TODO check access
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                Frame frame = vm.getFrame();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    frame.pushInt(field.getInt(null));
                } else if (type == long.class) {
                    frame.pushLong(field.getLong(null));
                } else if (type == float.class) {
                    frame.pushFloat(field.getFloat(null));
                } else if (type == double.class) {
                    frame.pushDouble(field.getDouble(null));
                } else if (type == boolean.class) {
                    frame.pushInt(field.getBoolean(null) ? 1 : 0);
                } else {
                    frame.pushObject(field.get(null));
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
        }

        @Override
        public Class getClassForClinit() {
            return f.get().getDeclaringClass();
        }
    }

    static final class PutStaticInsn extends FieldInsn {
        private final FieldRef f;

        PutStaticInsn(String owner, String name, String desc, Class<?> cls) {
            this.f = new FieldRef(owner, name, desc, cls, true, true);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            try {
                Field field = f.get();

                // enable access
                // TODO check access
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                Frame frame = vm.getFrame();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    int value = frame.popInt();
                    field.setInt(null, value);
                    vm.setStaticValue(f, value);
                } else if (type == long.class) {
                    long value = frame.popLong();
                    field.setLong(null, value);
                    vm.setStaticValue(f, value);
                } else if (type == float.class) {
                    float value = frame.popFloat();
                    field.setFloat(null, value);
                    vm.setStaticValue(f, value);
                } else if (type == double.class) {
                    double value = frame.popDouble();
                    field.setDouble(null, value);
                    vm.setStaticValue(f, value);
                } else if (type == boolean.class) {
                    boolean value = frame.popInt() != 0;
                    field.setBoolean(null, value);
                    vm.setStaticValue(f, value);
                } else {
                    Object value = frame.popObject();
                    field.set(null, value);
                    vm.setStaticValue(f, value);
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
        }

        @Override
        public Class getClassForClinit() {
            return f.get().getDeclaringClass();
        }
    }

    static final class GetFieldInsn extends FieldInsn {
        private final FieldRef f;

        GetFieldInsn(String owner, String name, String desc, Class<?> cls) {
            this.f = new FieldRef(owner, name, desc, cls, false, false);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            try {
                Field field = f.get();

                // enable access
                // TODO check access
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                Frame frame = vm.getFrame();
                Object obj = frame.popObject();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    frame.pushInt(field.getInt(obj));
                } else if (type == long.class) {
                    frame.pushLong(field.getLong(obj));
                } else if (type == float.class) {
                    frame.pushFloat(field.getFloat(obj));
                } else if (type == double.class) {
                    frame.pushDouble(field.getDouble(obj));
                } else if (type == boolean.class) {
                    frame.pushInt(field.getBoolean(obj) ? 1 : 0);
                } else {
                    frame.pushObject(field.get(obj));
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
        }
    }

    static final class PutFieldInsn extends FieldInsn {
        private final FieldRef f;

        PutFieldInsn(String owner, String name, String desc, Class<?> cls) {
            this.f = new FieldRef(owner, name, desc, cls, false, true);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            try {
                Field field = f.get();

                // enable access from constructors to final fields and from objects to their private fields
                // TODO check access
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                Frame frame = vm.getFrame();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    int t = frame.popInt();
                    Object target = frame.popObject();
                    if (type == byte.class) {
                        field.setByte(target, (byte) t);
                    } else if (type == char.class) {
                        field.setChar(target, (char) t);
                    } else if (type == short.class) {
                        field.setShort(target, (short) t);
                    } else {
                        field.setInt(target, t);
                    }
                } else if (type == long.class) {
                    long t = frame.popLong();
                    Object target = frame.popObject();
                    field.setLong(target, t);
                } else if (type == float.class) {
                    float t = frame.popFloat();
                    Object target = frame.popObject();
                    field.setFloat(target, t);
                } else if (type == double.class) {
                    double t = frame.popDouble();
                    Object target = frame.popObject();
                    field.setDouble(target, t);
                } else if (type == boolean.class) {
                    boolean t = frame.popInt() != 0;
                    Object target = frame.popObject();
                    field.setBoolean(target, t);
                } else {
                    Object t = frame.popObject();
                    Object target = frame.popObject();
                    if (t instanceof TypeInsn.LazyNewObject) {
                        ((TypeInsn.LazyNewObject) t).addFieldSet(target, f);
                    } else if (target instanceof TypeInsn.LazyNewObject) {
                        ((TypeInsn.LazyNewObject) target).addFieldSet(target, f, t);
                    } else {
                        field.set(target, t);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
        }

        @Override
        public String toString() {
            return getOpcodeName(PUTFIELD) + " " + f.get();
        }
    }
}
