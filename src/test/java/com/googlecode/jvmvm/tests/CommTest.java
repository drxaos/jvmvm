package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.tests.interpretable.communication.Bus;
import com.googlecode.jvmvm.tests.interpretable.communication.Cpu;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CommTest {

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
    public void test_communications() throws Exception {
        String src = Cpu.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("comm-test")
                .addFile(src, FileUtils.readFileToString(new File("src/test/java/" + src)))
                .addSystemClasses(bootstrap)
                .addSystemClass(Bus.class.getName())
                .compile()
                .markObject("bus", new Bus())
                .setupVM(Cpu.class.getCanonicalName(), "start", null, new Class[]{Bus.class}, new Object[]{Project.Marker.byName("bus")});

        byte[] bytes = project.saveToBytes();
        Project project2 = Project.fromBytes(bytes);
        Project project3 = Project.fromBytes(bytes);

        Bus bus;
        for (int i = 0; i < 100; i++) {
            project2.step(true);
            project3.step(true);
        }
        bus = (Bus) project2.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output == null;
        bus.input = "qwerty12345";
        bus = (Bus) project3.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output == null;
        bus.input = "zxcvbnm";
        for (int i = 0; i < 300; i++) {
            project2.step(true);
            project3.step(true);
        }
        bus = (Bus) project2.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output.equals("54321ytrewq");
        bus.output = null;
        bus.input = "Hello World!";
        bus = (Bus) project3.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output.equals("mnbvcxz");
        bus.output = null;
        bus.input = "aaaaaaaaaa";
        for (int i = 0; i < 100; i++) {
            project2.step(true);
            project3.step(true);
        }
        bus = (Bus) project2.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output == null;
        bus = (Bus) project3.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output == null;
        for (int i = 0; i < 300; i++) {
            project2.step(true);
            project3.step(true);
        }
        bus = (Bus) project2.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output.equals("!dlroW olleH");
        bus = (Bus) project3.findMarkedObject("bus");
        assert bus.input == null;
        assert bus.output.equals("aaaaaaaaaa");
    }
}
