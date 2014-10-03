package com.googlecode.jvmvm.loader;

import com.googlecode.jvmvm.compiler.Compiler;
import com.googlecode.jvmvm.compiler.javac.JavaCompiler;
import com.googlecode.jvmvm.vm.GlobalCodeLoader;
import com.googlecode.jvmvm.vm.VirtualMachine;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project implements Serializable {
    String projectName;
    Map<String, String> files = new HashMap<String, String>();
    List<String> systemClasses = new ArrayList<String>();
    com.googlecode.jvmvm.compiler.Compiler compiler = new JavaCompiler();
    boolean started = false;
    boolean vmDisabled = false;
    byte[] vmState;

    boolean shouldCompile = false;
    transient boolean compiled = false;

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
        classLoader = new MemoryClassLoader(this.getClass().getClassLoader(), compiler.compile(files));
        for (String bootstrapClass : systemClasses) {
            try {
                classLoader.addSystemClass(bootstrapClass);
            } catch (ClassNotFoundException e) {
                throw new ProjectCompilerException("cannot load system class", e);
            }
        }
        compiled = true;
        shouldCompile = true;
        return this;
    }

    public MemoryClassLoader getClassLoader() {
        if (!compiled) {
            throw new ProjectException("not compiled yet");
        }
        vmDisabled = true;
        classLoader.onVmDisabled();
        return classLoader;
    }

    public Project addSystemClasses(List<String> list) throws ClassNotFoundException {
        for (String className : list) {
            addSystemClass(className);
        }
        return this;
    }

    public Project addSystemClass(String className) throws ClassNotFoundException {
        systemClasses.add(className);
        if (classLoader != null) {
            classLoader.addSystemClass(className);
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
        started = true;
        return this;
    }

    public Map<String, com.googlecode.jvmvm.vm.MethodCode> getMethods(String className) throws ClassNotFoundException {
        return GlobalCodeLoader.getAll(classLoader.loadClass(className));
    }

    public Object run() throws ProjectExecutionException, ProjectLoaderException {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        try {
            return virtualMachine.run();
        } catch (Throwable throwable) {
            throw new ProjectExecutionException("program error", throwable);
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
            throw new ProjectExecutionException("program error", throwable);
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

    public Object getResult() {
        return virtualMachine.getResult();
    }
}
