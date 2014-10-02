package com.googlecode.jvmvm.compiler.javac;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * Created by xaos on 10/2/14.
 */
class CharSequenceJavaFileObject extends SimpleJavaFileObject {

    /**
     * CharSequence representing the source code to be compiled
     */
    private CharSequence content;

    /**
     * This constructor will store the source code in the
     * internal "content" variable and register it as a
     * source code, using a URI containing the class full name
     *
     * @param fileName name of the source code file
     * @param content  source code to compile
     */
    public CharSequenceJavaFileObject(String fileName, CharSequence content) {
        super(URI.create("string:///" + fileName), Kind.SOURCE);
        this.content = content;
    }

    /**
     * Answers the CharSequence to be compiled. It will give
     * the source code stored in variable "content"
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
