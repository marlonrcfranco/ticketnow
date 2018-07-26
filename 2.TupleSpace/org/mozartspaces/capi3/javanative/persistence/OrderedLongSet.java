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

/**
 * A set of ordered long values. For performance reasons this set does <b>not check for duplicates!</b> The caller of
 * all inserting methods ({@link OrderedLongSet#insertBefore(long, long)},
 * {@link OrderedLongSet#insertAfter(long, long)}, {@link OrderedLongSet#append(long)} and
 * {@link OrderedLongSet#prepend(long)}) has to make sure that the value has not yet been added to the set. <b>The
 * behaviour of this set is undefined if duplicates are added.</b><br/>
 * <br/>
 *
 * The set supports <b>only positive values </b> (including 0).
 *
 * @author Jan Zarnikov
 */
public interface OrderedLongSet extends Iterable<Long> {
    /**
     * Insert a new value before a specified value.
     *
     * @param beforeValue
     *            the value before which the new value should be inserted
     * @param value
     *            the new value
     * @throws java.util.NoSuchElementException
     *             if the <code>beforeValue</code> is not stored in this set.
     */
    void insertBefore(long beforeValue, long value);

    /**
     * Insert a new value after a specified value.
     *
     * @param afterValue
     *            the value after which the new value should be inserted
     * @param value
     *            the new value
     * @throws java.util.NoSuchElementException
     *             if the <code>beforeValue</code> is not stored in this set.
     */
    void insertAfter(long afterValue, long value);

    /**
     * Insert a new value at the end.
     *
     * @param value
     *            the new value to be inserted.
     */
    void append(long value);

    /**
     * Insert a new value at the beginning.
     *
     * @param value
     *            the new value to be inserted.
     */
    void prepend(long value);

    /**
     * Remove a value from the set.
     *
     * @param value
     *            the value to be removed
     * @throws java.util.NoSuchElementException
     *             if the <code>value</code> is not stored in this set.
     */
    void remove(long value);

    /**
     * The current number of values stored in the set.
     *
     * @return number of elements.
     */
    int size();

    /**
     * Remove all values from this set.
     */
    void clear();

    /**
     * Remove all values from this set and release all resources used by it. Afterwards the set becomes unusable. It is
     * not necessary to call {@link org.mozartspaces.capi3.javanative.persistence.OrderedLongSet#close()} before calling
     * this method (but not forbidden either).
     */
    void destroy();

    /**
     * Close this set and release all resources used by it. Afterwards the set becomes unusable.
     */
    void close();

    /**
     * Return an Iterator over this set. Note that the iterator is not thread-safe and do not modify this set while
     * iterating it. Also the iterator is read-only (the method {@link java.util.Iterator#remove()} is not supported).
     *
     * @return an iterator of the values in the set
     */
    @Override
    Iterator<Long> iterator();
}
