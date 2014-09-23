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

package net.sf.jauvm.util;

import net.sf.jauvm.Continuation;
import net.sf.jauvm.interpretable;

/**
 * A class to facilitate non-deterministic programing with back-tracking.
 * <p/>
 * This class helps in implementing non-deterministic programing algorithms doing a deep-first search on problem space
 * solutions.
 */
public final class Ambivalence<T> {
    private Continuation failure;

    /**
     * Indicates there is no choice of values to return at this point.
     * <p/>
     * This is the same as calling {@code fail()} with a return type. It never returns, though.
     * @see #fail()
     * @throws NoSolutionException if there are no alternative choices in the problem space to back-track to.
     */
    public @interpretable T choose() {
        fail();
        throw null;
    }

    /**
     * Indicates there is only one possible value to return at this point.
     * <p/>
     * Allways returns {@code value}. The identity function.
     * @param value the value to return
     * @return {@code value}
     */
    public T choose(T value) {
        return value;
    }

    /**
     * Ambiguously chooses one of its arguments to return.
     * <p/>
     * Invoked with one argument this is the same as calling {@code choose(T value)}; invoked with no arguments, its the
     * same as {@code choose()}, or a {@code fail()} with a return type.
     * @param values the values from which to choose from
     * @return the chosen value
     * @throws NoSolutionException if no choice provides a solution and there are no alternative choices in the
     *      problem space to back-track to.
     */
    public @interpretable T choose(T... values) {
        if (values.length == 0) fail();
        if (values.length == 1) return values[0];

        Continuation old = failure;

        Continuation success = new Continuation();
        for (T value : values) helper(value, success);

        failure = old;
        fail();

        throw null;
    }

    private @interpretable void helper(T value, Continuation success) {
        failure = new Continuation();
        success.returnTo(value);
    }

    /**
     * Causes the computation to fail at this point.
     * <p/>
     * Calling fail forces this {@code Ambivalence} object to back-track to previous choice points and try different
     * values for those.
     * @throws NoSolutionException if there are no alternative choices in the problem space to back-track to.
     */
    public @interpretable void fail() {
        if (failure != null) failure.returnTo();
        throw new NoSolutionException();
    }

    /**
     * Requires a certain condition to be true failing otherwise.
     * @throws NoSolutionException if there are no more sollutions in the problem space to try
     */
    public @interpretable void require(boolean condition) {
        if (!condition) fail();
    }

    /**
     * Covers all solutions defined by a {@code Runnable} object's {@code run()} method.
     * <p/>
     * This method takes a {@code Runnable} object whose {@code run()} method defines a problem space with this {@code
     * Ambivalence} object. This method will run that {@code run()} method till it's normal termination for every
     * solution defined in the problem space.
     */
    public @interpretable void cover(Runnable runnable) {
        Continuation old = failure;
        helper(runnable);
        failure = old;
    }

    private @interpretable void helper(Runnable runnable) {
        failure = new Continuation();
        runnable.run();
        fail();
    }


    /**
     * Thrown to indicate that there are no solutions in this {@code Ambivalence} object's problem space.
     */
    public class NoSolutionException extends RuntimeException {
        private NoSolutionException() {
        }

        /**
         * Gets the {@code Ambivalence} object whose problem space solutions where exausted.
         * @return the {@code Ambivalence} object associated to this exception
         */
        public Ambivalence<T> getAmbivalence() {
            return Ambivalence.this;
        }
    }
}
