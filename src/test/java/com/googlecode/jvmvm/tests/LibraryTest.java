package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectCompilerException;
import com.googlecode.jvmvm.tests.interpretable.libtest.Dependency;
import com.googlecode.jvmvm.tests.interpretable.libtest.Dependent;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class LibraryTest {

    @Before
    public void setUp() {

    }

    List<String> bootstrap = Arrays.asList(
            Object.class.getName(),

            String.class.getName(),
            StringBuilder.class.getName(),
            CharSequence.class.getName(),

            PrintStream.class.getName(),

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
            Cloneable.class.getName(),
            RandomAccess.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            List.class.getName(),
            AbstractList.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            HashMap.class.getName(),
            TreeMap.class.getName()
    );

    @Test(expected = ProjectCompilerException.class)
    public void test_compiler_library3() throws Exception {
        String src2 = Dependent.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("library-test")
                .addFile(src2, FileUtils.readFileToString(new File("src/test/java/" + src2)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(Dependent.class.getCanonicalName(), "test");
    }

    @Test(expected = ProjectCompilerException.class)
    public void test_compiler_library2() throws Exception {
        String src1 = Dependency.class.getCanonicalName().replace(".", "/") + ".java";
        String src2 = Dependent.class.getCanonicalName().replace(".", "/") + ".java";

        byte[] library = new Project("library")
                .addFile(src1.replace("Dependency", "Dependency1"), FileUtils.readFileToString(new File("src/test/java/" + src1)).replace("Dependency", "Dependency1"))
                .addSystemClasses(bootstrap)
                .compileJar();

        Project project = new Project("library-test")
                .addFile(src2, FileUtils.readFileToString(new File("src/test/java/" + src2)))
                .addSystemClasses(bootstrap)
                .addJar(library)
                .compile()
                .setupVM(Dependent.class.getCanonicalName(), "test");
    }

    @Test
    public void test_compiler_library1() throws Exception {
        String src1 = Dependency.class.getCanonicalName().replace(".", "/") + ".java";
        String src2 = Dependent.class.getCanonicalName().replace(".", "/") + ".java";

        byte[] library = new Project("library")
                .addFile(src1.replace("Dependency", "Dependency1"), FileUtils.readFileToString(new File("src/test/java/" + src1)).replace("Dependency", "Dependency1"))
                .addSystemClasses(bootstrap)
                .compileJar();

        Project project = new Project("library-test")
                .addFile(src2, FileUtils.readFileToString(new File("src/test/java/" + src2)).replace("Dependency", "Dependency1"))
                .addSystemClasses(bootstrap)
                .addJar(library)
                .compile()
                .setupVM(Dependent.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = "Dependent Hello World ! Dependency1";
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test
    public void test_compiler_library() throws Exception {
        String src1 = Dependency.class.getCanonicalName().replace(".", "/") + ".java";
        String src2 = Dependent.class.getCanonicalName().replace(".", "/") + ".java";

        byte[] library = new Project("library")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .compileJar();

        Project project = new Project("library-test")
                .addFile(src2, FileUtils.readFileToString(new File("src/test/java/" + src2)))
                .addSystemClasses(bootstrap)
                .addJar(library)
                .compile()
                .setupVM(Dependent.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = "Dependent Hello World ! Dependency";
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }
}
