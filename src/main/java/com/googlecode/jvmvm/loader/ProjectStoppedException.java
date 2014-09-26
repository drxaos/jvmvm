package com.googlecode.jvmvm.loader;

public class ProjectStoppedException extends Exception {
    Object result;

    public ProjectStoppedException(Object result) {
        super("program stopped");
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
