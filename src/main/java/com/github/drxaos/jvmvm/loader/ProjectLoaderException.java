package com.github.drxaos.jvmvm.loader;

public class ProjectLoaderException extends RuntimeException {
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
