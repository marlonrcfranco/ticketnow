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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.util.NamedThreadFactory;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Generic polling Timeout Processor for elements that time out. Internally a
 * {@link ConcurrentSkipListMap} is used for storing the elements and task is
 * scheduled at a fixed rate in {@link ScheduledExecutorService} which removes
 * timed out elements from the map and passes them to the timeout handler.
 *
 * @author Tobias Doenz
 *
 * @param <E>
 *            type of the element that expires (i.e., times out)
 */
@ThreadSafe
public final class PollingTimeoutProcessor<E> implements TimeoutProcessor<E> {

    private static final Logger log = LoggerFactory.get();

    private final String name;
    private final ConcurrentNavigableMap<ExpiringElement<E>, ExpiringElement<E>> elements;
    private final ScheduledExecutorService executor;

    private volatile TimeoutHandler<E> timeoutHandler;

    /**
     * Constructs a <code>PollingTimeoutProcessor</code>.
     *
     * @param name
     *            the timeout processor name, used as name for the internal
     *            thread
     */
    public PollingTimeoutProcessor(final String name) {
        this.name = name;
        elements = new ConcurrentSkipListMap<ExpiringElement<E>, ExpiringElement<E>>();
        executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
        // TODO make initialDelay and period configurable
        executor.scheduleAtFixedRate(new TimeoutProcessingTask(), 100, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setTimeoutHandler(final TimeoutHandler<E> timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
        assert this.timeoutHandler != null;
    }

    @Override
    public void addElement(final ExpiringElement<E> element) {
        elements.put(element, element);
        // TODO throw exception for duplicate element?
//        ExpiringElement<E> prev = elements.putIfAbsent(element, element);
//        if (prev != null) {
//            System.out.println("Element already in list");
//            // throw new IllegalArgumentException("Element already in list");
//        }
    }

    @Override
    public boolean removeElement(final ExpiringElement<E> element) {
        return elements.remove(element) != null;
    }

    @Override
    public void shutdown() {
        log.debug("Shutting down the Timeout Processor {}", name);
        executor.shutdown();
    }

    /**
     * Task that processes timeouts.
     *
     * @author Tobias Doenz
     */
    private final class TimeoutProcessingTask implements Runnable {

        private final Object dummy = new Object();

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            long currentTime = System.nanoTime();
            @SuppressWarnings("rawtypes")
            ExpiringElement toKey = new ExpiringElement(dummy, currentTime);
            ConcurrentNavigableMap<ExpiringElement<E>, ExpiringElement<E>> timeouts = elements.headMap(toKey, true);
            Iterator<Map.Entry<ExpiringElement<E>, ExpiringElement<E>>> it = timeouts.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ExpiringElement<E>, ExpiringElement<E>> element = it.next();
                timeoutHandler.elementTimedOut(element.getValue().getElement());
                it.remove();
            }
        }

    }
}
