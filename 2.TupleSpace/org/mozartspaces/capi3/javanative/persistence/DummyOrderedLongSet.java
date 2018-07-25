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
package org.mozartspaces.capi3.javanative.persistence;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A dummy implementation of the {@link OrderedLongSet} interface. No values are stored (neither persistently
 * nor in memory). The size is always 0 and the iterator is always empty.
 *
 * @author Jan Zarnikov
 */
public final class DummyOrderedLongSet implements OrderedLongSet {

    @Override
    public void insertBefore(final long beforeValue, final long value) {
    }

    @Override
    public void insertAfter(final long afterValue, final long value) {
    }

    @Override
    public void append(final long value) {
    }

    @Override
    public void prepend(final long value) {
    }

    @Override
    public void remove(final long value) {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Long next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
            }
        };
    }
}
