package com.github.drxaos.jvmvm.vm.placeholders;

import com.github.drxaos.jvmvm.vm.insn.MethodInsn;

public class Lambda {

    public static Lambda LAMBDA_STACK_SKIP = new Lambda(null);

    MethodInsn mInsn;

    public Lambda(MethodInsn mInsn) {
        this.mInsn = mInsn;
    }

    public MethodInsn getmInsn() {
        return mInsn;
    }
}
