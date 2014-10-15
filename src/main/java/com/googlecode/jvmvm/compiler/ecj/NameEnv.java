package com.googlecode.jvmvm.compiler.ecj;

import com.googlecode.jvmvm.loader.ProjectCompilerException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class NameEnv implements INameEnvironment {

    private Map<String, String> files;
    private Map<String, byte[]> classpath;
    private Set<String> packagesCache;
    private FileSystem fileSystem;

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

        ArrayList<FileSystem.Classpath> cp = new ArrayList<FileSystem.Classpath>();
        ArrayList<String> cps = new ArrayList<String>();
        Util.collectRunningVMBootclasspath(cp);
        for (FileSystem.Classpath classpath1 : cp) {
            cps.add(classpath1.getPath());
        }
        fileSystem = new FileSystem(cps.toArray(new String[cps.size()]), null, null);
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

        if (result.toString().startsWith("java.")) {
            return fileSystem.findType(compoundTypeName);
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

        if (result.toString().startsWith("java.")) {
            return fileSystem.findType(typeName, packageName);
        }

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

        if (name.startsWith("java.")) {
            return fileSystem.isPackage(parentPackageName, packageName);
        }

        return packagesCache.contains(name);
    }

    @Override
    public void cleanup() {
    }
}