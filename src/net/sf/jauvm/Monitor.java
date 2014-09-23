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

import java.util.concurrent.TimeUnit;

/**
 * Contains methods used to synchronize access to objects.
 */
public class Monitor {
    protected Monitor() {
    }

    /**
     * Acquires the monitor for an object.
     * <p/>
     * Calling this method on a given object is equivalent to entering a synchronized block that protects that object.
     * <p/>
     * Note: this method is currently not implemented, and as such always throws the {@code
     * UnsupportedOperationException}. See the {@code java.util.concurrent.locks} package for alternative
     * synchronization mechanisms.
     * @param obj the object on which to acquire the monitor lock
     * @throws NullPointerException if {@code obj} is null
     * @throws UnsupportedOperationException if this operation is not supported by the current JVM
     * @see java.util.concurrent.locks
     */
    public static void enter(Object obj) {
        if (obj == null) throw new NullPointerException();
        throw new UnsupportedOperationException("unstructured locks not supported");
    }

    /**
     * Releases the monitor for an object.
     * <p/>
     * Calling this method on a given object is equivalent to exiting a synchronized block that protects that object.
     * <p/>
     * Note: this method is currently not implemented, and as such always throws the {@code
     * UnsupportedOperationException}. See the {@code java.util.concurrent.locks} package for alternative
     * synchronization mechanisms.
     * @param obj the object on which to acquire the monitor lock
     * @throws NullPointerException if {@code obj} is null
     * @throws IllegalMonitorStateException if the current thread does not hold the lock on {@code obj}
     * @throws UnsupportedOperationException if this operation is not supported by the current JVM
     * @see java.util.concurrent.locks
     */
    public static void exit(Object obj) {
        if (!Thread.holdsLock(obj)) throw new IllegalMonitorStateException();
        throw new UnsupportedOperationException("unstructured locks not supported");
    }

    /**
     * Notifies a single thread that is waiting on an object's monitor.
     * <p/>
     * Calling this method on a given object is equivalent calling that object's {@code notify()} method.
     * @param obj the object a thread is waiting for
     * @see Object#notify()
     */
    public static void notify(Object obj) {
        obj.notify();
    }

    /**
     * Notifies all threads that are waiting on an object's monitor.
     * <p/>
     * Calling this method on a given object is equivalent calling that object's {@code notifyAll()} method.
     * @param obj the object threads are waiting for
     * @see Object#notifyAll()
     */
    public static void notifyAll(Object obj) {
        obj.notifyAll();
    }

    /**
     * Waits to be notified on an object.
     * monitor.
     * <p/>
     * Calling this method on a given object is equivalent calling that object's {@code wait()} method.
     * @param obj the object on which to wait
     * @see Object#wait()
     */
    public static void wait(Object obj) throws InterruptedException {
        obj.wait();
    }

    /**
     * Waits to be notified on an object or until a specified amount of of time elapses.
     * <p/>
     * Calling this method on a given object is equivalent calling {@code unit.timedWait(obj, time)}.
     * @param obj the object on which to wait
     * @param timeout the maximum time to wait
     * @param unit the time util of {@code timeout}
     * @see TimeUnit#timedWait(Object, long)
     */
    public static void wait(Object obj, long timeout, TimeUnit unit) throws InterruptedException {
        unit.timedWait(obj, timeout);
    }
}