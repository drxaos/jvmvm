/**
 * Copyright (c) 2005 Nuno Cruces
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package com.github.drxaos.jvmvm.vm.insn;

import com.github.drxaos.jvmvm.vm.Frame;
import com.github.drxaos.jvmvm.vm.VirtualMachine;
import org.objectweb.asm.Handle;

import java.lang.invoke.MethodType;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public final class IndyInsn extends Insn {

    public static IndyInsn getInsn(String name, String desc, Handle bsm, Object[] bsmArgs, Class<?> cls) {
        switch (bsm.getTag()) {
            case H_INVOKESTATIC:
                Handle h = (Handle) bsmArgs[1];
                Class<?> type = MethodType.fromMethodDescriptorString(desc, cls.getClassLoader()).returnType();
                Insn insn = MethodInsn.getInsn(INVOKESTATIC, h.getOwner(), h.getName(), h.getDesc(), cls);
                return new IndyInsn(insn, type);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private final Insn mInsn;
    private final Class lambdaType;

    IndyInsn(Insn mInsn, Class lambdaType) {
        this.mInsn = mInsn;
        this.lambdaType = lambdaType;
    }

    public void execute(VirtualMachine vm) {
        Object lambda = vm.getInterfaceImplementer().implement(lambdaType);
        Frame frame = vm.getFrame();
        frame.pushObject(lambda);
        vm.putLambda(lambda, mInsn);
    }

    @Override
    public String toString() {
        return "INVOKEDYNAMIC " + mInsn;
    }
}