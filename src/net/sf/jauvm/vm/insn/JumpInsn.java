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

import java.util.Map;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.VirtualMachine;
import org.objectweb.asm.Label;
import static org.objectweb.asm.Opcodes.*;

public final class JumpInsn extends LabeledInsn {
    public static Insn getInsn(int opcode, Label label) {
        return new JumpInsn(opcode, label);
    }


    private final int opcode;
    private Label label;
    private int target;

    JumpInsn(int opcode, Label label) {
        this.opcode = opcode;
        this.label = label;
    }

    public void resolve(Map<Label, Integer> map) {
        target = map.get(label);
        label = null;
    }

    public void execute(VirtualMachine vm) {
        Frame frame = vm.getFrame();
        switch (opcode) {
            case IFEQ:
                if (frame.popInt() == 0) vm.setCp(target);
                return;
            case IFNE:
                if (frame.popInt() != 0) vm.setCp(target);
                return;
            case IFLT:
                if (frame.popInt() < 0) vm.setCp(target);
                return;
            case IFGE:
                if (frame.popInt() >= 0) vm.setCp(target);
                return;
            case IFGT:
                if (frame.popInt() > 0) vm.setCp(target);
                return;
            case IFLE:
                if (frame.popInt() <= 0) vm.setCp(target);
                return;
            case IF_ICMPEQ:
                if (frame.popInt() == frame.popInt()) vm.setCp(target);
                return;
            case IF_ICMPNE:
                if (frame.popInt() != frame.popInt()) vm.setCp(target);
                return;
            case IF_ICMPLT:
                if (frame.popInt() > frame.popInt()) vm.setCp(target);
                return;
            case IF_ICMPGE:
                if (frame.popInt() <= frame.popInt()) vm.setCp(target);
                return;
            case IF_ICMPGT:
                if (frame.popInt() < frame.popInt()) vm.setCp(target);
                return;
            case IF_ICMPLE:
                if (frame.popInt() >= frame.popInt()) vm.setCp(target);
                return;
            case IF_ACMPEQ:
                if (frame.popObject() == frame.popObject()) vm.setCp(target);
                return;
            case IF_ACMPNE:
                if (frame.popObject() != frame.popObject()) vm.setCp(target);
                return;
            case GOTO:
                vm.setCp(target);
                return;
            case JSR:
                frame.pushInt(vm.getCp());
                vm.setCp(target);
                return;
            case IFNULL:
                if (frame.popObject() == null) vm.setCp(target);
                return;
            case IFNONNULL:
                if (frame.popObject() != null) vm.setCp(target);
                return;
            default:
                assert false;
        }
    }
}
