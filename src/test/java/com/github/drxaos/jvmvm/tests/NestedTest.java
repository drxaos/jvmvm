package com.github.drxaos.jvmvm.tests;

import com.github.drxaos.jvmvm.loader.Project;
import com.github.drxaos.jvmvm.tests.interpretable.NestedA;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class NestedTest {

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
        String name = NestedA.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("nested-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .addSystemClasses(bootstrap)
                .compile()
                .setupVM(NestedA.class.getCanonicalName(), "test3");

        Object res = project.run();
        Assert.assertEquals("result", "XBA", res);
    }
}
