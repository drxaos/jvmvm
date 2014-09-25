package com.googlecode.jvmvm.loader;

public class ProjectLoaderException extends Exception {
    public ProjectLoaderException() {
    }

    public ProjectLoaderException(String message) {
        super(message);
    }

    public ProjectLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectLoaderException(Throwable cause) {
        super(cause);
    }
}
