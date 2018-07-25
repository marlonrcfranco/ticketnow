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
package org.mozartspaces.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@code ExecutorService} without any threading, it executes all tasks
 * directly. So, in contrast to other executor services, the task execution is
 * synchronous and blocking.
 * <p>
 * This class implements only the method {@link #execute(Runnable)} and the
 * methods to manage termination, all other methods throw an {@code
 * UnsupportedOperationException}.
 *
 * @author Tobias Doenz
 */
public final class WithinThreadExecutorService implements ExecutorService {

    private volatile boolean shutdown;

    @Override
    public void execute(final Runnable command) {
        command.run();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return Collections.emptyList();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return true;
    }


    // the methods below throw an UnsupportedOperationException
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException,
            ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<?> submit(final Runnable task) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        throw new UnsupportedOperationException();
    }

}
