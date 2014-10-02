package com.googlecode.jvmvm.compiler.javac;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.ProjectCompilerException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaCompiler implements com.googlecode.jvmvm.compiler.Compiler {
    public MemoryClassLoader compile(Map<String, String> files) throws ProjectCompilerException {
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            jfiles.add(new CharSequenceJavaFileObject(entry.getKey(), entry.getValue()));
        }
        Boolean res = compiler.getTask(null, fileManager, diagnostics, null, null, jfiles).call();
        if (!res) {
            throw new ProjectCompilerException(diagnostics.getDiagnostics());
        }
        return fileManager.getClassLoader(null);
    }
}

