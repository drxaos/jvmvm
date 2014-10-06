package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.tests.interpretable.SystemExamples;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class SubstitutionTest {

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
            Collection.class.getName(),

            StackTraceElement.class.getName(),
            Throwable.class.getName(),
            Exception.class.getName(),
            RuntimeException.class.getName(),
            UnsupportedOperationException.class.getName(),
            NoSuchElementException.class.getName(),
            UnsupportedEncodingException.class.getName(),

            Serializable.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            List.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            HashMap.class.getName(),
            TreeMap.class.getName()
    );

    @Test
    public void test_vm_system_file() throws Exception {
        String src1 = SystemExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .remap("java/io/File", "com/googlecode/jvmvm/tests/interpretable/FileStub")
                .compile()
                .setupVM(SystemExamples.class.getCanonicalName(), "test1");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = "deleted;type ok;name ok;no file;";
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }
}
