package com.googlecode.jvmvm.compiler.ecj;

import com.googlecode.jvmvm.loader.ProjectCompilerException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class NameEnv implements INameEnvironment {

    private Map<String, String> files;
    private Map<String, byte[]> classpath;
    private Set<String> packagesCache;

    public NameEnv(Map<String, String> files, Map<String, byte[]> classpath) {
        this.files = files;
        this.classpath = classpath;
        packagesCache = new HashSet<String>();
        Set<String> allFiles = new HashSet<String>();
        allFiles.addAll(files.keySet());
        allFiles.addAll(classpath.keySet());
        for (String path : allFiles) {
            while (!path.isEmpty()) {
                path = FilenameUtils.getPath(path).replaceFirst("/$", "");
                String pkg = path.replace("/", ".");
                if (packagesCache.contains(pkg)) {
                    break;
                } else {
                    packagesCache.add(pkg);
                }
            }
        }
    }

    @Override
    public NameEnvironmentAnswer findType(final char[][] compoundTypeName) {
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < compoundTypeName.length; i++) {
            if (i != 0) {
                result.append('.');
            }
            result.append(compoundTypeName[i]);
        }
        return findType(result.toString());
    }

    @Override
    public NameEnvironmentAnswer findType(final char[] typeName, final char[][] packageName) {
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < packageName.length; i++) {
            result.append(packageName[i]);
            result.append('.');
        }
        result.append(typeName);
        return findType(result.toString());
    }

    private NameEnvironmentAnswer findType(final String name) {
        try {
            byte[] bytecode = classpath.get(name.replace(".", "/") + ".class");
            String source = files.get(name.replace(".", "/") + ".java");
            if (bytecode != null) {
                char[] fileName = name.toCharArray();
                ClassFileReader classFileReader = new ClassFileReader(bytecode, fileName, true);
                return new NameEnvironmentAnswer(classFileReader, null);
            } else if (source != null) {
                ICompilationUnit compilationUnit = new CompilationUnit(source.toCharArray(), name, "UTF-8");
                return new NameEnvironmentAnswer(compilationUnit, null);
            }
            return null;
        } catch (ClassFormatException e) {
            // Something very very bad
            throw new ProjectCompilerException("compiler error", e);
        }
    }

    @Override
    public boolean isPackage(char[][] parentPackageName, char[] packageName) {
        // Rebuild something usable
        StringBuilder sb = new StringBuilder();
        if (parentPackageName != null) {
            for (char[] p : parentPackageName) {
                sb.append(new String(p));
                sb.append(".");
            }
        }
        sb.append(new String(packageName));
        String name = sb.toString();
        return packagesCache.contains(name);
    }

    @Override
    public void cleanup() {
    }
}