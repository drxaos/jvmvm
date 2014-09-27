package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.loader.ProjectStoppedException;
import com.googlecode.jvmvm.tests.interpretable.InheritanceA;
import com.googlecode.jvmvm.tests.interpretable.Stack;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ComplexTest {

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
    public void test_vm_synthetic() throws Exception {
        String name = Stack.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("synt-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(Stack.class.getCanonicalName(), "run", null,
                        new Class[]{String.class, String.class, String.class},
                        new Object[]{"asd", "141414", "+@#$"});

        Object res = project.run();
        Assert.assertEquals("result1", "01623785", res);
    }
}
