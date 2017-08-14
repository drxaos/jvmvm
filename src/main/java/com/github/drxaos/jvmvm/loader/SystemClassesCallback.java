package com.github.drxaos.jvmvm.loader;

public interface SystemClassesCallback {
    boolean shouldResolve(String className);
}
