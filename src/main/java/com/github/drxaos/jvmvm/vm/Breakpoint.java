package com.github.drxaos.jvmvm.vm;

public class Breakpoint {
    String clazz;
    String method;
    Integer line;

    public Breakpoint(String clazz, String method, Integer line) {
        this.clazz = clazz;
        this.method = method;
        this.line = line;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Breakpoint that = (Breakpoint) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        return line != null ? line.equals(that.line) : that.line == null;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Breakpoint{" +
                "clazz='" + clazz + '\'' +
                ", method='" + method + '\'' +
                ", line=" + line +
                '}';
    }
}
