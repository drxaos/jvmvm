package com.googlecode.jvmvm.tests.interpretable;

public class InheritanceA {
    public static StringBuilder out = new StringBuilder();
    public static String out2 = "0";

    static {
        //String sf = InheritanceB.SF;
        out.append("SA;");
        out2 += "1";
    }

    {
        out.append("IA;");
        out2 += "2";
    }

    public InheritanceA() {
        out.append("CA;");
        out2 += "3";
    }

    public void main() {
        new InheritanceB();
        out2 += "4";
    }

    public static String main(String[] a) {
        new InheritanceB();
        out2 += "5";
        return out2;
    }
}

class InheritanceB extends InheritanceA {
    public static String SF = "SF;";

    static {
        out.append("SB;");
        out2 += "6";
    }

    {
        out.append("IB;");
        out2 += "7";
    }

    public InheritanceB() {
        out.append("CB;");
        out2 += "8";
    }
}