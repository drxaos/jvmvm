package com.googlecode.jvmvm.loader;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

public class ProjectCompilerException extends Exception {
    List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public ProjectCompilerException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }
}
