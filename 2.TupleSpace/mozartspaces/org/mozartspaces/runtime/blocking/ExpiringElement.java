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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

/**
 * An object/element that expires at some time in the future. It consists of the
 * element itself and the expire time.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Tobias Doenz
 *
 * @param <E>
 *            the type of the element that expires
 */
@Immutable
public final class ExpiringElement<E> implements Delayed {

    private final E element;
    private final long expireTime;

    /**
     * Constructs an expiring element.
     *
     * @param element
     *            the element that expires
     * @param expireTime
     *            the timer value, calculated with the value of
     *            {@link System#nanoTime()} and a timeout value, when the
     *            element expires.
     */
    public ExpiringElement(final E element, final long expireTime) {
        this.element = element;
        assert this.element != null;
        this.expireTime = expireTime;
    }

    /**
     * Gets the element that expires.
     *
     * @return the element
     */
    public E getElement() {
        return element;
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        long duration = expireTime - System.nanoTime();
        return unit.convert(duration, TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(final Delayed other) {
        if (other.getClass() != getClass()) {
            throw new ClassCastException();
        }
        ExpiringElement<?> otherElement = (ExpiringElement<?>) other;
        if (this.expireTime > otherElement.expireTime) {
            return 1;
        }
        if (this.expireTime < otherElement.expireTime) {
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExpiringElement<?> other = (ExpiringElement<?>) obj;
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        return true;
    }

}
