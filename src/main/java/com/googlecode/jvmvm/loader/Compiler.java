package com.googlecode.jvmvm.loader;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
    String projectName;
    Map<String, String> files = new HashMap<String, String>();
    DiagnosticCollector<JavaFileObject> diagnostics;
    ClassFileManager fileManager;

    public Compiler(String projectName) {
        this.projectName = projectName;
    }

    public Compiler addFile(String fileName, String contents) {
        files.put(fileName, contents);
        return this;
    }

    public Compiler compile() throws ProjectCompilerException {
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
        return this;
    }

    public MemoryClassLoader getClassLoader() {
        return fileManager.getClassLoader(null);
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics.getDiagnostics();
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
