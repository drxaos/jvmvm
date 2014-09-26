package com.googlecode.jvmvm.loader;

import com.googlecode.jvmvm.vm.GlobalCodeLoader;
import com.googlecode.jvmvm.vm.VirtualMachine;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project implements Serializable {
    String projectName;
    Map<String, String> files = new HashMap<String, String>();
    List<String> systemClasses = new ArrayList<String>();
    boolean started = false;
    boolean vmDisabled = false;
    byte[] vmState;

    boolean shouldCompile = false;
    transient boolean compiled = false;

    transient DiagnosticCollector<JavaFileObject> diagnostics;
    transient ClassFileManager fileManager;
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

    public Project compile() throws ProjectCompilerException {
        if (compiled) {
            throw new ProjectException("already compiled");
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = new DiagnosticCollector<JavaFileObject>();
        fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            jfiles.add(new CharSequenceJavaFileObject(entry.getKey(), entry.getValue()));
        }
        Boolean res = compiler.getTask(null, fileManager, diagnostics, null, null, jfiles).call();
        if (!res) {
            throw new ProjectCompilerException(diagnostics.getDiagnostics());
        }
        this.classLoader = fileManager.getClassLoader(null);
        for (String bootstrapClass : systemClasses) {
            classLoader.addSystemClass(bootstrapClass);
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

    public Project addSystemClasses(List<String> list) throws ProjectCompilerException {
        for (String className : list) {
            addSystemClass(className);
        }
        return this;
    }

    public Project addSystemClass(String className) throws ProjectCompilerException {
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

    public Project startVM(String className, String methodName, Object self, Class[] paramTypes, Object[] paramValues) throws ProjectLoaderException {
        if (vmDisabled) {
            throw new ProjectLoaderException("vm disabled");
        }
        if (virtualMachine != null || vmState != null) {
            throw new ProjectLoaderException("already loaded");
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

    public Project step() throws ProjectExecutionException, ProjectStoppedException, ProjectLoaderException {
        if (vmState != null && virtualMachine == null) {
            try {
                virtualMachine = VirtualMachine.create(classLoader, vmState);
            } catch (Throwable throwable) {
                throw new ProjectLoaderException("vm load error", throwable);
            }
        }
        try {
            virtualMachine.step();
        } catch (Throwable throwable) {
            throw new ProjectExecutionException("program error", throwable);
        }
        if (!virtualMachine.isActive()) {
            throw new ProjectStoppedException(virtualMachine.getResult());
        }
        return this;
    }

    public Object getRet() {
        return virtualMachine.getResult();
    }
}

class CharSequenceJavaFileObject extends SimpleJavaFileObject {

    /**
     * CharSequence representing the source code to be compiled
     */
    private CharSequence content;

    /**
     * This constructor will store the source code in the
     * internal "content" variable and register it as a
     * source code, using a URI containing the class full name
     *
     * @param fileName name of the source code file
     * @param content  source code to compile
     */
    public CharSequenceJavaFileObject(String fileName, CharSequence content) {
        super(URI.create("string:///" + fileName), Kind.SOURCE);
        this.content = content;
    }

    /**
     * Answers the CharSequence to be compiled. It will give
     * the source code stored in variable "content"
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}

class JavaClassObject extends SimpleJavaFileObject {

    /**
     * Byte code created by the compiler will be stored in this
     * ByteArrayOutputStream so that we can later get the
     * byte array out of it
     * and put it in the memory as an instance of our class.
     */
    protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * Registers the compiled class object under URI
     * containing the class full name
     *
     * @param name Full name of the compiled class
     * @param kind Kind of the data. It will be CLASS in our case
     */
    public JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    /**
     * Will be used by our file manager to get the byte code that
     * can be put into memory to instantiate our class
     *
     * @return compiled byte code
     */
    public byte[] getBytes() {
        return bos.toByteArray();
    }

    /**
     * Will provide the compiler with an output stream that leads
     * to our byte array. This way the compiler will write everything
     * into the byte array that we will instantiate later
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        return bos;
    }
}

class ClassFileManager extends ForwardingJavaFileManager {
    Map<String, JavaClassObject> jclassObjectMap = new HashMap<String, JavaClassObject>();

    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    @Override
    public MemoryClassLoader getClassLoader(Location location) {
        return new MemoryClassLoader(jclassObjectMap);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaClassObject jclassObject = new JavaClassObject(className, kind);
        jclassObjectMap.put(className, jclassObject);
        return jclassObject;
    }
}
