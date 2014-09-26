package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectStoppedException;
import com.googlecode.jvmvm.tests.interpretable.InheritanceA;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class StaticsTest {

    @Before
    public void setUp() {

    }

    List<String> bootstrap = Arrays.asList(
            "java.lang.Object",
            "java.lang.String",
            "java.lang.StringBuilder",
            "java.lang.StackTraceElement",
            "java.lang.Throwable",
            "java.lang.Exception",
            "java.lang.RuntimeException",
            "java.io.Serializable",
            "sun.reflect.SerializationConstructorAccessorImpl"
    );

    @Test
    public void test_compile() throws Exception {
        String name = InheritanceA.class.getCanonicalName().replace(".", "/") + ".java";

        MemoryClassLoader cl = new Project("compiler-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .getClassLoader();

        Class<?> cls = cl.loadClass(InheritanceA.class.getCanonicalName());
        cls.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{null});
        Assert.assertEquals("result", "SA;SB;IA;CA;IB;CB;", cls.getField("out").get(null).toString());
    }

    @Test
    public void test_save_load() throws Exception {
        String name = InheritanceA.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serialization-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile();

        byte[] serializedProject = project.saveToBytes();
        Project restoredProject = Project.fromBytes(serializedProject);

        Class<?> cls = restoredProject.getClassLoader().loadClass(InheritanceA.class.getCanonicalName());
        cls.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{null});
        Assert.assertEquals("result", "SA;SB;IA;CA;IB;CB;", cls.getField("out").get(null).toString());
    }

    @Test
    public void test_vm_save_load() throws Exception {
        String name = InheritanceA.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serialization-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .startVM(InheritanceA.class.getCanonicalName(), "ms", null, new Class[0], new Object[0]);

        try {
            int i = 0;
            while (true) {
                project.step();
                Assert.assertTrue(i++ < 1000000);

                byte[] serializedProject = project.saveToBytes();
                Project restoredProject = Project.fromBytes(serializedProject);
                try {
                    int j = 0;
                    while (true) {
                        restoredProject.step();
                        Assert.assertTrue(j++ < 1000000);
                    }
                } catch (ProjectStoppedException e) {
                    Assert.assertEquals("result", "IA;P;F;CA;IB;CB;BM;", e.getResult());
                }
            }
        } catch (ProjectStoppedException e) {
            Assert.assertEquals("result", "IA;P;F;CA;IB;CB;BM;", e.getResult());
        }
    }
}
