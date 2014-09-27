package com.googlecode.jvmvm.loader;

public class ProjectExecutionException extends RuntimeException {
    public ProjectExecutionException() {
    }

    public ProjectExecutionException(String message) {
        super(message);
    }

    public ProjectExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectExecutionException(Throwable cause) {
        super(cause);
    }
}
