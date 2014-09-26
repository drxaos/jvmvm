package com.googlecode.jvmvm.loader;

import org.objectweb.asm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.SecureClassLoader;
import java.util.*;


class Modifier extends ClassAdapter {

    public Modifier(ClassVisitor cv) {
        super(cv);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) != 0) {
            return null;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_STATIC) != 0 && name.equals("<clinit>")) {
            return null;
        }
        access &= ~Opcodes.ACC_TRANSIENT;
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String serializable = Serializable.class.getCanonicalName().replace(".", "/");
        List<String> intf = new ArrayList<String>(Arrays.asList(interfaces));
        if (!intf.contains(serializable)) {
            intf.add(serializable);
            interfaces = intf.toArray(new String[intf.size()]);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }
}

public class MemoryClassLoader extends SecureClassLoader {
    static class Classes extends HashMap<String, byte[]> implements Serializable {
    }

    Classes classes = new Classes();

    MemoryClassLoader(Classes classes) {
        super(null);
        this.classes = classes;
    }

    MemoryClassLoader(Map<String, JavaClassObject> jclassObjectMap) {
        super(null);
        for (Map.Entry<String, JavaClassObject> e : jclassObjectMap.entrySet()) {

            byte[] bytes = e.getValue().getBytes();
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            Modifier mcw = new Modifier(cw);
            cr.accept(mcw, 0);
            bytes = cw.toByteArray();

            classes.put(e.getKey(), bytes);
        }

    }

    MemoryClassLoader addClass(String className, byte[] bytecode) {
        classes.put(className, bytecode);
        return this;
    }

    MemoryClassLoader addSystemClass(String className) {
        try {
            super.loadClass(className, false);
            classes.put(className, new byte[0]);
        } catch (ClassNotFoundException e) {
            // don't add
        }
        return this;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (classes.containsKey(name)) {

            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class c = findLoadedClass(name);
                if (c == null) {
                    long t0 = System.nanoTime();

                    if (classes.get(name).length == 0) {
                        try {
                            c = super.loadClass(name, resolve);
                        } catch (ClassNotFoundException e) {
                            // ClassNotFoundException thrown if class not found
                            // from the non-null parent class loader
                        }
                    }

                    if (c == null) {
                        // If still not found, then invoke findClass in order
                        // to find the class.
                        long t1 = System.nanoTime();
                        c = findClass(name);

                        // this is the defining class loader; record the stats
                        sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                        sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                        sun.misc.PerfCounter.getFindClasses().increment();
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = classes.get(name);
        return super.defineClass(name, b, 0, b.length);
    }

    public InputStream getBytecodeStream(Class cls) {
        if (classes.containsKey(cls.getCanonicalName()) && classes.get(cls.getCanonicalName()).length != 0) {
            return new ByteArrayInputStream(classes.get(cls.getCanonicalName()));
        } else {
            return null;
        }
    }
}
