package com.googlecode.jvmvm.compiler;

import com.googlecode.jvmvm.loader.ProjectCompilerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarUtil {

    public static Map<String, byte[]> unpack(byte[] jar) {
        Map<String, byte[]> classes = new HashMap<String, byte[]>();
        try {
            JarInputStream in = new JarInputStream(new ByteArrayInputStream(jar));
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null) {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int numRead;
                while ((numRead = in.read(buf)) >= 0) {
                    b.write(buf, 0, numRead);
                }
                classes.put(je.getName(), b.toByteArray());
            }
            in.close();
        } catch (IOException e) {
            throw new ProjectCompilerException("jar parse error", e);
        }
        return classes;
    }
}
