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

import net.sf.jauvm.vm.GlobalCodeCache;
import net.sf.jauvm.vm.MethodCode;
import net.sf.jauvm.vm.VirtualMachine;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * A JVM byte code interpreter.
 * <p/>
 * An {@code Interpreter} object wraps a given {@code Runnable} object, interpreting that object's {@code run()} method
 * when its own {@code run()} method is invoked.
 * <p/>
 * This class also holds this library's {@code main(String[] args)} method, that receives a fully qualified class name
 * as its first argument and interprets that class's {@code main(String[] args)} method, passing it the remainder of
 * its arguments.
 * <p/>
 * Only methods tagged as {@code interpretable} are interpreted by an {@code Interpreter}.
 *
 * @see interpretable
 */
public class Interpreter implements Runnable {
    private final Runnable run;

    VirtualMachine vm;

    /**
     * Constructs a new {@code Interpreter} object to interpret the specified {@code Runnable} object.
     *
     * @param run the {@code Runnable} object whose {@code run()} method is to be interpreted
     * @throws NullPointerException if {@code run} is {@code null}
     */
    public Interpreter(final Runnable run) {
        if (run == null) throw new NullPointerException();
        this.run = run;

        try {
            Class<?> cls = run.getClass();
            Method method = cls.getMethod("run");
            MethodCode code = GlobalCodeCache.get(cls, "run()V");
            vm = new VirtualMachine(new Throwable().getStackTrace(), method, code, run);
        } catch (Error e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    /**
     * Runs this {@code Interpreter} object, interpreting the underlying {@code Runnable} object's {@code run()} method.
     *
     * @throws java.lang.reflect.UndeclaredThrowableException if the underlying {@code Runnable} object's {@code run()} method throws a
     *                                      checked exception
     */
    public final void run() {
        try {
            vm.run();
        } catch (Error e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    private static void usage() {
        System.out.println("usage: java " + Interpreter.class.getName() + " class [args...]");
        System.out.println("   or  java " + Interpreter.class.getName() + " -ser file");
        System.exit(1);
    }
}
