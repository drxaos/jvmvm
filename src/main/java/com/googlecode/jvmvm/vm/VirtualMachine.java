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

package com.googlecode.jvmvm.vm;

import com.googlecode.jvmvm.vm.insn.Insn;
import com.googlecode.jvmvm.vm.insn.ReturnInsn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Type;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VirtualMachine implements Serializable {
    private long stepNumber = 0;
    private int cp;
    private Frame frame;
    private Insn[] insns;
    private ExcptHandler[] excpts;
    private StackTraceElement[] trace;
    private Object result;

    transient ClassLoader classLoader;


    // TODO on start schedule execution of <clinit-jvmvm> of classes loaded by classloader before called method
    // TODO on deserialization load maps of static values to static fields
    // TODO serialize vm on each step to prevent using not serializable objects

    
    VirtualMachine() {
    }

    /**
     * Start static void method() in VM
     *
     * @param cl              classLoader for program
     * @param className       class to search method
     * @param methodSignature method to start
     * @return VM
     * @throws Throwable on error
     */
    public static VirtualMachine create(ClassLoader cl, String className, String methodName, Object self, Class[] paramTypes, Object[] paramValues) throws Throwable {
        Class cls = cl.loadClass(className);
        Method method = cls.getMethod(methodName, paramTypes);
        String methodDescriptor = Type.getMethodDescriptor(method);
        MethodCode code = GlobalCodeLoader.get(cls, methodName + methodDescriptor);
        VirtualMachine vm = new VirtualMachine(new Throwable().getStackTrace(), method, code, ArrayUtils.addAll(new Object[]{self}, paramValues));
        vm.classLoader = cl;
        return vm;
    }

    public static VirtualMachine create(ClassLoader cl, InputStream in) throws Throwable {
        CustomClassLoaderObjectInputStream ois = new CustomClassLoaderObjectInputStream(in, cl);
        Object o = ois.readObject();
        if (o instanceof VirtualMachine) {
            VirtualMachine vm = (VirtualMachine) o;
            vm.classLoader = cl;
            return vm;
        } else {
            throw new IOException("object class is [" + o.getClass().getCanonicalName() + "]");
        }
    }

    public static VirtualMachine create(ClassLoader cl, byte[] serialized) throws Throwable {
        return create(cl, new ByteArrayInputStream(serialized));
    }

    VirtualMachine(StackTraceElement[] trace, Method method, MethodCode code, Object... params) {
        this.setFrame(Frame.newBootstrapFrame(method, code, params));
        this.trace = trace;
    }

    public void run() throws Throwable {
        run(-1);
    }

    public boolean isActive() {
        return frame != null;
    }

    public void step() throws Throwable {
        run(1);
    }

    public long getStepNumber() {
        return stepNumber;
    }

    public void run(long cycles) throws Throwable {
        while (frame != null) {
            try {
                synchronized (this) {
                    Insn insn = insns[cp++];
                    insn.execute(this);
                    stepNumber++;
                }
                if (cycles > 0) {
                    cycles--;
                }
                if (cycles == 0) {
                    return;
                }
            } catch (StackTracedException e) {
                Throwable t = e.getCause();
                fillInStackTrace(t, t.getStackTrace());
                if (!findHandler(t)) throw t;
            } catch (Throwable t) {
                fillInStackTrace(t);
                if (!findHandler(t)) throw t;
            }
        }
    }


    public byte[] serializeToBytes() {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            save(s);
            s.close();
            return s.toByteArray();
        } catch (Exception e) {
            // cannot save
        }
        return null;
    }

    public String serializeToString() {
        return Base64.encodeBase64String(serializeToBytes());
    }

    public void save(OutputStream out) throws VirtualMachineException {
        synchronized (this) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(this);
            } catch (NotSerializableException e) {
                throw new VirtualMachineException("Instance of illegal class [" + e.getMessage() + "] at " + getPointer(), e);
            } catch (IOException e) {
                throw new VirtualMachineException("unknown error", e);
            }
        }
    }

    public StackTraceElement getPointer() {
        if (frame == null) {
            return null;
        } else if (frame.getMethod() != null) {
            Method m = frame.getMethod();
            MethodCode c = frame.getCode();
            return new StackTraceElement(m.getDeclaringClass().getName(), m.getName(),
                    c.source, LineNumber.getLine(c.lines, cp));
        } else {
            Constructor m = frame.getConstructor();
            MethodCode c = frame.getCode();
            return new StackTraceElement(m.getDeclaringClass().getName(), "<init>",
                    c.source, LineNumber.getLine(c.lines, cp));
        }
    }

    private boolean findHandler(Throwable thrwbl) {
        while (frame != null) {
            for (ExcptHandler excpt : excpts) {
                if (excpt.start < cp && cp <= excpt.end && (excpt.cls == null || excpt.cls.get().isInstance(thrwbl))) {
                    cp = excpt.handler;
                    frame = frame.getMutableCopy();
                    frame.popAll();
                    frame.pushObject(thrwbl);
                    return true;
                }
            }
            setCp(frame.getRet());
            setFrame(frame.getParent());
        }
        return false;
    }

    private void fillInStackTrace(Throwable thrwbl, StackTraceElement... st) {
        List<StackTraceElement> lst = new ArrayList<StackTraceElement>();
        lst.addAll(Arrays.asList(st));
        int cp = this.cp;
        Frame frame = this.frame;
        while (frame != null) {
            MethodCode c = frame.getCode();
            if (frame.getMethod() != null) {
                Method m = frame.getMethod();
                lst.add(new StackTraceElement(m.getDeclaringClass().getName(), m.getName(),
                        c.source, LineNumber.getLine(c.lines, cp)));
            } else {
                Constructor m = frame.getConstructor();
                lst.add(new StackTraceElement(m.getDeclaringClass().getName(), m.getName(),
                        c.source, LineNumber.getLine(c.lines, cp)));
            }
            cp = frame.getRet();
            frame = frame.getParent();
        }
        lst.addAll(Arrays.asList(trace));
        thrwbl.setStackTrace(lst.toArray(trace));
    }


    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
        if (frame == null) return;
        insns = frame.getCode().insns;
        excpts = frame.getCode().excpts;
    }

    public Insn getInsn() {
        return insns[cp];
    }

    public boolean inTailPosition(Class<?> returnType) {
        for (ExcptHandler excpt : excpts) if (excpt.start < cp && cp <= excpt.end) return false;
        return insns[cp] instanceof ReturnInsn && ((ReturnInsn) insns[cp]).canReturn(returnType);
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}

class CustomClassLoaderObjectInputStream extends ObjectInputStream {
    private ClassLoader classLoader;

    public CustomClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    List<String> vm = Arrays.asList(
            "com.googlecode.jvmvm.vm.VirtualMachine",
            "sun.reflect.SerializationConstructorAccessorImpl"
    );

    protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
        if (vm.contains(desc.getName())) {
            return Class.forName(desc.getName(), false, this.getClass().getClassLoader());
        } else {
            return Class.forName(desc.getName(), false, classLoader);
        }
    }
}
