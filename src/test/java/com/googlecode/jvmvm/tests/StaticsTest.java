package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectStoppedException;
import com.googlecode.jvmvm.tests.interpretable.InheritanceA;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
            "java.io.Serializable"
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

        Project project = new Project("vm-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(InheritanceA.class.getCanonicalName(), "main", null, new Class[]{String[].class}, new Object[]{new String[0]});

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
                    Class<?> cls = restoredProject.getClassLoader().loadClass(InheritanceA.class.getCanonicalName());
                    Assert.assertEquals("result (i=" + i + ")", "SA;SB;IA;CA;IB;CB;", cls.getField("out").get(null).toString());
                    Assert.assertEquals("result (i=" + i + ")", "01623785", cls.getField("out2").get(null));
                }
            }
        } catch (ProjectStoppedException e) {
            Class<?> cls = project.getClassLoader().loadClass(InheritanceA.class.getCanonicalName());
            Assert.assertEquals("result", "SA;SB;IA;CA;IB;CB;", cls.getField("out").get(null).toString());
            Assert.assertEquals("result", "01623785", cls.getField("out2").get(null));
        }
    }

    @Test
    public void test_vm_restart() throws Exception {
        String name = InheritanceA.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("vm-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(InheritanceA.class.getCanonicalName(), "main", null, new Class[]{String[].class}, new Object[]{new String[0]});

        Object res1 = project.run();

        project.setupVM(InheritanceA.class.getCanonicalName(), "main", null, new Class[]{String[].class}, new Object[]{new String[0]});
        Object res2 = project.run();

        Assert.assertEquals("result1", "01623785", res1);
        Assert.assertEquals("result2", "0162378523785", res2);
    }
}
