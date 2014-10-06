package com.googlecode.jvmvm.compiler.javac;

import javax.tools.FileObject;
import javax.tools.StandardJavaFileManager;
import java.util.List;
import java.util.Map;

class ClassFileManager extends MemoryFileManager {


    Map<String, String> sources;
    List<byte[]> jars;

    public ClassFileManager(StandardJavaFileManager standardManager, Map<String, String> sources, List<byte[]> jars) {
        super(standardManager);
        this.sources = sources;
        this.jars = jars;


    }

//    @Override
//    public Iterable<JavaFileObject> list(Location location, String packageName, Set set, boolean recurse) throws IOException {
//        Iterable<JavaFileObject> list = fileManager.list(location, packageName, set, recurse);
//        ArrayList<JavaFileObject> res = new ArrayList<JavaFileObject>();
//        for (JavaFileObject javaFileObject : list) {
//            if (!javaFileObject.getName().isEmpty()) {
//                res.add(javaFileObject);
//            }
//        }
//        return res;
//    }
//
//    @Override
//    public boolean hasLocation(Location location) {
//        return super.hasLocation(location);
//    }
//
//    @Override
//    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
//        return super.getJavaFileForInput(location, className, kind);
//    }
//
//    @Override
//    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
//        return super.getFileForInput(location, packageName, relativeName);
//    }
//
//    @Override
//    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
//        JavaClassObject jclassObject = new JavaClassObject(className, kind);
//        jclassObjectMap.put(className, jclassObject);
//        return jclassObject;
//    }

    public Map<String, FileObject> getJclassObjectMap() {
        return outputs;
    }

    public void addSources(Map<String, String> files) {
        for (Map.Entry<String, String> e : files.entrySet()) {
            addSourceInput(e.getKey(), e.getKey().replaceFirst("\\.java$", "").replace("/", "."), e.getValue());
        }
    }
}
