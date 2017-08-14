package com.github.drxaos.jvmvm.loader;

import java.io.Serializable;

public interface SystemClassesCallback extends Serializable {
    boolean shouldResolve(String className);
}
