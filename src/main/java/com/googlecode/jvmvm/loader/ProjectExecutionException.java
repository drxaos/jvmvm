package com.googlecode.jvmvm.loader;

public class ProjectExecutionException extends RuntimeException {

    public ProjectExecutionException(String message, Throwable cause, StackTraceElement pointer) {
        super(message + " at " + pointer, cause);
    }

}
