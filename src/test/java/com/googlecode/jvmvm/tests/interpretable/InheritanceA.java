package com.googlecode.jvmvm.tests.interpretable;

public class InheritanceA {
    public static final StringBuilder out = new StringBuilder();

    static {
        out.append("SA;");
    }

    {
        out.append("IA;");
    }

    public InheritanceA() {
        out.append("CA;");
    }

    public static void main(String []a) {
        System.out.println(new InheritanceB().out);
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