package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectStoppedException;
import com.googlecode.jvmvm.tests.interpretable.LoaderB;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class LoaderTest {

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
        String name = LoaderB.class.getCanonicalName().replace(".", "/") + ".java";

        MemoryClassLoader cl = new Project("compiler-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .getClassLoader();

        Class<?> cls = cl.loadClass(LoaderB.class.getCanonicalName());
        Object b = cls.newInstance();
        Object res = cls.getMethod("m").invoke(b);
        Assert.assertEquals("result", "IA;P;F;CA;IB;CB;BM;", res);
    }

    @Test
    public void test_save_load() throws Exception {
        String name = LoaderB.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serialization-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(project);
        byte[] serializedProject = baos.toByteArray();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedProject));
        Project restoredProject = (Project) ois.readObject();

        Class<?> cls = restoredProject.getClassLoader().loadClass(LoaderB.class.getCanonicalName());
        Object b = cls.newInstance();
        Object res = cls.getMethod("m").invoke(b);
        Assert.assertEquals("result", "IA;P;F;CA;IB;CB;BM;", res);
    }

    @Test
    public void test_vm_save_load() throws Exception {
        String name = LoaderB.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("vm-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(LoaderB.class.getCanonicalName(), "ms", null, new Class[0], new Object[0]);

        try {
            int i = 0;
            while (project.isActive()) {
                project.step();
                Assert.assertTrue(i++ < 1000000);

                byte[] serializedProject = project.saveToBytes();
                Project restoredProject = Project.fromBytes(serializedProject);
                try {
                    int j = 0;
                    while (restoredProject.isActive()) {
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
