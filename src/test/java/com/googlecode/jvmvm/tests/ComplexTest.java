package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.tests.interpretable.Stack;
import com.googlecode.jvmvm.tests.interpretable.third.BalancedBinaryTree;
import com.googlecode.jvmvm.tests.interpretable.third.BinaryTree;
import com.googlecode.jvmvm.tests.interpretable.third.ChemicalEquation;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class ComplexTest {

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
            Cloneable.class.getName(),
            RandomAccess.class.getName(),

            Arrays.class.getName(),
            Collections.class.getName(),

            Deque.class.getName(),
            AbstractSequentialList.class.getName(),

            List.class.getName(),
            AbstractList.class.getName(),
            ArrayList.class.getName(),
            LinkedList.class.getName(),

            Set.class.getName(),
            HashSet.class.getName(),

            Map.class.getName(),
            AbstractMap.class.getName(),
            Map.Entry.class.getName(),
            HashMap.class.getName(),
            "java.util.HashMap$EntrySet",
            "java.util.HashMap$Entry",

            Override.class.getName()
    );

    @Test
    public void test_vm_stack() throws Exception {
        String name = Stack.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("complex-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(Stack.class.getCanonicalName(), "run", null,
                        new Class[]{String.class, String.class, String.class},
                        new Object[]{"asd", "141414", "+@#$"});

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = Stack.run("asd", "141414", "+@#$");
        Assert.assertEquals("result1", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test
    public void test_vm_third1() throws Exception {
        String name = ChemicalEquation.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("complex-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(ChemicalEquation.class.getCanonicalName(), "run");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = ChemicalEquation.run();
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }

    @Test
    public void test_vm_third2() throws Exception {
        String src1 = BinaryTree.class.getCanonicalName().replace(".", "/") + ".java";
        String src2 = BalancedBinaryTree.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("complex-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addFile(src2, FileUtils.readFileToString(new File("src/test/java/" + src2)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(BalancedBinaryTree.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        Object res = project.run();

        String expected = BalancedBinaryTree.test();
        Assert.assertEquals("result", expected, res);

        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
    }
}
