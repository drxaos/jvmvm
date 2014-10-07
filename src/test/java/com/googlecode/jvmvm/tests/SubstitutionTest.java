package com.googlecode.jvmvm.tests;

import com.googlecode.jvmvm.loader.Project;
import com.googlecode.jvmvm.tests.interpretable.SystemExamples;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
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

            PrintStream.class.getName(),
            BufferedInputStream.class.getName(),

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
            IOException.class.getName(),
            MalformedURLException.class.getName(),

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

    Map<String, String> remapping = new HashMap<String, String>() {{
        put(File.class.getName(), FileStub.class.getName());
        put(System.class.getName(), SystemStub.class.getName());
    }};

    public static class FileStub {
        String name;

        public FileStub(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean delete() {
            return true;
        }

        public boolean exists() {
            return false;
        }
    }

    public static class SystemStub {
        public static ByteArrayOutputStream outBytes;
        public static ByteArrayOutputStream errBytes;
        public static PrintStream out;
        public static PrintStream err;

        static {
            reset();
        }

        public static long currentTimeMillis() {
            return 12345l;
        }

        public static void reset() {
            outBytes = new ByteArrayOutputStream();
            errBytes = new ByteArrayOutputStream();
            out = new PrintStream(outBytes);
            err = new PrintStream(errBytes);
        }
    }

    public static class UrlStub {
        String url;

        public UrlStub(String url) {
            this.url = url;
        }

        public final InputStream openStream() throws java.io.IOException {
            return new ByteArrayInputStream("file contents".getBytes());
        }
    }

    public static class FileOutputStreamStub {
        String fileName;
        static ByteArrayOutputStream file = new ByteArrayOutputStream();

        static String getFileContents() {
            return new String(file.toByteArray());
        }

        static void resetFileContents() {
            file = new ByteArrayOutputStream();
        }

        public FileOutputStreamStub(String fileName) {
            this.fileName = fileName;
        }

        public void close() throws IOException {
        }

        public void write(byte b[], int off, int len) throws IOException {
            file.write(b, off, len);
        }
    }

    @Test
    public void test_vm_system() throws Exception {
        String src1 = SystemExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .remap(remapping)
                .addSystemClasses(remapping.values())
                .compile()
                .setupVM(SystemExamples.class.getCanonicalName(), "test");

        byte[] bytes = project.saveToBytes();

        SystemStub.reset();
        Object res = project.run();

        String expected = "qwerty12345";
        Assert.assertEquals("result", expected, res);
        Assert.assertEquals("out", "hello out!\n" + expected + "\n", new String(SystemStub.outBytes.toByteArray()).replace("\r\n", "\n").replace("\r", "\n"));
        Assert.assertEquals("err", "hello err!\n" + expected + "\n", new String(SystemStub.errBytes.toByteArray()).replace("\r\n", "\n").replace("\r", "\n"));

        SystemStub.reset();
        Project project2 = Project.fromBytes(bytes);
        while (project2.isActive()) {
            project2.step(true);
        }
        Object res2 = project2.getResult();
        Assert.assertEquals("result2", expected, res2);
        Assert.assertEquals("out", "hello out!\n" + expected + "\n", new String(SystemStub.outBytes.toByteArray()).replace("\r\n", "\n").replace("\r", "\n"));
        Assert.assertEquals("err", "hello err!\n" + expected + "\n", new String(SystemStub.errBytes.toByteArray()).replace("\r\n", "\n").replace("\r", "\n"));
    }

    @Test
    public void test_vm_system_file() throws Exception {
        String src1 = SystemExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .remap("java/io/File", FileStub.class.getName())
                .addSystemClass(FileStub.class.getName())
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

    @Test
    public void test_vm_system_network() throws Exception {
        String src1 = SystemExamples.class.getCanonicalName().replace(".", "/") + ".java";

        Project project = new Project("serializer-test")
                .addFile(src1, FileUtils.readFileToString(new File("src/test/java/" + src1)))
                .addSystemClasses(bootstrap)
                .remap("java/net/URL", UrlStub.class.getName())
                .addSystemClass(UrlStub.class.getName())
                .remap("java/io/FileOutputStream", FileOutputStreamStub.class.getName())
                .addSystemClass(FileOutputStreamStub.class.getName())
                .compile()
                .setupVM(SystemExamples.class.getCanonicalName(), "test2");

        byte[] bytes = project.saveToBytes();

        FileOutputStreamStub.resetFileContents();
        project.run();

        String expected = "file contents";
        Assert.assertEquals("result", expected, FileOutputStreamStub.getFileContents());

        Project project2 = Project.fromBytes(bytes);

        FileOutputStreamStub.resetFileContents();
        while (project2.isActive()) {
            project2.step(true);
        }
        Assert.assertEquals("result2", expected, FileOutputStreamStub.getFileContents());
    }
}
