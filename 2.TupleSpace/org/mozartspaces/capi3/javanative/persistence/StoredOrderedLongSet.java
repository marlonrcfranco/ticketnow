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
 * An implementation of the {@link OrderedLongSet} that uses a {@link StoredMap} to store its data.
 *
 * @author Jan Zarnikov
 */
public final class StoredOrderedLongSet implements OrderedLongSet {

    private final StoredMap<Long, long[]> data;

    private volatile long first, last;

    private static final long FIRST_LAST = -1;

    /**
     * Create a new stored ordered long set with the given stored map.
     * @param data a stored map that will be used to hold the data of this set.
     */
    public StoredOrderedLongSet(final StoredMap<Long, long[]> data) {
        this.data = data;
        long[] storedFirstLast = data.get(FIRST_LAST);
        if (storedFirstLast != null) {
            first = storedFirstLast[0];
            last = storedFirstLast[1];
        } else {
            first = -1;
            last = -1;
            updateFirstLast();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insertBefore(final long beforeValue, final long value) {
        long[] links = data.get(beforeValue);
        if (links == null) {
            throw new NoSuchElementException();
        }
        if (beforeValue == first) {
            prepend(value);
        } else {
            long before = links[0];
            long after = beforeValue;
            long[] newLinks = new long[] {before, after};
            long[] newBeforeLinks = data.get(before);
            newBeforeLinks[1] = value;
            long[] newAfterLinks = data.get(after);
            newAfterLinks[0] = value;
            data.put(before, newBeforeLinks);
            data.put(after, newAfterLinks);
            data.put(value, newLinks);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insertAfter(final long afterValue, final long value) {
        long[] links = data.get(afterValue);
        if (links == null) {
            throw new NoSuchElementException();
        }
        if (afterValue == last) {
            append(value);
        } else {
            long before = afterValue;
            long after = links[1];
            long[] newLinks = new long[] {before, after};
            long[] newAfterLinks = data.get(after);
            newAfterLinks[0] = value;
            links[1] = value;
            data.put(before, links);
            data.put(value, newLinks);
            data.put(after, newAfterLinks);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void append(final long value) {
        if (last == -1) {
            insertVeryFirst(value);
        } else {
            long[] oldLastLinks = data.get(last);
            oldLastLinks[1] = value;
            data.put(last, oldLastLinks);
            long[] newLastLinks = new long[] {last, -1};
            data.put(value, newLastLinks);
            last = value;
            updateFirstLast();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void prepend(final long value) {
        if (first == -1) {
            insertVeryFirst(value);
        } else {
            long[] oldFirstLinks = data.get(first);
            oldFirstLinks[0] = value;
            data.put(first, oldFirstLinks);
            long[] newFirstLinks = new long[] {-1, first};
            data.put(value, newFirstLinks);
            first = value;
            updateFirstLast();
        }
    }

    private void insertVeryFirst(final long value) {
        first = value;
        last = value;
        long[] firstLinks = new long[] {-1, -1};
        data.put(first, firstLinks);
        updateFirstLast();
    }

    private void updateFirstLast() {
        data.put(FIRST_LAST, new long[] {first, last});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove(final long value) {
        long[] links = data.get(value);
        if (links == null) {
            throw new NoSuchElementException();
        }
        if (value == first && value == last) {
            clear();
        } else if (value == first) {
            long newFirst = links[1];
            long[] newFirstLinks = data.get(newFirst);
            newFirstLinks[0] = -1;
            data.put(newFirst, newFirstLinks);
            data.remove(first);
            first = newFirst;
            updateFirstLast();
        } else if (value == last) {
            long newLast = links[0];
            long[] newLastLinks = data.get(newLast);
            newLastLinks[1] = -1;
            data.put(newLast, newLastLinks);
            data.remove(last);
            last = newLast;
            updateFirstLast();
        } else {
            long[] prevLinks = data.get(links[0]);
            long[] nextLinks = data.get(links[1]);
            prevLinks[1] = links[1];
            nextLinks[0] = links[0];
            data.put(links[0], prevLinks);
            data.put(links[1], nextLinks);
            data.remove(value);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int size() {
        return data.size() - 1;
    }

    @Override
    public synchronized void clear() {
        data.clear();
        first = -1;
        last = -1;
        updateFirstLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void destroy() {
        data.destroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() {
        data.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Iterator<Long> iterator() {
        return new SortedOrderedLongSetIterator();
    }

    /**
     * An implementation of {@link Iterator} for iterating the ordered set. This iterator does not support
     * modifications.
     */
    private final class SortedOrderedLongSetIterator implements Iterator<Long> {

        private long current;
        private long[] links;

        private SortedOrderedLongSetIterator() {
            current = first;
            if (current == -1) {
                links = new long[] {-1, -1};
            } else {
                links = data.get(current);
            }
        }

        @Override
        public boolean hasNext() {
            return current != -1;
        }

        @Override
        public Long next() {
            if (current == -1) {
                throw new NoSuchElementException();
            }
            long oldCurrent = current;
            current = links[1];
            if (current != -1) {
                links = data.get(current);
            }
            return oldCurrent;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
