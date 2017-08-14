package com.github.drxaos.jvmvm.loader;

import com.github.drxaos.jvmvm.compiler.Compiler;
import com.github.drxaos.jvmvm.compiler.javac.JavaCompiler;
import com.github.drxaos.jvmvm.vm.GlobalCodeLoader;
import com.github.drxaos.jvmvm.vm.MethodCode;
import com.github.drxaos.jvmvm.vm.VirtualMachine;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Project implements Serializable {
    String projectName;
    Map<String, String> files = new HashMap<String, String>();
    List<byte[]> jars = new ArrayList<byte[]>();
    List<String> systemClasses = new ArrayList<String>();
    SystemClassesCallback systemClassesCallback = null;
    Map<String, String> remapping = new HashMap<String, String>();
    Compiler compiler = new JavaCompiler();
    boolean started = false;
    boolean vmDisabled = false;
    byte[] vmState;

    boolean shouldCompile = false;
    transient boolean compiled = false;

    Map<String, Object> marks = new HashMap<String, Object>();

    transient MemoryClassLoader classLoader;
    transient VirtualMachine virtualMachine;

    public static Project fromBytes(byte[] data) throws ProjectLoaderException {
        return fromInputStream(new ByteArrayInputStream(data));
    }

    public static Project fromInputStream(InputStream in) throws ProjectLoaderException {
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            return fromInputStream(ois);
        } catch (IOException e) {
            throw new ProjectLoaderException("cannot read project", e);
        }
    }

    public static Project fromInputStream(ObjectInputStream in) throws ProjectLoaderException {
        try {
            return (Project) in.readObject();
        } catch (IOException e) {
            throw new ProjectLoaderException("cannot read project", e);
        } catch (ClassNotFoundException e) {
            throw new ProjectLoaderException("cannot read project", e);
        }
    }

    public Project(String projectName) {
        this.projectName = projectName;
    }

    public Project addJar(String fileName) throws IOException {
        return addJar(FileUtils.readFileToByteArray(new File(fileName)));
    }

    public Project addJar(byte[] contents) {
        jars.add(contents);
        return this;
    }

    public Project addFile(String fileName, String contents) {
        files.put(fileName, contents);
        return this;
    }

    public Project addFiles(Map<String, String> filenamesWithContents) {
        for (Map.Entry<String, String> e : filenamesWithContents.entrySet()) {
            addFile(e.getKey(), e.getValue());
        }
        return this;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public Project compile() throws ProjectCompilerException {
        if (compiled) {
            throw new ProjectException("already compiled");
        }
        Map<String, byte[]> classes = compiler.compile(files, systemClasses, jars);
        ClassLoader fallbackClassLoader = this.getClass().getClassLoader();
        classLoader = new MemoryClassLoader(fallbackClassLoader, classes, jars);
        for (String bootstrapClass : systemClasses) {
            try {
                classLoader.addSystemClass(bootstrapClass);
            } catch (ClassNotFoundException e) {
                throw new ProjectCompilerException("cannot load system class", e);
            }
        }
        classLoader.setSystemClassesCallback(systemClassesCallback);
        for (Map.Entry<String, String> remapClass : remapping.entrySet()) {
            classLoader.addRemapping(remapClass.getKey(), remapClass.getValue());
        }
        compiled = true;
        shouldCompile = true;
        return this;
    }

    public byte[] compileJar() throws IOException {
        if (compiled) {
            throw new ProjectException("already compiled");
        }
        Map<String, byte[]> classes = compiler.compile(files, systemClasses, jars);
        compiled = true;
        shouldCompile = false;

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        JarOutputStream jar = new JarOutputStream(b, new Manifest());
        for (Map.Entry<String, byte[]> e : classes.entrySet()) {
            JarEntry jarAdd = new JarEntry(e.getKey().replace(".", "/") + ".class");
            jarAdd.setTime(System.currentTimeMillis());
            jar.putNextEntry(jarAdd);
            jar.write(e.getValue());
        }
        jar.close();

        return b.toByteArray();
    }

    public MemoryClassLoader getClassLoader() {
        if (!compiled) {
            throw new ProjectException("not compiled yet");
        }
        vmDisabled = true;
        classLoader.onVmDisabled();
        return classLoader;
    }

    public Project addSystemClasses(Collection<String> list) throws ClassNotFoundException {
        for (String className : list) {
            addSystemClass(className);
        }
        return this;
    }

    public Project addSystemClass(String className) throws ClassNotFoundException {
        if (className == null || className.isEmpty()) {
            return this;
        }
        systemClasses.add(className);
        if (classLoader != null) {
            classLoader.addSystemClass(className);
        }
        return this;
    }

    public Project setSystemClassesCallback(SystemClassesCallback callback) {
        this.systemClassesCallback = callback;
        if (classLoader != null) {
            classLoader.setSystemClassesCallback(callback);
        }
        return this;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, ProjectCompilerException {
        in.defaultReadObject();
        if (shouldCompile) {
            compile();
        }
    }

    public void saveToStream(ObjectOutputStream out) throws IOException {
        if (virtualMachine != null) {
            vmState = virtualMachine.serializeToBytes();
        }
        out.writeObject(this);
    }

    public void saveToStream(OutputStream out) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        saveToStream(objectOutputStream);
    }

    public byte[] saveToBytes() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        saveToStream(b);
        return b.toByteArray();
    }

    public Project setupVM(String className, String methodName) throws ProjectLoaderException {
        return setupVM(className, methodName, null, new Class[0], new Object[0]);
    }

    public Project setupVM(String className, String methodName, Object self, Class[] paramTypes, Object[] paramValues) throws ProjectLoaderException {
        if (vmDisabled) {
            throw new ProjectLoaderException("vm disabled");
        }
        for (int i = 0; i < paramValues.length; i++) {
            if (paramValues[i] instanceof Marker) {
                paramValues[i] = findMarkedObject(((Marker) paramValues[i]).name);
            }
        }
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        } else if (virtualMachine != null) {
            try {
                virtualMachine = VirtualMachine.restart(virtualMachine, className, methodName, self, paramTypes, paramValues);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm start error", throwable);
            }
        } else {
            try {
                virtualMachine = VirtualMachine.create(classLoader, className, methodName, self, paramTypes, paramValues);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm start error", throwable);
            }
        }
        if (marks != null) {
            for (Map.Entry<String, Object> e : marks.entrySet()) {
                virtualMachine.setMark(e.getKey(), e.getValue());
            }
            marks = null;
        }
        started = true;
        return this;
    }

    public Map<String, MethodCode> getMethods(String className) throws ClassNotFoundException {
        return GlobalCodeLoader.getAll(classLoader.loadClass(className));
    }

    public Object run() throws ProjectExecutionException, ProjectLoaderException, BreakpointException {
        return run(-1);
    }

    public Object run(long timeout) throws ProjectExecutionException, ProjectLoaderException, BreakpointException {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        try {
            return virtualMachine.run(timeout);
        } catch (BreakpointException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new ProjectExecutionException("program error", throwable, virtualMachine.getPointer());
        }
    }

    public void step() throws ProjectExecutionException, ProjectLoaderException {
        step(true);
    }

    public void step(boolean autoSerializationCheck) throws ProjectExecutionException, ProjectLoaderException {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        try {
            virtualMachine.step();
            if (autoSerializationCheck) {
                virtualMachine.checkSerialization();
            }
        } catch (Throwable throwable) {
            throw new ProjectExecutionException("program error", throwable, virtualMachine.getPointer());
        }
    }

    public boolean isActive() throws ProjectLoaderException {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        return virtualMachine.isActive();
    }

    public Project markObject(String name, Serializable obj) {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        if (virtualMachine != null) {
            virtualMachine.setMark(name, obj);
        } else {
            marks.put(name, obj);
        }
        return this;
    }

    public Object findMarkedObject(String name) {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        if (virtualMachine != null) {
            return virtualMachine.getMark(name);
        } else {
            return marks.get(name);
        }
    }

    public Object getResult() {
        return virtualMachine.getResult();
    }

    public Project remap(Map<String, String> remapping) {
        for (Map.Entry<String, String> e : remapping.entrySet()) {
            remap(e.getKey(), e.getValue());
        }
        return this;
    }

    public Project remap(String fromClass, String toClass) {
        remapping.put(fromClass, toClass);
        if (classLoader != null) {
            classLoader.addRemapping(fromClass, toClass);
        }
        return this;
    }

    public static class Marker {
        String name;

        public Marker(String name) {
            this.name = name;
        }

        public static Marker byName(String name) {
            return new Marker(name);
        }
    }

    public void setBreakpoint(String clazz, String method) {
        virtualMachine.setBreakpoint(clazz, method);
    }

    public void setBreakpoint(String clazz, Integer line) {
        virtualMachine.setBreakpoint(clazz, line);
    }

    public void removeBreakpoint(String clazz, String method) {
        virtualMachine.removeBreakpoint(clazz, method);
    }

    public void removeBreakpoint(String clazz, Integer line) {
        virtualMachine.removeBreakpoint(clazz, line);
    }

    public void removeBreakpoint(String clazz, String method, Integer line) {
        virtualMachine.removeBreakpoint(clazz, method, line);
    }

    public void clearBreakpoints() {
        virtualMachine.clearBreakpoints();
    }

}
