package com.github.drxaos.jvmvm.loader;

import com.github.drxaos.jvmvm.vm.Breakpoint;

public class BreakpointException extends Exception {
    Breakpoint breakpoint;
    StackTraceElement pointer;

    public BreakpointException(Breakpoint breakpoint, StackTraceElement pointer) {
        this.breakpoint = breakpoint;
        this.pointer = pointer;
    }

    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    public StackTraceElement getPointer() {
        return pointer;
    }
}
