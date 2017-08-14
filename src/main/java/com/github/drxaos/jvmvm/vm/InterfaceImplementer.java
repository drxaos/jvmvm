package com.github.drxaos.jvmvm.vm;

import com.github.drxaos.jvmvm.loader.MemoryClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class InterfaceImplementer {

    Map<Class, Class> implementations = new HashMap<>();

    public Object implement(Class<?> intf) {

        if (implementations.containsKey(intf)) {
            try {
                return implementations.get(intf).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        String newClassName = intf.getName() + "_IMPL";
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(V1_6,
                ACC_PUBLIC | ACC_SUPER,
                newClassName.replace(".", "/"),
                null,
                "java/lang/Object",
                new String[]{
                        intf.getName().replace(".", "/"),
                        Serializable.class.getName().replace(".", "/")
                });

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();
        byte[] raw = cw.toByteArray();

        try {
            Class<?> implClass = ((MemoryClassLoader) intf.getClassLoader()).defineClass(newClassName, raw);
            implementations.put(intf, implClass);
            return implClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
