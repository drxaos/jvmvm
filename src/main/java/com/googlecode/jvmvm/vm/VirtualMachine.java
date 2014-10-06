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

import com.googlecode.jvmvm.loader.MemoryClassLoader;
import com.googlecode.jvmvm.loader.ProjectLoaderException;
import com.googlecode.jvmvm.vm.insn.Insn;
import com.googlecode.jvmvm.vm.insn.ReturnInsn;
import com.googlecode.jvmvm.vm.placeholders.HashMap.HashMap$EntryIterator;
import com.googlecode.jvmvm.vm.placeholders.HashMap.HashMap$EntrySet;
import com.googlecode.jvmvm.vm.placeholders.Mapper;
import com.googlecode.jvmvm.vm.placeholders.Placeholder;
import com.googlecode.jvmvm.vm.placeholders.PlaceholderFactory;
import com.googlecode.jvmvm.vm.ref.FieldRef;
import org.apache.commons.codec.binary.Base64;
import org.objectweb.asm.Type;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class VirtualMachine implements Serializable {
    private long stepNumber = 0;
    private int cp;
    private Frame frame;
    private Insn[] insns;
    private ExcptHandler[] excpts;
    private StackTraceElement[] trace;
    private Object result;

    private Map<FieldRef, Object> staticValues = new LinkedHashMap<FieldRef, Object>();
    private Set<Class> clinitedClasses = new HashSet<Class>();

    transient ClassLoader classLoader;

    VirtualMachine() {
    }

    /**
     * Start static void method() in VM
     *
     * @param cl        classLoader for program
     * @param className class to search method
     * @return VM
     * @throws Throwable on error
     */
    public static VirtualMachine create(ClassLoader cl, String className, String methodName, Object self, Class[] paramTypes, Object[] paramValues) throws Throwable {
        Class cls = cl.loadClass(className);
        Method method = null;
        try {
            method = cls.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            method = cls.getDeclaredMethod(methodName, paramTypes);
        }
        String methodDescriptor = Type.getMethodDescriptor(method);
        MethodCode code = GlobalCodeLoader.get(cls, methodName + methodDescriptor);
        VirtualMachine vm = new VirtualMachine(new Throwable().getStackTrace(), method, code, paramValues);
        vm.classLoader = cl;

        InvokeStaticInitializer.invoke(vm, cls);
        return vm;
    }

    public static VirtualMachine restart(VirtualMachine vm, String className, String methodName, Object self, Class[] paramTypes, Object[] paramValues) throws Throwable {
        Class cls = vm.classLoader.loadClass(className);
        Method method = null;
        try {
            method = cls.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            method = cls.getDeclaredMethod(methodName, paramTypes);
        }
        String methodDescriptor = Type.getMethodDescriptor(method);
        MethodCode code = GlobalCodeLoader.get(cls, methodName + methodDescriptor);
        vm.restart(new Throwable().getStackTrace(), method, code, paramValues);

        InvokeStaticInitializer.invoke(vm, cls);
        return vm;
    }

    public static VirtualMachine create(ClassLoader cl, InputStream in) throws Throwable {
        CustomClassLoaderObjectInputStream ois = new CustomClassLoaderObjectInputStream(in, cl);
        Object o = ois.readObject();
        if (o instanceof VirtualMachine) {
            VirtualMachine vm = (VirtualMachine) o;
            vm.classLoader = cl;
            vm.restoreStatics();
            return vm;
        } else {
            throw new IOException("object class is [" + o.getClass().getName() + "]");
        }
    }

    private void restoreStatics() throws ProjectLoaderException {
        for (Map.Entry<FieldRef, Object> e : staticValues.entrySet()) {
            Field f = e.getKey().get();

            try {
                // enable access
                // TODO check access
                f.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

                f.set(null, e.getValue());
            } catch (Exception ex) {
                throw new ProjectLoaderException("cannot restore statics", ex);
            }
        }
    }

    public static VirtualMachine create(ClassLoader cl, byte[] serialized) throws Throwable {
        return create(cl, new ByteArrayInputStream(serialized));
    }

    VirtualMachine(StackTraceElement[] trace, Method method, MethodCode code, Object... params) {
        this.setFrame(Frame.newBootstrapFrame(method, code, params));
        this.trace = trace;
    }

    void restart(StackTraceElement[] trace, Method method, MethodCode code, Object... params) {
        this.setFrame(Frame.newBootstrapFrame(method, code, params));
        this.trace = trace;
    }

    public Object run() throws Throwable {
        run(-1);
        return getResult();
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

    void run(long cycles) throws Throwable {
        while (frame != null) {
            try {
                synchronized (this) {
                    Insn insn = insns[cp++];

                    // clinit executed immediately if:
                    //  + T is a class and an instance of T is created.
                    //  + T is a class and a static method declared by T is invoked.
                    //  + A static field declared by T is assigned.
                    //  + A static field declared by T is used and the field is not a constant variable (§4.12.4).
                    //  + T is a top level class (§7.6), and an assert statement (§14.10) lexically nested within T (§8.1.3) is executed.
                    Class clinitCls = insn.getClassForClinit();
                    if (clinitCls != null && InvokeStaticInitializer.shouldClinit(this, clinitCls)) {
                        cp--;
                        InvokeStaticInitializer.invoke(this, clinitCls);
                        insn = insns[cp++];
                    }
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

    public void checkSerialization() throws NotSerializableException {
        try {
            save(new ByteArrayOutputStream());
        } catch (VirtualMachineException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NotSerializableException) {
                // stack contains not serializable object
                fillInStackTrace(cause, cause.getStackTrace());
                throw (NotSerializableException) cause;
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

        // TODO try https://github.com/EsotericSoftware/kryo

        synchronized (this) {
            try {
                CustomClassLoaderObjectOutputStream oos = new CustomClassLoaderObjectOutputStream(out);
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

    public boolean isClinited(Class cls) {
        return clinitedClasses.contains(cls);
    }

    public void onClinited(Class cls) {
        clinitedClasses.add(cls);
    }

    public void setStaticValue(FieldRef f, Object value) {
        staticValues.remove(f);
        staticValues.put(f, value);
    }
}

class CustomClassLoaderObjectInputStream extends ObjectInputStream {
    private ClassLoader classLoader;

    final static byte SOURCE_DEFAULT_CLASSLOADER = 0;
    final static byte SOURCE_VM_CLASSLOADER = 1;
    private byte source = 0;

    public CustomClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
        enableResolveObject(true);
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
        if (obj instanceof Placeholder) {
            return ((Placeholder) obj).restore();
        }
        return super.resolveObject(obj);
    }

    public void setLoadingSource(byte source) {
        this.source = source;
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        setLoadingSource(readByte());
        return super.readClassDescriptor();
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
        if (source == SOURCE_VM_CLASSLOADER) {
            return Class.forName(desc.getName(), false, classLoader);
        } else {
            return Class.forName(desc.getName(), false, this.getClass().getClassLoader());
        }
    }
}

class CustomClassLoaderObjectOutputStream extends ObjectOutputStream {

    public static HashMap<String, PlaceholderFactory> placeholders = new HashMap<String, PlaceholderFactory>();

    public static void addPlaceholder(PlaceholderFactory placeholder) {
        placeholders.put(placeholder.getClassName(), placeholder);
    }

    static {
        addPlaceholder(new HashMap$EntrySet());
        addPlaceholder(new HashMap$EntryIterator());
    }

    // TODO try to serialize non-Serializable classes from list
    // http://stackoverflow.com/questions/10142991/using-javassist-to-instrument-a-private-method-of-a-parent-class

    public CustomClassLoaderObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
        PlaceholderFactory placeholderFactory = placeholders.get(obj.getClass().getName());
        if (placeholderFactory != null) {
            return placeholderFactory.replace(obj);
        } else if (!(obj instanceof Serializable) && !(obj instanceof Enum) && !obj.getClass().isArray()) {
            return new Mapper().replace(obj);
        }
        return super.replaceObject(obj);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        if (desc.forClass().getClassLoader() instanceof MemoryClassLoader) {
            writeByte(CustomClassLoaderObjectInputStream.SOURCE_VM_CLASSLOADER);
        } else {
            writeByte(CustomClassLoaderObjectInputStream.SOURCE_DEFAULT_CLASSLOADER);
        }
        super.writeClassDescriptor(desc);
    }
}
