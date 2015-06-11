package com.github.drxaos.jvmvm.compiler.ecj;

import com.github.drxaos.jvmvm.loader.ProjectCompilerException;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.File;
import java.util.*;

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
        collectRunningVMBootclasspath(cp);
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

    public static void collectRunningVMBootclasspath(List bootclasspaths) {
        /* no bootclasspath specified
		 * we can try to retrieve the default librairies of the VM used to run
		 * the batch compiler
		 */
        String javaversion = System.getProperty("java.version");//$NON-NLS-1$
        if (javaversion != null && javaversion.equalsIgnoreCase("1.1.8")) { //$NON-NLS-1$
            throw new IllegalStateException();
        }

		/*
		 * Handle >= JDK 1.2.2 settings: retrieve the bootclasspath
		 */
        // check bootclasspath properties for Sun, JRockit and Harmony VMs
        String bootclasspathProperty = System.getProperty("sun.boot.class.path"); //$NON-NLS-1$
        if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
            // IBM J9 VMs
            bootclasspathProperty = System.getProperty("vm.boot.class.path"); //$NON-NLS-1$
            if ((bootclasspathProperty == null) || (bootclasspathProperty.length() == 0)) {
                // Harmony using IBM VME
                bootclasspathProperty = System.getProperty("org.apache.harmony.boot.class.path"); //$NON-NLS-1$
            }
        }
        if ((bootclasspathProperty != null) && (bootclasspathProperty.length() != 0)) {
            StringTokenizer tokenizer = new StringTokenizer(bootclasspathProperty, File.pathSeparator);
            String token;
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                FileSystem.Classpath currentClasspath = FileSystem.getClasspath(token, null, null);
                if (currentClasspath != null) {
                    bootclasspaths.add(currentClasspath);
                }
            }
        } else {
            // try to get all jars inside the lib folder of the java home
            final File javaHome = getJavaHome();
            if (javaHome != null) {
                File[] directoriesToCheck = null;
                if (System.getProperty("os.name").startsWith("Mac")) {//$NON-NLS-1$//$NON-NLS-2$
                    directoriesToCheck = new File[]{
                            new File(javaHome, "../Classes"), //$NON-NLS-1$
                    };
                } else {
                    // fall back to try to retrieve them out of the lib directory
                    directoriesToCheck = new File[]{
                            new File(javaHome, "lib") //$NON-NLS-1$
                    };
                }
                File[][] systemLibrariesJars = Main.getLibrariesFiles(directoriesToCheck);
                if (systemLibrariesJars != null) {
                    for (int i = 0, max = systemLibrariesJars.length; i < max; i++) {
                        File[] current = systemLibrariesJars[i];
                        if (current != null) {
                            for (int j = 0, max2 = current.length; j < max2; j++) {
                                FileSystem.Classpath classpath =
                                        FileSystem.getClasspath(current[j].getAbsolutePath(),
                                                null, false, null, null);
                                if (classpath != null) {
                                    bootclasspaths.add(classpath);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static File getJavaHome() {
        String javaHome = System.getProperty("java.home");//$NON-NLS-1$
        if (javaHome != null) {
            File javaHomeFile = new File(javaHome);
            if (javaHomeFile.exists()) {
                return javaHomeFile;
            }
        }
        return null;
    }
}