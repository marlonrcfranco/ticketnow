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
package org.mozartspaces.runtime.blocking;

import java.util.concurrent.DelayQueue;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Generic non-polling Timeout Processor for elements that time out. Internally
 * a {@link DelayQueue} is used for storing the elements and a thread is
 * blockingly taking elements from this queue and passing them to the timeout
 * handler.
 *
 * @author Tobias Doenz
 *
 * @param <E>
 *            type of the element that expires (i.e., times out)
 */
@ThreadSafe
public final class NonPollingTimeoutProcessor<E> implements TimeoutProcessor<E> {

    private static final Logger log = LoggerFactory.get();

    private final DelayQueue<ExpiringElement<E>> elements;
    private final Thread thread;

    private volatile TimeoutHandler<E> timeoutHandler;

    /**
     * Constructs a <code>NonPollingTimeoutProcessor</code>.
     *
     * @param name
     *            the timeout processor name, used as name for the internal
     *            thread
     */
    public NonPollingTimeoutProcessor(final String name) {
        elements = new DelayQueue<ExpiringElement<E>>();
        thread = new TimeoutProcessingThread();
        thread.setName(name);
        thread.start();
    }

    @Override
    public void setTimeoutHandler(final TimeoutHandler<E> timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
        assert this.timeoutHandler != null;
    }

    @Override
    public void addElement(final ExpiringElement<E> element) {
        elements.add(element);
    }

    @Override
    public boolean removeElement(final ExpiringElement<E> element) {
        return elements.remove(element);
    }

    /**
     * {@inheritDoc} The internal timeout processor thread is interrupted.
     */
    @Override
    public void shutdown() {
        log.debug("Shutting down the {}", thread.getName());
        thread.interrupt();
    }

    /**
     * Thread that blockingly takes element from the delay queue and passes them
     * to the timeout handler.
     *
     * @author Tobias Doenz
     */
    private final class TimeoutProcessingThread extends Thread {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    E element = elements.take().getElement();
                    timeoutHandler.elementTimedOut(element);
                }
            } catch (InterruptedException e) {
                log.debug("Thread interrupted, shutting down");
            }
        }

    }
}
