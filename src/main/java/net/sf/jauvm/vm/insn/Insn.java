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

import net.sf.jauvm.Monitor;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.VirtualMachine;
import org.objectweb.asm.Opcodes;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static org.objectweb.asm.Opcodes.*;

public abstract class Insn implements Serializable {
    public static final Insn[] arrayType = new Insn[0];

    public static Insn getInsn(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                return new ReturnInsn(opcode);
            default:
                return new NoArgsInsn(opcode);
        }
    }

    public abstract void execute(VirtualMachine vm) throws Throwable;


    static final class ReturnInsn extends Insn {
        private final int opcode;

        ReturnInsn(int opcode) {
            this.opcode = opcode;
        }

        public void execute(VirtualMachine vm) {
            Frame frame = vm.getFrame();
            switch (opcode) {
                case RETURN: {
                    Frame parent = frame.getParent() == null ? null : frame.getParent().getMutableCopy();
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }
                case IRETURN: {
                    Frame parent = frame.getParent().getMutableCopy();
                    parent.pushInt(frame.popInt());
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }
                case LRETURN: {
                    Frame parent = frame.getParent().getMutableCopy();
                    parent.pushLong(frame.popLong());
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }
                case FRETURN: {
                    Frame parent = frame.getParent().getMutableCopy();
                    parent.pushFloat(frame.popFloat());
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }
                case DRETURN: {
                    Frame parent = frame.getParent().getMutableCopy();
                    parent.pushDouble(frame.popDouble());
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }
                case ARETURN: {
                    Frame parent = frame.getParent().getMutableCopy();
                    parent.pushObject(frame.popObject());
                    vm.setCp(frame.getRet());
                    vm.setFrame(parent);
                    return;
                }

                default:
                    assert false;
            }
        }

        public boolean canReturn(Class cls) {
            switch (opcode) {
                case RETURN:
                    return cls == void.class;
                case IRETURN:
                    return cls == int.class || cls == byte.class || cls == short.class || cls == char.class || cls == boolean.class;
                case LRETURN:
                    return cls == long.class;
                case FRETURN:
                    return cls == float.class;
                case DRETURN:
                    return cls == double.class;
                case ARETURN:
                    return Object.class.isAssignableFrom(cls);
                default:
                    assert false;
                    throw null;
            }
        }

        @Override
        public String toString() {
            return getOpcodeName(opcode);
        }
    }

    static final class NoArgsInsn extends Insn {
        private final int opcode;

        NoArgsInsn(int opcode) {
            this.opcode = opcode;
        }

        public strictfp void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            switch (opcode) {
                case NOP:
                    return;

                case ICONST_0:
                    frame.pushInt(0);
                    return;
                case ICONST_1:
                    frame.pushInt(1);
                    return;
                case ICONST_2:
                    frame.pushInt(2);
                    return;
                case ICONST_3:
                    frame.pushInt(3);
                    return;
                case ICONST_4:
                    frame.pushInt(4);
                    return;
                case ICONST_5:
                    frame.pushInt(5);
                    return;
                case ICONST_M1:
                    frame.pushInt(-1);
                    return;
                case LCONST_0:
                    frame.pushLong(0l);
                    return;
                case LCONST_1:
                    frame.pushLong(1l);
                    return;
                case FCONST_0:
                    frame.pushFloat(0f);
                    return;
                case FCONST_1:
                    frame.pushFloat(1f);
                    return;
                case FCONST_2:
                    frame.pushFloat(2f);
                    return;
                case DCONST_0:
                    frame.pushDouble(0d);
                    return;
                case DCONST_1:
                    frame.pushDouble(1d);
                    return;
                case ACONST_NULL:
                    frame.pushObject(null);
                    return;

                case IALOAD: {
                    int t = frame.popInt();
                    frame.pushInt(Array.getInt(frame.popObject(), t));
                    return;
                }
                case LALOAD: {
                    int t = frame.popInt();
                    frame.pushLong(Array.getLong(frame.popObject(), t));
                    return;
                }
                case FALOAD: {
                    int t = frame.popInt();
                    frame.pushFloat(Array.getFloat(frame.popObject(), t));
                    return;
                }
                case DALOAD: {
                    int t = frame.popInt();
                    frame.pushDouble(Array.getDouble(frame.popObject(), t));
                    return;
                }
                case CALOAD: {
                    int t = frame.popInt();
                    frame.pushInt(Array.getChar(frame.popObject(), t));
                    return;
                }
                case SALOAD: {
                    int t = frame.popInt();
                    frame.pushInt(Array.getShort(frame.popObject(), t));
                    return;
                }
                case AALOAD: {
                    int t = frame.popInt();
                    frame.pushObject(Array.get(frame.popObject(), t));
                    return;
                }
                case BALOAD: {
                    int t1 = frame.popInt();
                    Object t2 = frame.popObject();
                    if (t2 instanceof byte[]) frame.pushInt(Array.getByte(t2, t1));
                    else frame.pushInt(Array.getBoolean(t2, t1) ? 1 : 0);
                    return;
                }
                case IASTORE: {
                    int t1 = frame.popInt();
                    int t2 = frame.popInt();
                    Array.setInt(frame.popObject(), t2, t1);
                    return;
                }
                case LASTORE: {
                    long t1 = frame.popLong();
                    int t2 = frame.popInt();
                    Array.setLong(frame.popObject(), t2, t1);
                    return;
                }
                case FASTORE: {
                    float t1 = frame.popFloat();
                    int t2 = frame.popInt();
                    Array.setFloat(frame.popObject(), t2, t1);
                    return;
                }
                case DASTORE: {
                    double t1 = frame.popDouble();
                    int t2 = frame.popInt();
                    Array.setDouble(frame.popObject(), t2, t1);
                    return;
                }
                case CASTORE: {
                    char t1 = (char) frame.popInt();
                    int t2 = frame.popInt();
                    Array.setChar(frame.popObject(), t2, t1);
                    return;
                }
                case SASTORE: {
                    short t1 = (short) frame.popInt();
                    int t2 = frame.popInt();
                    Array.setShort(frame.popObject(), t2, t1);
                    return;
                }
                case AASTORE: {
                    Object t1 = frame.popObject();
                    int t2 = frame.popInt();
                    try {
                        Array.set(frame.popObject(), t2, t1);
                    } catch (IllegalArgumentException e) {
                        throw new ArrayStoreException(t1.getClass().toString());
                    }
                    return;
                }
                case BASTORE: {
                    int t1 = frame.popInt();
                    int t2 = frame.popInt();
                    Object t3 = frame.popObject();
                    if (t3 instanceof byte[]) Array.setByte(t3, t2, (byte) t1);
                    else Array.setBoolean(t3, t2, t1 != 0);
                    return;
                }

                case POP:
                    frame.pop();
                    return;
                case POP2:
                    frame.pop2();
                    return;
                case DUP:
                    frame.dup();
                    return;
                case DUP_X1:
                    frame.dupBnth1();
                    return;
                case DUP_X2:
                    frame.dupBnth2();
                    return;
                case DUP2:
                    frame.dup2();
                    return;
                case DUP2_X1:
                    frame.dup2Bnth1();
                    return;
                case DUP2_X2:
                    frame.dup2Bnth2();
                    return;
                case SWAP:
                    frame.swap();
                    return;

                case IADD: {
                    frame.pushInt(frame.popInt() + frame.popInt());
                    return;
                }
                case ISUB: {
                    frame.pushInt(-frame.popInt() + frame.popInt());
                    return;
                }
                case IMUL: {
                    frame.pushInt(frame.popInt() * frame.popInt());
                    return;
                }
                case IDIV: {
                    int t = frame.popInt();
                    frame.pushInt(frame.popInt() / t);
                    return;
                }
                case IREM: {
                    int t = frame.popInt();
                    frame.pushInt(frame.popInt() % t);
                    return;
                }
                case INEG: {
                    frame.pushInt(-frame.popInt());
                    return;
                }
                case ISHL: {
                    int t = frame.popInt();
                    frame.pushInt(frame.popInt() << t);
                    return;
                }
                case ISHR: {
                    int t = frame.popInt();
                    frame.pushInt(frame.popInt() >> t);
                    return;
                }
                case IUSHR: {
                    int t = frame.popInt();
                    frame.pushInt(frame.popInt() >>> t);
                    return;
                }
                case IAND: {
                    frame.pushInt(frame.popInt() & frame.popInt());
                    return;
                }
                case IOR: {
                    frame.pushInt(frame.popInt() | frame.popInt());
                    return;
                }
                case IXOR: {
                    frame.pushInt(frame.popInt() ^ frame.popInt());
                    return;
                }
                case LADD: {
                    frame.pushLong(frame.popLong() + frame.popLong());
                    return;
                }
                case LSUB: {
                    frame.pushLong(-frame.popLong() + frame.popLong());
                    return;
                }
                case LMUL: {
                    frame.pushLong(frame.popLong() * frame.popLong());
                    return;
                }
                case LDIV: {
                    long t = frame.popLong();
                    frame.pushLong(frame.popLong() / t);
                    return;
                }
                case LREM: {
                    long t = frame.popLong();
                    frame.pushLong(frame.popLong() % t);
                    return;
                }
                case LNEG: {
                    frame.pushLong(-frame.popLong());
                    return;
                }
                case LSHL: {
                    long t = frame.popLong();
                    frame.pushLong(frame.popLong() << t);
                    return;
                }
                case LSHR: {
                    long t = frame.popLong();
                    frame.pushLong(frame.popLong() >> t);
                    return;
                }
                case LUSHR: {
                    long t = frame.popLong();
                    frame.pushLong(frame.popLong() >>> t);
                    return;
                }
                case LAND: {
                    frame.pushLong(frame.popLong() & frame.popLong());
                    return;
                }
                case LOR: {
                    frame.pushLong(frame.popLong() | frame.popLong());
                    return;
                }
                case LXOR: {
                    frame.pushLong(frame.popLong() ^ frame.popLong());
                    return;
                }
                case FADD: {
                    frame.pushFloat(frame.popFloat() + frame.popFloat());
                    return;
                }
                case FSUB: {
                    frame.pushFloat(-frame.popFloat() + frame.popFloat());
                    return;
                }
                case FMUL: {
                    frame.pushFloat(frame.popFloat() * frame.popFloat());
                    return;
                }
                case FDIV: {
                    float t = frame.popFloat();
                    frame.pushFloat(frame.popFloat() / t);
                    return;
                }
                case FREM: {
                    float t = frame.popFloat();
                    frame.pushFloat(frame.popFloat() % t);
                    return;
                }
                case FNEG: {
                    frame.pushFloat(-frame.popFloat());
                    return;
                }
                case DADD: {
                    frame.pushDouble(frame.popDouble() + frame.popDouble());
                    return;
                }
                case DSUB: {
                    frame.pushDouble(-frame.popDouble() + frame.popDouble());
                    return;
                }
                case DMUL: {
                    frame.pushDouble(frame.popDouble() * frame.popDouble());
                    return;
                }
                case DDIV: {
                    double t = frame.popDouble();
                    frame.pushDouble(frame.popDouble() / t);
                    return;
                }
                case DREM: {
                    double t = frame.popDouble();
                    frame.pushDouble(frame.popDouble() % t);
                    return;
                }
                case DNEG: {
                    frame.pushDouble(-frame.popDouble());
                    return;
                }

                case I2L:
                    frame.pushLong((long) frame.popInt());
                    return;
                case I2F:
                    frame.pushFloat((float) frame.popInt());
                    return;
                case I2D:
                    frame.pushDouble((double) frame.popInt());
                    return;
                case L2I:
                    frame.pushInt((int) frame.popLong());
                    return;
                case L2F:
                    frame.pushFloat((float) frame.popLong());
                    return;
                case L2D:
                    frame.pushDouble((double) frame.popLong());
                    return;
                case F2I:
                    frame.pushInt((int) frame.popFloat());
                    return;
                case F2L:
                    frame.pushLong((long) frame.popFloat());
                    return;
                case F2D:
                    frame.pushDouble((double) frame.popFloat());
                    return;
                case D2I:
                    frame.pushInt((int) frame.popDouble());
                    return;
                case D2L:
                    frame.pushLong((long) frame.popDouble());
                    return;
                case D2F:
                    frame.pushFloat((float) frame.popDouble());
                    return;
                case I2B:
                    frame.pushInt((byte) frame.popInt());
                    return;
                case I2C:
                    frame.pushInt((char) frame.popInt());
                    return;
                case I2S:
                    frame.pushInt((short) frame.popInt());
                    return;

                case LCMP:
                    frame.pushInt(-Long.signum(frame.popLong() - frame.popLong()));
                    return;
                case FCMPL: {
                    float t1 = frame.popFloat();
                    float t2 = frame.popFloat();
                    if (t1 == t2) frame.pushInt(0);
                    else if (t1 > t2) frame.pushInt(1);
                    else frame.pushInt(-1);
                    return;
                }
                case FCMPG: {
                    float t1 = frame.popFloat();
                    float t2 = frame.popFloat();
                    if (t1 == t2) frame.pushInt(0);
                    else if (t1 < t2) frame.pushInt(-1);
                    else frame.pushInt(1);
                    return;
                }
                case DCMPL: {
                    double t1 = frame.popDouble();
                    double t2 = frame.popDouble();
                    if (t1 == t2) frame.pushInt(0);
                    else if (t1 > t2) frame.pushInt(1);
                    else frame.pushInt(-1);
                    return;
                }
                case DCMPG: {
                    double t1 = frame.popDouble();
                    double t2 = frame.popDouble();
                    if (t1 == t2) frame.pushInt(0);
                    else if (t1 < t2) frame.pushInt(-1);
                    else frame.pushInt(1);
                    return;
                }

                case ARRAYLENGTH:
                    frame.pushInt(Array.getLength(frame.popObject()));
                    return;

                case ATHROW:
                    throw (Throwable) frame.popObject();

                case MONITORENTER:
                    try {
                        Monitor.enter(frame.popObject());
                    } catch (UnsupportedOperationException e) {
                        throw new InternalError().initCause(e);
                    }
                    return;
                case MONITOREXIT:
                    try {
                        Monitor.exit(frame.popObject());
                    } catch (UnsupportedOperationException e) {
                        throw new InternalError().initCause(e);
                    }
                    return;

                default:
                    assert false;
            }
        }

        @Override
        public String toString() {
            return getOpcodeName(opcode);
        }
    }

    public static String getOpcodeName(int opcode) {
        for (Field field : Opcodes.class.getDeclaredFields()) {
            if (field.getType().getName().equals("int") &&
                    !field.getName().startsWith("ACC_") &&
                    !field.getName().startsWith("V1_") &&
                    !field.getName().startsWith("T_")) {
                try {
                    if (field.getInt(null) == opcode) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
        return "UNKNOWN(" + Integer.toHexString(opcode) + ")";
    }
}