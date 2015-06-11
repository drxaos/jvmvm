/*
 Copyright (c) 2009-2013 Olivier Chafik, All Rights Reserved
	
 This file is part of JNAerator (http://jnaerator.googlecode.com/).
	
 JNAerator is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
	
 JNAerator is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
	
 You should have received a copy of the GNU General Public License
 along with JNAerator.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.drxaos.jvmvm.compiler.javac;

import org.apache.commons.io.FilenameUtils;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    public final Map<String, MemoryFileObject> classpath = new HashMap<String, MemoryFileObject>();
    public final Map<String, MemoryJavaFile> inputs = new HashMap<String, MemoryJavaFile>();
    public final Map<String, FileObject> outputs = new HashMap<String, FileObject>();


    public MemoryFileManager(JavaFileManager fm) {
        super(fm);
    }

    public MemoryJavaFile addClassInput(String path, String className, byte[] content) {
        if (!path.startsWith("file:///")) {
            path = "file:///" + path;
        }

        MemoryJavaFile mjf = new MemoryJavaFile(path, className, content, JavaFileObject.Kind.CLASS);
        inputs.put(path, mjf);
        return mjf;
    }

    public MemoryJavaFile addSourceInput(String path, String className, String content) {
        if (!path.startsWith("file:///")) {
            path = "file:///" + path;
        }

        MemoryJavaFile mjf = new MemoryJavaFile(path, className, content, JavaFileObject.Kind.SOURCE);
        inputs.put(path, mjf);
        return mjf;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (packageName.startsWith("java.")) {
            return super.list(location, packageName, kinds, recurse);
        }
        ArrayList<JavaFileObject> list = new ArrayList<JavaFileObject>();
        for (Map.Entry<String, MemoryJavaFile> e : inputs.entrySet()) {
            try {
                MemoryJavaFile f = e.getValue();
                String p = new URI(f.getPath()).getPath();
                String path = FilenameUtils.getPath(p);
                String name = FilenameUtils.getBaseName(p);
                String ext = FilenameUtils.getExtension(p);
                if (kinds.contains(f.getKind()) && (packageName.replace(".", "/") + "/").equals(path)) {
                    list.add(f);
                }
            } catch (URISyntaxException e1) {
                throw new RuntimeException(e1);
            }
        }
        return list;
    }

    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof MemoryJavaFile) {
            file.getClass(); // null check
            location.getClass(); // null check
            return ((MemoryJavaFile) file).getClassName();
        } else {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toString().equals(b.toString());
    }

    public Iterable<? extends JavaFileObject> getJavaFileObjects() {
        return new ArrayList<JavaFileObject>(inputs.values());
    }

    public Iterable<? extends JavaFileObject> getSourceObjects() {
        ArrayList<JavaFileObject> list = new ArrayList<JavaFileObject>();
        for (MemoryJavaFile f : inputs.values()) {
            if (f.getKind() == JavaFileObject.Kind.SOURCE) {
                list.add(f);
            }
        }
        return list;
    }

    public Iterable<? extends JavaFileObject> getClassObjects() {
        ArrayList<JavaFileObject> list = new ArrayList<JavaFileObject>();
        for (MemoryJavaFile f : inputs.values()) {
            if (f.getKind() == JavaFileObject.Kind.CLASS) {
                list.add(f);
            }
        }
        return list;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
//		System.out.println("getJavaFileForInput(className = " + className + ", location = " + location + ", kind = " + kind + ")");
        if (kind == JavaFileObject.Kind.SOURCE) {
            return inputs.get(className);
        }
        return super.getJavaFileForInput(location, className, kind);
    }

    public static String getFullPathForClass(String className, String extension) {
        return "file:///" + getSimplePathForClass(className, extension);
    }

    public static String getSimplePathForClass(String className, String extension) {
        return className.replace('.', '/') + "." + extension;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
//		System.out.println("getJavaFileForOutput(className = " + className + ", location = " + location + ", kind = " + kind + ")");
        MemoryJavaFile jo = null;
        if (kind == JavaFileObject.Kind.CLASS) {
            outputs.put(getFullPathForClass(className, "class"), jo = new MemoryJavaFile(getFullPathForClass(className, "class"), className, (String) null, kind));
        } else if (kind == JavaFileObject.Kind.SOURCE) {
            inputs.put(getFullPathForClass(className, "java"), jo = new MemoryJavaFile(getFullPathForClass(className, "java"), className, (String) null, kind));
        }

        return jo == null ? super.getJavaFileForInput(location, className, kind) : jo;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
//		System.out.println("getFileForOutput(relativeName = " + relativeName + ")");
        if (relativeName.startsWith("file:///")) {
            relativeName = relativeName.substring("file:///".length());
        }

        FileObject out = outputs.get(relativeName);
        if (out == null) {
            out = new MemoryFileObject(relativeName, (String) null);
            outputs.put(relativeName, out);
        }
        return out;
    }
}