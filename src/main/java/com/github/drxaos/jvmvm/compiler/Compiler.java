package com.github.drxaos.jvmvm.compiler;

import com.github.drxaos.jvmvm.loader.ProjectCompilerException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Compiler extends Serializable {
    Map<String, byte[]> compile(Map<String, String> files, List<String> systemClasses, List<byte[]> jars) throws ProjectCompilerException;
}
