package com.github.drxaos.jvmvm.loader;

import com.github.drxaos.jvmvm.compiler.JarUtil;
import org.objectweb.asm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.SecureClassLoader;
import java.util.*;

class RemapMethodVisitor extends MethodVisitor {
    Map<String, String> remapping;

    public RemapMethodVisitor(MethodVisitor mv, Map<String, String> remapping) {
        super(Opcodes.ASM4, mv);
        this.remapping = remapping;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (remapping.keySet().contains(owner)) {
            mv.visitFieldInsn(opcode, remapping.get(owner), name, desc);
        } else {
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (remapping.keySet().contains(owner)) {
            mv.visitMethodInsn(opcode, remapping.get(owner), name, desc);
        } else {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void visitTypeInsn(int opcode, String type) {
        if (remapping.keySet().contains(type)) {
            mv.visitTypeInsn(opcode, remapping.get(type));
        } else {
            mv.visitTypeInsn(opcode, type);
        }
    }

    public void visitVarInsn(int opcode, int var) {
        mv.visitVarInsn(opcode, var);
    }
}

class Modifier extends ClassVisitor {
    Map<String, String> remapping;

    public Modifier(ClassVisitor cv, Map<String, String> remapping) {
        super(Opcodes.ASM4, cv);
        this.remapping = remapping;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_STATIC) != 0 && name.equals("<clinit>")) {
            name = "void";
        }
        access &= ~Opcodes.ACC_TRANSIENT;
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (null != mv) {
            return new RemapMethodVisitor(mv, remapping);
        }
        return mv;
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
    List<String> serviceClasses = Arrays.asList(
            "sun.reflect.SerializationConstructorAccessorImpl"
    );

    SystemClassesCallback systemClassesCallback = null;

    Map<String, byte[]> classes = new HashMap<String, byte[]>();
    Map<String, String> remapping = new HashMap<String, String>();
    boolean vmDisabled = false;
    Set<String> modifiedClasses = new HashSet<String>();
    ClassLoader fallbackClassLoader;

    public MemoryClassLoader(ClassLoader fallbackClassLoader, Map<String, byte[]> classes, List<byte[]> jars) {
        super(null);
        this.fallbackClassLoader = fallbackClassLoader;
        this.classes = classes;
        for (byte[] jar : jars) {
            for (Map.Entry<String, byte[]> e : JarUtil.unpack(jar).entrySet()) {
                classes.put(e.getKey().replaceFirst("\\.class$", "").replace("/", "."), e.getValue());
            }
        }
    }

    public void onVmDisabled() {
        vmDisabled = true;
    }

    MemoryClassLoader addClass(String className, byte[] bytecode) {
        classes.put(className, bytecode);
        return this;
    }

    MemoryClassLoader addSystemClass(String className) throws ClassNotFoundException {
        classes.put(className, new byte[0]);
        return this;
    }

    public MemoryClassLoader setSystemClassesCallback(SystemClassesCallback systemClassesCallback) {
        this.systemClassesCallback = systemClassesCallback;
        return this;
    }

    MemoryClassLoader addRemapping(String className, String toClassName) {
        remapping.put(className.replace(".", "/"), toClassName.replace(".", "/"));
        return this;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!classes.containsKey(name) && systemClassesCallback != null && systemClassesCallback.shouldResolve(name)) {
            // lazy resolving
            addSystemClass(name);
        }

        if (classes.containsKey(name)) {
            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class c = findLoadedClass(name);
                if (c == null) {
                    long t0 = System.nanoTime();

                    if (classes.get(name).length == 0) {
                        try {
                            c = fallbackClassLoader.loadClass(name);
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
            if (serviceClasses.contains(name)) {
                return super.loadClass(name, resolve);
            } else {
                throw new ClassNotFoundException(name);
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = classes.get(name);

        if (b.length == 0) {
            return super.findClass(name);
        }

        if (!vmDisabled && !modifiedClasses.contains(name)) {
            ClassReader cr = new ClassReader(b);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            Modifier mcw = new Modifier(cw, remapping);
            cr.accept(mcw, 0);
            b = cw.toByteArray();
            classes.put(name, b);
            modifiedClasses.add(name);
        }
        int i = name.lastIndexOf('.');
        String pkgname = name.substring(0, i);
        Package pkg = getPackage(pkgname);
        if (pkg == null) {
            definePackage(pkgname, null, null, null, null, null, null, null);
        }
        return super.defineClass(name, b, 0, b.length);
    }

    public InputStream getBytecodeStream(Class cls) {
        if (classes.containsKey(cls.getName()) && classes.get(cls.getName()).length != 0) {
            return new ByteArrayInputStream(classes.get(cls.getName()));
        } else {
            return null;
        }
    }
}
