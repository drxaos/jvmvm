package com.github.drxaos.jvmvm.tests;

import com.github.drxaos.jvmvm.loader.Project;
import com.github.drxaos.jvmvm.loader.ProjectExecutionException;
import com.github.drxaos.jvmvm.tests.interpretable.CustomExamples;
import com.github.drxaos.jvmvm.tests.interpretable.MapExamples;
import com.github.drxaos.jvmvm.tests.interpretable.SystemExamples;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
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

            Charset.class.getName(),

            Math.class.getName(),

            Iterable.class.getName(),
            Iterator.class.getName(),
            Collection.class.getName(),

            StackTraceElement.class.getName(),
            Throwable.class.getName(),
            Exception.class.getName(),
            RuntimeException.class.getName(),
            UnsupportedOperationException.class.getName(),
            NoSuchElementException.class.getName(),
            UnsupportedEncodingException.class.getName(),
            IOException.class.getName(),
            MalformedURLException.class.getName(),
            CloneNotSupportedException.class.getName(),

            InputStream.class.getName(),

            Serializable.class.getName(),
            Comparator.class.getName(),
            Comparable.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            List.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            AbstractMap.class.getName(),
            SortedMap.class.getName(),
            HashMap.class.getName(),
            TreeMap.class.getName()
    );

    @Test
    public void test_vm_hashmap_entry() throws Exception {
        String src1 = MapExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(MapExamples.class.getCanonicalName(), "testHashMapEntry");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = MapExamples.testHashMapEntry();
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test
    public void test_vm_treemap_entry() throws Exception {
        String src1 = MapExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(MapExamples.class.getCanonicalName(), "testTreeMapEntry");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = MapExamples.testTreeMapEntry();
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test
    public void test_vm_custom1() throws Exception {
        String src1 = CustomExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(CustomExamples.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = CustomExamples.test();
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

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(SystemExamples.class.getCanonicalName(), "test");
        Object res = project.run();
    }
}
