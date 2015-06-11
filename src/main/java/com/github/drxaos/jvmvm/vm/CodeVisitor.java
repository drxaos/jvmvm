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

package com.github.drxaos.jvmvm.vm;

import com.github.drxaos.jvmvm.vm.insn.*;
import com.github.drxaos.jvmvm.vm.ref.ClassRef;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.util.*;

public final class CodeVisitor extends EmptyVisitor {
    private static final EmptyVisitor emptyVisitor = new EmptyVisitor();

    private final Class<?> cls;
    private final Map<String, MethodCode> code;

    private String source;
    private int version;

    private boolean interpretable;
    private String name;
    private String desc;
    private int access;

    private List<Insn> insns;
    private List<ExcptHandler> excpts;
    private SortedSet<LineNumber> lines;
    private Map<Label, Integer> labels;


    public CodeVisitor(Class<?> cls, Map<String, MethodCode> code) {
        this.cls = cls;
        this.code = code;
    }


    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.version = version;
    }

    public void visitSource(String source, String debug) {
        this.source = source;
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (version < 49 /*|| "<init>".equals(name) || "<cinit>".equals(name)*/) return null;
        this.interpretable = true;
        this.name = name;
        this.desc = desc;
        this.access = access;
        return this;
    }


    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        //interpretable |= interpretable.class == ClassRef.get(Types.getInternalName(desc), cls);
        return emptyVisitor;
    }

    public void visitCode() {
        if (!interpretable) return;
        insns = new ArrayList<Insn>();
        excpts = new ArrayList<ExcptHandler>();
        lines = new TreeSet<LineNumber>();
        labels = new HashMap<Label, Integer>();
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        if (!interpretable) return;
        for (Insn insn : insns) {
            if (insn instanceof LabeledInsn) {
                LabeledInsn labeledInsn = (LabeledInsn) insn;
                labeledInsn.resolve(labels);
            }
        }
        for (ExcptHandler excpt : excpts) excpt.resolve(labels);
        MethodCode methodCode = new MethodCode(access, insns, excpts, lines, maxStack + maxLocals, source);
        code.put((name + desc).intern(), methodCode);
    }

    public void visitInsn(int opcode) {
        if (!interpretable) return;
        insns.add(Insn.getInsn(opcode));
    }

    public void visitIntInsn(int opcode, int operand) {
        if (!interpretable) return;
        insns.add(IntInsn.getInsn(opcode, operand));
    }

    public void visitVarInsn(int opcode, int var) {
        if (!interpretable) return;
        insns.add(VarInsn.getInsn(opcode, var));
    }

    public void visitTypeInsn(int opcode, String name) {
        if (!interpretable) return;
        insns.add(TypeInsn.getInsn(opcode, name, cls));
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (!interpretable) return;
        insns.add(FieldInsn.getInsn(opcode, owner, name, desc, cls));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (!interpretable) return;
        insns.add(MethodInsn.getInsn(opcode, owner, name, desc, cls));
    }

    public void visitJumpInsn(int opcode, Label label) {
        if (!interpretable) return;
        insns.add(JumpInsn.getInsn(opcode, label));
    }

    public void visitLabel(Label label) {
        if (!interpretable) return;
        labels.put(label, insns.size());
    }

    public void visitLdcInsn(Object cst) {
        if (!interpretable) return;
        insns.add(LdcInsn.getInsn(cst, cls));
    }

    public void visitIincInsn(int var, int increment) {
        if (!interpretable) return;
        insns.add(IincInsn.getInsn(var, increment));
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        if (!interpretable) return;
        insns.add(TableSwitchInsn.getInsn(min, max, dflt, labels));
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (!interpretable) return;
        insns.add(LookupSwitchInsn.getInsn(dflt, keys, labels));
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        if (!interpretable) return;
        insns.add(MultiANewArrayInsn.getInsn(desc, dims, cls));
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (!interpretable) return;
        excpts.add(new ExcptHandler(start, end, handler, type == null ? null : new ClassRef(type, cls)));
    }

    public void visitLineNumber(int line, Label start) {
        if (!interpretable) return;
        lines.add(new LineNumber(labels.get(start), line));
    }
}