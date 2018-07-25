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

import net.jcip.annotations.ThreadSafe;

/**
 * Manages and processes elements that time out. Such expiring elements can be added
 * and removed, if they time out, they are passed to the timeout handler.
 *
 * @author Tobias Doenz
 *
 * @param <E>
 *            type of the element that expires (i.e., times out)
 */
@ThreadSafe
public interface TimeoutProcessor<E> {

    /**
     * Adds an element to the timeout processor.
     *
     * @param element
     *            the element to add
     */
    void addElement(ExpiringElement<E> element);

    /**
     * Removes an element from the timeout processor.
     *
     * @param element
     *            the element to remove
     * @return {@code true} if the element was removed
     */
    boolean removeElement(ExpiringElement<E> element);

    /**
     * Shuts down the timeout processor.
     */
    void shutdown();

    /**
     * Sets the timeout handler that is called when an element times out.
     *
     * @param timeoutHandler
     *            the timeout handler
     */
    void setTimeoutHandler(TimeoutHandler<E> timeoutHandler);

}
