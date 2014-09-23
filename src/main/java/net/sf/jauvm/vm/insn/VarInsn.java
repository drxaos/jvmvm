/**
 * Copyright (c) 2005 Nuno Cruces
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
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

package net.sf.jauvm.vm.insn;

import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.VirtualMachine;
import static org.objectweb.asm.Opcodes.*;

public final class VarInsn extends Insn {
    public static Insn getInsn(int opcode, int var) {
        return new VarInsn(opcode, var);
    }


    private final int opcode;
    private final int var;

    VarInsn(int opcode, int var) {
        this.opcode = opcode;
        this.var = var;
    }

    public void execute(VirtualMachine vm) {
        Frame frame = vm.getFrame();
        switch (opcode) {
            case ILOAD:
                frame.loadInt(var);
                return;
            case LLOAD:
                frame.loadLong(var);
                return;
            case FLOAD:
                frame.loadFloat(var);
                return;
            case DLOAD:
                frame.loadDouble(var);
                return;
            case ALOAD:
                frame.loadObject(var);
                return;
            case ISTORE:
                frame.storeInt(var);
                return;
            case LSTORE:
                frame.storeLong(var);
                return;
            case FSTORE:
                frame.storeFloat(var);
                return;
            case DSTORE:
                frame.storeDouble(var);
                return;
            case ASTORE:
                frame.storeObject(var);
                return;

            case RET:
                vm.setCp(frame.getInt(var));
                return;

            default:
                assert false;
        }
    }
}