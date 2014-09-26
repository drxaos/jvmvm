package com.googlecode.jvmvm.tests.interpretable;

class LoaderA {

    public String P = "P;";
    public static String S = "S;";
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

    public String m() {
        out += "AM;";
        return out;
    }

    public static String ms() {
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

    public String m() {
        out += "BM;";
        return out;
    }

    public static String ms() {
        return new LoaderB().m();
    }
}