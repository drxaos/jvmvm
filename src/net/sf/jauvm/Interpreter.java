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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import net.sf.jauvm.vm.GlobalCodeCache;
import net.sf.jauvm.vm.MethodCode;
import net.sf.jauvm.vm.VirtualMachine;

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
 * @see interpretable
 */
public class Interpreter implements Runnable {
    private final Runnable run;

    /**
     * Constructs a new {@code Interpreter} object to interpret the specified {@code Runnable} object.
     * @param run the {@code Runnable} object whose {@code run()} method is to be interpreted
     * @throws NullPointerException if {@code run} is {@code null}
     */
    public Interpreter(final Runnable run) {
        if (run == null) throw new NullPointerException();
        this.run = run;
    }

    /**
     * Constructs a new {@code Interpreter} object to return to the specified {@code Continuation} object's
     * stored execution point.
     * <p/>
     * Invoking this constructor has the same effect as:
     * <pre>  {@code new Interpreter(new Runnable() {
     *      public @interpretable void run() {
     *          cont.returnTo();
     *      }
     *  });}</pre>
     * <p/>
     * The given {@code Continuation} must be of {@code void} return type.
     * @param cont the {@code Continuation} object to which to return to
     * @throws NullPointerException if {@code cont} is {@code null}
     * @throws IllegalArgumentException if {@code cont} is not of {@code void} return type
     * @see Continuation#getReturnType()
     */
    public Interpreter(final Continuation cont) {
        if (cont.getReturnType() != void.class) throw new IllegalArgumentException("return type mismatch");
        this.run = new Runnable() {
            public @interpretable void run() {
                cont.returnTo();
            }
        };
    }

    /**
     * Runs this {@code Interpreter} object, interpreting the underlying {@code Runnable} object's {@code run()} method.
     * @throws UndeclaredThrowableException if the underlying {@code Runnable} object's {@code run()} method throws a
     *      checked exception
     */
    public final void run() {
        try {
            Class<?> cls = run.getClass();
            Method method = cls.getMethod("run");
            MethodCode code = GlobalCodeCache.get(cls, "run()V");

            if (code == null) run.run();
            else new VirtualMachine(new Throwable().getStackTrace(), method, code, run).run();
        } catch (Error e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    /**
     * The framework's {@code main(String[] args)} method.
     * <p/>
     * Loads the class specified by the first argument and interprets that class's {@code main(String[] args)} method,
     * passing it the remainder of the arguments.
     * @param args the first argument is the fully qualified name of the class to interpret, and the remainder of the
     *      arguments are passed to that class's {@code main(String[] args)} method
     * @throws Throwable any exception thrown as the result of trying to execute the classe's {@code
     *      main(String[] args)} method
     */
    public static void main(String... args) throws Throwable {
        if (args.length < 1) usage();

        if ("-ser".equals(args[0])) {
            if (args.length != 2) usage();

            ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[1]));
            Continuation cont = (Continuation) in.readObject();
            in.close();

            new Interpreter(cont).run();
            return;
        }

        Class<?> cls = Class.forName(args[0]);
        Method method = cls.getMethod("main", String[].class);
        MethodCode code = GlobalCodeCache.get(cls, "main([Ljava/lang/String;)V");

        String[] params = new String[args.length - 1];
        for (int i = 0; i < params.length; i++) params[i] = args[i + 1];

        if (code != null) new VirtualMachine(new Throwable().getStackTrace(), method, code, (Object) params).run();
        else {
            try {
                method.setAccessible(true);
                method.invoke(null, (Object) params);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    private static void usage() {
        System.out.println("usage: java " + Interpreter.class.getName() + " class [args...]");
        System.out.println("   or  java " + Interpreter.class.getName() + " -ser file");
        System.exit(1);
    }
}
