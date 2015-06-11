package com.github.drxaos.jvmvm.tests.interpretable;

class LoaderA {

    public String P = "P;";
    public static String S = "S;";
    public static String S1 = "S" + "1" + ";";
    public static final String F = "F;";

    private static final String X;

    static {
        X = "X;";
    }

    static String outs = "";
    String out = "";

    static {
        outs += "SA;";
        outs += X;
        outs += F;
        outs += S;
    }

    {
        out += "IA;";
        out += P;
        out += F;
    }

    public LoaderA() {
        out += "CA;";
    }

    public String m() throws Exception {
        out += "AM;";
        return out;
    }

    public static String ms() throws Exception {
        return outs;
    }

    public static void main(String[] a) {
        new LoaderB();
    }
}

public class LoaderB extends LoaderA {
    static {
        outs += "SB;";
    }

    {
        out += "IB;";
    }

    public LoaderB() {
        out += "CB;";
    }

    public String m() throws RuntimeException {
        out += "BM;";
        return out;
    }

    public static String ms() throws Exception {
        return new LoaderB().m();
    }
}
