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

import java.lang.reflect.Field;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.VirtualMachine;
import net.sf.jauvm.vm.ref.FieldRef;
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
    }

    static final class PutStaticInsn extends FieldInsn {
        private final FieldRef f;

        PutStaticInsn(String owner, String name, String desc, Class<?> cls) {
            this.f = new FieldRef(owner, name, desc, cls, true, true);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            try {
                Field field = f.get();
                Frame frame = vm.getFrame();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    field.setInt(null, frame.popInt());
                } else if (type == long.class) {
                    field.setLong(null, frame.popLong());
                } else if (type == float.class) {
                    field.setFloat(null, frame.popFloat());
                } else if (type == double.class) {
                    field.setDouble(null, frame.popDouble());
                } else if (type == boolean.class) {
                    field.setBoolean(null, frame.popInt() != 0);
                } else {
                    field.set(null, frame.popObject());
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
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
                Frame frame = vm.getFrame();
                Class<?> type = field.getType();
                if (type == int.class || type == byte.class || type == char.class || type == short.class) {
                    int t = frame.popInt();
                    field.setInt(frame.popObject(), t);
                } else if (type == long.class) {
                    long t = frame.popLong();
                    field.setLong(frame.popObject(), t);
                } else if (type == float.class) {
                    float t = frame.popFloat();
                    field.setFloat(frame.popObject(), t);
                } else if (type == double.class) {
                    double t = frame.popDouble();
                    field.setDouble(frame.popObject(), t);
                } else if (type == boolean.class) {
                    boolean t = frame.popInt() != 0;
                    field.setBoolean(frame.popObject(), t);
                } else {
                    Object t = frame.popObject();
                    field.set(frame.popObject(), t);
                }
            } catch (IllegalAccessException e) {
                throw new InternalError().initCause(e);
            }
        }
    }
}
