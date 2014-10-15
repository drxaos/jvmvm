package com.googlecode.jvmvm.ui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SrcUtil {

    public static String loadSrc(String base, String path) throws IOException {
        return new String(loadData(base, path), "UTF-8");
    }

    public static byte[] loadData(String base, String path) throws IOException {
        try {
            return FileUtils.readFileToByteArray(new File(base + "/" + path));
        } catch (IOException e) {
            try {
                InputStream in = SrcUtil.class.getClassLoader().getResourceAsStream(path);
                if (in == null) {
                    in = SrcUtil.class.getClassLoader().getResourceAsStream("/" + path);
                }
                if (in == null) {
                    in = ClassLoader.getSystemResourceAsStream(path);
                }
                if (in == null) {
                    in = ClassLoader.getSystemResourceAsStream("/" + path);
                }
                if (in == null) {
                    throw new IOException("cannot load source (not found): " + path);
                }
                return IOUtils.toByteArray(in);
            } catch (Exception e1) {
                IOException z = new IOException("cannot load source: " + path, e1);
                z.printStackTrace();
                throw z;
            }
        }
    }

}
