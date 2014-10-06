package com.googlecode.jvmvm.loader;

public class ProjectCompilerException extends RuntimeException {
    Object data;

    public ProjectCompilerException(String message, Throwable cause, Object data) {
        super(message, cause);
        this.data = data;
    }

    public ProjectCompilerException(String message, Object data) {
        super(message + ": " + data);
        this.data = data;
    }

    public ProjectCompilerException(String message) {
        super(message);
    }

    public ProjectCompilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
