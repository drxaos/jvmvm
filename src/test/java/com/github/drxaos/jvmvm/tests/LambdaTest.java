package com.github.drxaos.jvmvm.tests;

import com.github.drxaos.jvmvm.loader.BreakpointException;
import com.github.drxaos.jvmvm.loader.Project;
import com.github.drxaos.jvmvm.loader.SystemClassesCallback;
import com.github.drxaos.jvmvm.tests.interpretable.Calculator;
import com.github.drxaos.jvmvm.tests.interpretable.Closure;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class LambdaTest {

    @Before
    public void setUp() {

    }

    @Test
    public void test_calculator() throws Exception {
        String name = Calculator.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("lambda-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .setSystemClassesCallback(className -> {
                    System.err.println("Loading: " + className);
                    return true;
                })
                .compile()
                .setupVM(Calculator.class.getCanonicalName(), "main", null,
                        new Class[]{String[].class},
                        new Object[]{new String[]{}});

        project.run();
    }

    @Test
    public void test_calculator_breakpoint() throws Exception {
        String name = Calculator.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("lambda-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .setSystemClassesCallback((SystemClassesCallback) className -> {
                    System.err.println("Loading: " + className);
                    return true;
                })
                .compile()
                .setupVM(Calculator.class.getCanonicalName(), "main", null,
                        new Class[]{String[].class},
                        new Object[]{new String[]{}});

        project.setBreakpoint("com.github.drxaos.jvmvm.tests.interpretable.Calculator", "operateBinary");
        try {
            project.run();
            Assert.fail();
        } catch (BreakpointException e) {
            project.saveToBytes();
            project.clearBreakpoints();
            project.run();
        } catch (Throwable e) {
            Assert.fail();
        }
    }

    @Test
    public void test_closure() throws Exception {
        String name = Closure.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("lambda-test")
                .addFile(name, FileUtils.readFileToString(new File("src/test/java/" + name)))
                .setSystemClassesCallback(className -> {
                    System.err.println("Loading2: " + className);
                    return true;
                })
                .compile()
                .setupVM(Closure.class.getCanonicalName(), "main", null,
                        new Class[]{String[].class},
                        new Object[]{new String[]{}});

        project.run();
    }

}
