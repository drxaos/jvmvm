package com.googlecode.jvmvm.tests.interpretable;

public class InheritanceA {
    public static StringBuilder out = new StringBuilder();

    static {
        out.append("SA;");
    }

    {
        out.append("IA;");
    }

    public InheritanceA() {
        out.append("CA;");
    }

    public void main() {
        new InheritanceB();
    }

    public static void main(String[] a) {
        new InheritanceB();
    }
}

class InheritanceB extends InheritanceA {
    static {
        out.append("SB;");
    }

    {
        out.append("IB;");
    }

    public InheritanceB() {
        out.append("CB;");
    }
}