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

package net.sf.jauvm;

import java.io.*;
import java.lang.reflect.Method;
import net.sf.jauvm.vm.Frame;
import net.sf.jauvm.vm.Types;
import net.sf.jauvm.vm.ref.MethodRef;

/**
 * A continuation representing the stored execution state of an {@code Interpreter} object.
 * <p/>
 * A {@code Continuation} object wraps a given method invocation, which can later (and even repeatedly) be returned from
 * or otherwise terminated by throwing an exception.
 * <p/>
 * A {@code Continuation} object can be created and returned to only in interpreted code, i.e. in methods tagged as
 * {@code interpretable} and ran by an {@code Interpreter}.
 * @see Interpreter
 * @see interpretable
 * @serial exclude
 */
public class Continuation implements Serializable {
    private static final long serialVersionUID = 4711097160804712512l;

    final Frame frame;
    final int ret;

    volatile transient Method method;


    Continuation(Method method, Frame frame, int ret) {
        this.method = method;
        this.frame = frame;
        this.ret = ret;
    }

    /**
     * Captures the current execution state of the {@code Interpreter} as a new {@code Continuation} object.
     * <p/>
     * The call to this constructor in the following example captures the execution state of the caller of {@code
     * aMethod()}, wrapping the current {@code aMethod()} invocation in a {@code Continuation} object:
     * <pre>  {@code public static @interpretable void aMethod() {
     *      Continuation cont = new Continuation();
     *  }}</pre>
     * A continuation can only be captured in interpreted code.
     * @throws UnsupportedOperationException if the constructor is not invoked from interpreted code
     */
    public Continuation() {
        throw new UnsupportedOperationException("continuations not supported here");
    }


    /**
     * Returns to the execution point represented by this <code>Continuation</code> object (with no return value).
     * <p/>
     * Returning to a continuation that wraps a given method invocation, is the same as returning, possibly once again,
     * from that method invocation.
     * <p/>
     * Accordingly, this method can only be invoked on continuations of {@code void} return type, that is, continuations
     * captured in a method of {@code void} return type. Also, a continuation can only be returned to in interpreted
     * code.
     * @throws UnsupportedOperationException if this method is not invoked from interpreted code
     * @throws IllegalArgumentException if this continuation is not of {@code void} return type
     * @see #getReturnType()
     */
    public void returnTo() {
        throw new UnsupportedOperationException("continuations not supported here");
    }

    /**
     * Returns {@code value} to the execution point represented by this <code>Continuation</code> object.
     * <p/>
     * Returning to a continuation that wraps a given method invocation, is the same as returning, possibly once again,
     * from that method invocation.
     * <p/>
     * Accordingly, this method can only be invoked on continuations whose return type is assignable from {@code value},
     * that is, {@code value} must be assignable to the return type of the method the continuation was captured in. The
     * return value {@code value} is assignable to that return type if there is an unwrapping and/or widening conversion
     * that goes from {@code value}'s type to that return type. Also, a continuation can only be returned to in
     * interpreted code.
     * @throws UnsupportedOperationException if this method is not invoked from interpreted code
     * @throws IllegalArgumentException if {@code value} is not assignable to this continuation's return type
     * @see #getReturnType()
     */
    public void returnTo(Object value) {
        throw new UnsupportedOperationException("continuations not supported here");
    }

    /**
     * Throws {@code thrwbl} to the execution point represented by this <code>Continuation</code> object.
     * <p/>
     * Throwing an exception to a continuation that wraps a given method invocation, is the same as having that method
     * invocation throw the given exception.
     * <p/>
     * Accordingly, {@code thrwbl} must either be an unchecked exception, or a checked exception accepted by this
     * continuation, that is, a checked exception declared by method the continuation was captured in. Also, a
     * continuation can only be thrown to in interpreted code.
     * @throws UnsupportedOperationException if this method is not invoked from interpreted code
     * @throws IllegalArgumentException if {@code thrwbl} is a checked exception not declared by this continuation
     * @see #getExceptionTypes()
     */
    public void throwTo(Throwable thrwbl) {
        throw new UnsupportedOperationException("continuations not supported here");
    }

    /**
     * Gets the return type of the method invocation wrapped in this {@code Continuation} object.
     * @return this continuation's return type
     */
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    /**
     * Gets the exception types declared by the method invocation wrapped in this {@code Continuation} object.
     * @return an array with this continuation's accepted exception types
     */
    public Class<?>[] getExceptionTypes() {
        return method.getExceptionTypes();
    }


    /**
     * Gets this method invocation's current continuation as a new <code>Continuation</code> object.
     * @return the current continuation
     */
    public static @interpretable Continuation getCurrent() {
        return new Continuation();
    }

    /**
     * @deprecated As of release 1.0.1, replaced by {@link #getCurrent()}. Scheduled for removal in 1.1.
     */
    public static @interpretable Continuation getCurrentContinuation() {
        return getCurrent();
    }

    /**
     * Serializes this method invocation's current continuation to the file.
     * @return true if the {@code Continuation} object was just saved, false if returning from the file
     * @throws IOException if an I/O error occurs writing to the file
     */
    public static @interpretable boolean saveCurrentTo(File file) throws IOException {
        helper(file, new Continuation());
        return false;
    }

    private static @interpretable void helper(File file, Continuation ret) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        try {
            out.writeObject(new Continuation());
        } finally {
            out.close();
        }

        ret.returnTo(true);
    }

    /**
     * Forks a new thread at the current execution point.
     * @return {@code null} in the new thread, the new {@code Thread} object in the current thread
     */
    public static @interpretable Thread forkThread() {
        helper(new Continuation());
        return null;
    }

    private static @interpretable void helper(Continuation ret) {
        Thread t = new Thread(new Interpreter(new Continuation()));
        t.start();
        ret.returnTo(t);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(method.getDeclaringClass());
        out.writeUTF(method.getName());
        out.writeUTF(Types.getDescriptor(method));

        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Class<?> cls = (Class) in.readObject();
        String name = in.readUTF();
        String desc = in.readUTF();

        method = MethodRef.get(cls, name, desc);

        in.defaultReadObject();
    }
}
