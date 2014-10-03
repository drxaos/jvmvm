package com.googlecode.jvmvm.compiler.javac;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ClassFileManager extends ForwardingJavaFileManager {
    Map<String, JavaClassObject> jclassObjectMap = new HashMap<String, JavaClassObject>();

    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaClassObject jclassObject = new JavaClassObject(className, kind);
        jclassObjectMap.put(className, jclassObject);
        return jclassObject;
    }

    public Map<String, JavaClassObject> getJclassObjectMap() {
        return jclassObjectMap;
    }
}
