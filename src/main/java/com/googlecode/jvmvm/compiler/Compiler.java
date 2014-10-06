package com.googlecode.jvmvm.compiler;

import com.googlecode.jvmvm.loader.ProjectCompilerException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Compiler extends Serializable {
    Map<String, byte[]> compile(Map<String, String> files, List<byte[]> jars) throws ProjectCompilerException;
}
