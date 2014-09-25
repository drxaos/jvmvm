package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.tests.interpretable.LoaderB;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class LoaderTest {

    @Before
    public void setUp() {

    }

    @Test
    public void test_compile() throws Exception {
        String name = LoaderB.class.getCanonicalName().replace(".", "/") + ".java";
        MemoryClassLoader cl = new com.googlecode.jvmvm.loader.Compiler("loader-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .compile()
                .getClassLoader()
                .addBootstrapClass("java.lang.Object")
                .addBootstrapClass("java.lang.String")
                .addBootstrapClass("java.lang.StringBuilder")
                .addBootstrapClass("java.io.Serializable")
                .addBootstrapClass("java.io.OutputStream")
                .addBootstrapClass("java.io.ByteArrayOutputStream")
                .addBootstrapClass("java.io.InputStream")
                .addBootstrapClass("java.io.ByteArrayInputStream")
                .addBootstrapClass("java.io.PrintStream");

        Class<?> cls = cl.loadClass(LoaderB.class.getCanonicalName());
        Object b = cls.newInstance();
        Object res = cls.getMethod("m").invoke(b);
        Assert.assertEquals("result", "IA;P;F;CA;IB;CB;", res);
    }
}
