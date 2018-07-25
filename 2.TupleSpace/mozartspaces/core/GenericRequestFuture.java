/**
 * MozartSpaces - Java implementation of Extensible Virtual Shared Memory (XVSM)
 * Copyright 2009-2013 Space Based Computing Group, eva Kuehn, E185/1, TU Vienna
 * Visit http://www.mozartspaces.org for more information.
 *
 * MozartSpaces is free software: you can redistribute it and/or
 * modify it under the terms of version 3 of the GNU Affero General
 * Public License as published by the Free Software Foundation.
 *
 * MozartSpaces is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with MozartSpaces. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.mozartspaces.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A generic implementation of <code>RequestFuture</code> which does not support
 * cancellation.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type
 */
// similar to ValueLatch in JCiP Listing 8.17
@ThreadSafe
public final class GenericRequestFuture<R extends Serializable> implements RequestFuture<R> {

    private final CountDownLatch done = new CountDownLatch(1);

    /**
     * Object that stores the caller stack trace, used to enhance the stack
     * trace of an error that is set to this future, if any occurs and this
     * field's value is not <code>null</code>.
     */
    private final Throwable callerThrowable;

    @GuardedBy("this")
    private R result;

    @GuardedBy("this")
    private Throwable error;

    /**
     * Constructs a <code>GenericRequestFuture</code>.
     *
     * @param storeStackTrace
     *            <code>true</code> if the calling stack trace should be stored
     *            and added to the stack trace of an error (exception) that is
     *            set to this future (if any occurs), <code>false</code>
     *            otherwise
     */
    public GenericRequestFuture(final boolean storeStackTrace) {
        // TODO make configurable (actually in MzsCore)
        callerThrowable = storeStackTrace ? new Throwable() : null;
    }

    @Override
    public boolean isDone() {
        return done.getCount() == 0;
    }

    @Override
    public R getResult() throws MzsCoreException, InterruptedException {
        done.await();
        return getResultOrError();
    }

    @Override
    public R getResult(final long timeoutInMilliseconds) throws MzsCoreException, InterruptedException,
            TimeoutException {
        if (!done.await(timeoutInMilliseconds, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException();
        }
        return getResultOrError();
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        try {
            return getResult();
        } catch (MzsCoreException ex) {
            throw new ExecutionException(ex);
        } catch (RuntimeException ex) {
            throw new ExecutionException(ex);
        }
    }

    @Override
    public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        try {
            return getResult(unit.toMillis(timeout));
        } catch (MzsCoreException ex) {
            throw new ExecutionException(ex);
        } catch (RuntimeException ex) {
            throw new ExecutionException(ex);
        }
    }

    private R getResultOrError() throws MzsCoreException {
        synchronized (this) {
            if (error != null) {
                enhanceStackTrace(error);
                if (error instanceof MzsCoreException) {
                    throw (MzsCoreException) error;
                } else if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                } else {
                    throw new MzsCoreException(error);
                }
            }
            return result;
        }
    }

    /**
     * Sets the result.
     *
     * @param result
     *            the result
     */
    public void setResult(final R result) {
        // not whole method synchronized to prevent FindBugs warning
        synchronized (this) {
            if (isDone()) {
                throw new IllegalStateException("Result or error already set");
            }
            this.result = result;
            done.countDown();
        }
    }

    /**
     * Sets the error.
     *
     * @param error
     *            the error
     */
    public synchronized void setError(final Throwable error) {
        if (isDone()) {
            throw new IllegalStateException("Result or error already set");
        }
        this.error = error;
        done.countDown();
    }

    private void enhanceStackTrace(final Throwable error) {
        StackTraceElement[] callerStackTrace = null;
        if (callerThrowable != null) {
            callerStackTrace = new StackTraceElement[] {
                    new StackTraceElement("[Request sent with the following stack trace]", "", null, -1)
            };
            callerStackTrace = concatenateArrays(callerStackTrace, callerThrowable.getStackTrace());
        } else {
            callerStackTrace = new StackTraceElement[0];
        }
        StackTraceElement[] retrieverStackTrace = new StackTraceElement[] {
                new StackTraceElement("[Request result retrieved with the following stack trace]", "", null, -1)
        };
        retrieverStackTrace = concatenateArrays(retrieverStackTrace, new Throwable().getStackTrace());

        error.setStackTrace(concatenateArrays(error.getStackTrace(), callerStackTrace, retrieverStackTrace));
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] concatenateArrays(final T[]... arrays) {
        int length = 0;
        for (T[] array : arrays) {
            length += array.length;
        }
        T[] concatArray = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), length);
        int concatArrayPos = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, concatArray, concatArrayPos, array.length);
            concatArrayPos += array.length;
        }
        return concatArray;
    }

}
