package com.googlecode.jvmvm.compiler.javac;

import com.googlecode.jvmvm.compiler.JarUtil;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import com.sun.tools.javac.api.JavacTool;
import org.apache.commons.io.IOUtils;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaCompiler implements com.googlecode.jvmvm.compiler.Compiler {
    public Map<String, byte[]> compile(Map<String, String> files, List<String> systemClasses, List<byte[]> jars) throws ProjectCompilerException {
        javax.tools.JavaCompiler compiler = JavacTool.create();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null), files, jars);
        fileManager.addSources(files);
        for (byte[] jar : jars) {
            for (Map.Entry<String, byte[]> e : JarUtil.unpack(jar).entrySet()) {
                fileManager.addClassInput(e.getKey(), e.getKey().replaceFirst("\\.class$", "").replace("/", "."), e.getValue());
            }
        }
        for (String systemClass : systemClasses) {
            try {
                String path = systemClass.replace(".", "/") + ".class";
                byte[] b = IOUtils.toByteArray(Class.forName(systemClass).getClassLoader().getResourceAsStream(path));
                fileManager.addClassInput(path, systemClass, b);
            } catch (Exception e) {
                // don't add
            }
        }
        Boolean res = compiler.getTask(null, fileManager, diagnostics, null, null, fileManager.getSourceObjects()).call();
        if (!res) {
            throw new ProjectCompilerException("compilation errors", diagnostics.getDiagnostics());
        }
        Map<String, FileObject> jclassObjectMap = fileManager.getJclassObjectMap();
        Map<String, byte[]> classes = new HashMap<String, byte[]>();
        for (Map.Entry<String, FileObject> e : jclassObjectMap.entrySet()) {
            byte[] bytes = ((MemoryJavaFile) e.getValue()).content;
            classes.put(((MemoryJavaFile) e.getValue()).getClassName(), bytes);
        }
        for (JavaFileObject cls : fileManager.getClassObjects()) {
            byte[] bytes = ((MemoryJavaFile) cls).content;
            classes.put(((MemoryJavaFile) cls).getClassName(), bytes);
        }
        return classes;
    }

}

