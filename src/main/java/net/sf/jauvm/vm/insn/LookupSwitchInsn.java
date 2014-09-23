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
import net.sf.jauvm.vm.VirtualMachine;
import org.objectweb.asm.Label;

public final class LookupSwitchInsn extends LabeledInsn {
    public static Insn getInsn(Label dflt, int[] keys, Label[] labels) {
        return new LookupSwitchInsn(dflt, keys, labels);
    }


    private Label label;
    private int target;
    private Label[] labels;
    private final int[] targets;
    private final int[] keys;

    LookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.label = dflt;
        this.labels = labels;
        this.targets = new int[labels.length];
        this.keys = keys;
    }

    public void resolve(Map<Label, Integer> map) {
        target = map.get(label);
        for (int i = 0; i < labels.length; i++) targets[i] = map.get(labels[i]);
        label = null;
        labels = null;
    }

    public void execute(VirtualMachine vm) {
        int i = vm.getFrame().popInt();
        for (int j = 0; j < keys.length; j++)
            if (i == keys[j]) {
                vm.setCp(targets[j]);
                return;
            }
        vm.setCp(target);
    }
}
