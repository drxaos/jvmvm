package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectExecutionException;
import com.googlecode.jvmvm.tests.interpretable.HashMapExamples;
import com.googlecode.jvmvm.tests.interpretable.SystemExamples;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class SerializerTest {

    @Before
    public void setUp() {

    }

    List<String> bootstrap = Arrays.asList(
            Object.class.getName(),

            String.class.getName(),
            StringBuilder.class.getName(),
            CharSequence.class.getName(),

            Character.class.getName(),
            Boolean.class.getName(),
            Number.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Float.class.getName(),
            Double.class.getName(),

            Math.class.getName(),

            Iterable.class.getName(),
            Iterator.class.getName(),

            StackTraceElement.class.getName(),
            Throwable.class.getName(),
            Exception.class.getName(),
            RuntimeException.class.getName(),
            UnsupportedOperationException.class.getName(),
            NoSuchElementException.class.getName(),

            Serializable.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            List.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            Map.Entry.class.getName(),
            HashMap.class.getName(),
            "java.util.HashMap$EntrySet"
    );

    @Test
    public void test_vm_hashmap_entry() throws Exception {
        String src1 = HashMapExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("complex-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(HashMapExamples.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = HashMapExamples.test();
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test(expected = ProjectExecutionException.class)
    public void test_vm_system() throws Exception {
        String src1 = SystemExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("complex-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(SystemExamples.class.getCanonicalName(), "test");
        Object res = project.run();
    }
}
