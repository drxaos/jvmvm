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

import net.sf.jauvm.vm.*;
import net.sf.jauvm.vm.ref.ConstructorRef;
import net.sf.jauvm.vm.ref.MethodRef;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public abstract class MethodInsn extends Insn {
    public static Insn getInsn(int opcode, String owner, String name, String desc, Class<?> cls) {
        switch (opcode) {
            case INVOKEVIRTUAL:
                return new InvokeVirtualInsn(owner, name, desc, cls);
            case INVOKESTATIC:
                return new InvokeStaticInsn(owner, name, desc, cls);
            case INVOKEINTERFACE:
                return new InvokeInterfaceInsn(owner, name, desc, cls);
            case INVOKESPECIAL:
                if ("<init>".equals(name)) return new InvokeConstructorInsn(owner, desc, cls);
                else return new InvokeSpecialInsn(owner, name, desc, cls);
            default:
                assert false;
                return null;
        }
    }


    static final class InvokeVirtualInsn extends MethodInsn {
        private final MethodRef m;

        InvokeVirtualInsn(String owner, String name, String desc, Class<?> cls) {
            this.m = new MethodRef(owner, name, desc, cls, false, false);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Method method = m.get();
            method.setAccessible(true); // TODO check access

            Object target = frame.getTarget(method.getParameterTypes());
            MethodCode code = m.getCode(target.getClass());
            if (code != null) {
                method = m.get(target.getClass());
                Frame f = vm.getInsn() instanceof ReturnInsn && ((ReturnInsn) vm.getInsn()).canReturn(method.getReturnType()) ?
                        frame.newTailCallFrame(method, code) :
                        frame.newCallFrame(vm.getCp(), method, code);
                vm.setFrame(f);
                vm.setCp(0);
            } else {
                try {
                    Object[] params = frame.popParameters(method.getParameterTypes());
                    Object ret = method.invoke(frame.popObject(), params);
                    Class<?> type = method.getReturnType();
                    if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
                        frame.pushInt(ret);
                    } else if (type == long.class) {
                        frame.pushLong(ret);
                    } else if (type == float.class) {
                        frame.pushFloat(ret);
                    } else if (type == double.class) {
                        frame.pushDouble(ret);
                    } else if (type != void.class) {
                        frame.pushObject(ret);
                    }
                } catch (IllegalAccessException e) {
                    throw new InternalError().initCause(e);
                } catch (InvocationTargetException e) {
                    throw new StackTracedException(e.getCause());
                }
            }
        }
    }

    static final class InvokeStaticInsn extends MethodInsn {
        private final MethodRef m;

        InvokeStaticInsn(String owner, String name, String desc, Class<?> cls) {
            this.m = new MethodRef(owner, name, desc, cls, true, false);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Method method = m.get();
            method.setAccessible(true); // TODO check access

            MethodCode code = m.getCode(method.getDeclaringClass());
            if (code != null) {
                Frame f = vm.getInsn() instanceof ReturnInsn && ((ReturnInsn) vm.getInsn()).canReturn(method.getReturnType()) ?
                        frame.newTailCallFrame(method, code) :
                        frame.newCallFrame(vm.getCp(), method, code);
                vm.setFrame(f);
                vm.setCp(0);
            } else {
                try {
                    Object[] params = frame.popParameters(method.getParameterTypes());
                    Object ret = method.invoke(null, params);
                    Class<?> type = method.getReturnType();
                    if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
                        frame.pushInt(ret);
                    } else if (type == long.class) {
                        frame.pushLong(ret);
                    } else if (type == float.class) {
                        frame.pushFloat(ret);
                    } else if (type == double.class) {
                        frame.pushDouble(ret);
                    } else if (type != void.class) {
                        frame.pushObject(ret);
                    }
                } catch (IllegalAccessException e) {
                    throw new InternalError().initCause(e);
                } catch (InvocationTargetException e) {
                    throw new StackTracedException(e.getCause());
                }
            }
        }
    }

    static final class InvokeInterfaceInsn extends MethodInsn {
        private final MethodRef m;

        InvokeInterfaceInsn(String owner, String name, String desc, Class<?> cls) {
            this.m = new MethodRef(owner, name, desc, cls, false, true);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Method method = m.get();
            method.setAccessible(true); // TODO check access

            Object target = frame.getTarget(method.getParameterTypes());

            if (!method.getDeclaringClass().isInstance(target))
                throw new IncompatibleClassChangeError(Types.getInternalName(method.getDeclaringClass()));

            MethodCode code = m.getCode(target.getClass());
            if (code != null) {
                method = m.get(target.getClass());
                Frame f = vm.getInsn() instanceof ReturnInsn && ((ReturnInsn) vm.getInsn()).canReturn(method.getReturnType()) ?
                        frame.newTailCallFrame(method, code) :
                        frame.newCallFrame(vm.getCp(), method, code);
                vm.setFrame(f);
                vm.setCp(0);
            } else {
                try {
                    Object[] params = frame.popParameters(method.getParameterTypes());
                    Object ret = method.invoke(frame.popObject(), params);
                    Class<?> type = method.getReturnType();
                    if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
                        frame.pushInt(ret);
                    } else if (type == long.class) {
                        frame.pushLong(ret);
                    } else if (type == float.class) {
                        frame.pushFloat(ret);
                    } else if (type == double.class) {
                        frame.pushDouble(ret);
                    } else if (type != void.class) {
                        frame.pushObject(ret);
                    }
                } catch (IllegalAccessException e) {
                    throw new InternalError().initCause(e);
                } catch (InvocationTargetException e) {
                    throw new StackTracedException(e.getCause());
                }
            }
        }
    }

    static final class InvokeSpecialInsn extends MethodInsn {
        private final MethodRef m;

        InvokeSpecialInsn(String owner, String name, String desc, Class<?> cls) {
            this.m = new MethodRef(owner, name, desc, cls, false, false);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();
            Method method = m.get();
            method.setAccessible(true); // TODO check access
//            if (!Modifier.isPrivate(method.getModifiers()))
//                throw new InternalError();

            MethodCode code = m.getCode(method.getDeclaringClass());
            if (code != null) {
                Frame f = vm.getInsn() instanceof ReturnInsn && ((ReturnInsn) vm.getInsn()).canReturn(method.getReturnType()) ?
                        frame.newTailCallFrame(method, code) :
                        frame.newCallFrame(vm.getCp(), method, code);
                vm.setFrame(f);
                vm.setCp(0);
            } else {
                try {
                    Object[] params = frame.popParameters(method.getParameterTypes());
                    Object ret = method.invoke(frame.popObject(), params);
                    Class<?> type = method.getReturnType();
                    if (type == int.class || type == byte.class || type == short.class || type == char.class || type == boolean.class) {
                        frame.pushInt(ret);
                    } else if (type == long.class) {
                        frame.pushLong(ret);
                    } else if (type == float.class) {
                        frame.pushFloat(ret);
                    } else if (type == double.class) {
                        frame.pushDouble(ret);
                    } else if (type != void.class) {
                        frame.pushObject(ret);
                    }
                } catch (IllegalAccessException e) {
                    throw new InternalError().initCause(e);
                } catch (InvocationTargetException e) {
                    throw new StackTracedException(e.getCause());
                }
            }
        }
    }

    static final class InvokeConstructorInsn extends MethodInsn {
        private final ConstructorRef c;

        InvokeConstructorInsn(String owner, String desc, Class<?> cls) {
            this.c = new ConstructorRef(owner, desc, cls);
        }

        public void execute(VirtualMachine vm) throws Throwable {
            Frame frame = vm.getFrame();

            Constructor<?> constructor = c.get();
            MethodCode code = GlobalCodeCache.get(constructor.getDeclaringClass(), "<init>" + c.getDescriptor());

            if (code != null) {
                Frame f = frame.newCallFrame(vm.getCp(), constructor, code);
                vm.setFrame(f);
                vm.setCp(0);

                if (constructor.getDeclaringClass().equals(Object.class)) {
                    // no more super constructors, initializing object
                    vm.getFrame().loadObject(0);
                    Object o = vm.getFrame().popObject();
                    if (o instanceof TypeInsn.LazyNewObject) {
                        ((TypeInsn.LazyNewObject) o).init(Object.class, vm);
                    }
                }

            } else {
                try {
                    TypeInsn.LazyNewObject lazyNewObject = (TypeInsn.LazyNewObject) frame.popObject();
                    Object[] params = frame.popParameters(constructor.getParameterTypes());
                    lazyNewObject.init(constructor.getDeclaringClass(), constructor.getParameterTypes(), params, vm);
                } catch (InstantiationException e) {
                    throw new InstantiationError(Types.getInternalName(constructor));
                } catch (IllegalAccessException e) {
                    throw new InternalError().initCause(e);
                } catch (InvocationTargetException e) {
                    throw new StackTracedException(e.getCause());
                }
            }
        }
    }
}
