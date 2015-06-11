package com.github.drxaos.jvmvm.vm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvokeStaticInitializer {
    public static boolean shouldClinit(VirtualMachine vm, Class cls) {
        return !vm.isClinited(cls);
    }

    public static void invoke(VirtualMachine vm, Class cls) {
        if (vm.isClinited(cls)) {
            return;
        }
        try {
            Frame frame = vm.getFrame();
            Method method = cls.getDeclaredMethod("void", new Class[0]);
            method.setAccessible(true); // TODO check access

            MethodCode code = GlobalCodeLoader.get(cls, "void()V");
            if (code != null) {
                Frame f = vm.inTailPosition(method.getReturnType()) ?
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
        } catch (NoSuchMethodException e) {
            // don't invoke
        } catch (StackTracedException e) {
            throw new RuntimeException(e);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        vm.onClinited(cls);
    }
}
