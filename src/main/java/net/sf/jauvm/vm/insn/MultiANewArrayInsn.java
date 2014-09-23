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

import java.lang.reflect.Array;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.Types;
import net.sf.jauvm.vm.VirtualMachine;
import net.sf.jauvm.vm.ref.ClassRef;

public final class MultiANewArrayInsn extends Insn {
    public static Insn getInsn(String desc, int dims, Class<?> cls) {
        if (Types.getInternalName(desc.substring(dims)) == null) return new MultiNewArrayInsn(desc, dims);
        else return new MultiANewArrayInsn(desc, dims, cls);
    }


    private final ClassRef c;
    private final int dims;

    MultiANewArrayInsn(String desc, int dims, Class<?> cls) {
        this.c = new ClassRef(Types.getInternalName(desc.substring(dims)), cls);
        this.dims = dims;
    }

    public void execute(VirtualMachine vm) {
        Frame frame = vm.getFrame();
        int[] dimensions = new int[dims];
        for (int i = dims; i-- > 0;) dimensions[i] = frame.popInt();
        frame.pushObject(Array.newInstance(c.get(), dimensions));
    }


    static final class MultiNewArrayInsn extends Insn {
        private final Class<?> c;
        private final int dims;

        MultiNewArrayInsn(String desc, int dims) {
            this.dims = dims;
            switch (desc.substring(dims).charAt(0)) {
                case 'I':
                    this.c = int.class;
                    return;
                case 'J':
                    this.c = long.class;
                    return;
                case 'F':
                    this.c = float.class;
                    return;
                case 'D':
                    this.c = double.class;
                    return;
                case 'C':
                    this.c = char.class;
                    return;
                case 'B':
                    this.c = byte.class;
                    return;
                case 'S':
                    this.c = short.class;
                    return;
                case 'Z':
                    this.c = boolean.class;
                    return;
                default:
                    this.c = null;
                    assert false;
            }
        }

        public void execute(VirtualMachine vm) {
            Frame frame = vm.getFrame();
            int[] dimensions = new int[dims];
            for (int i = dims; i-- > 0;) dimensions[i] = frame.popInt();
            frame.pushObject(Array.newInstance(c, dimensions));
        }
    }
}
