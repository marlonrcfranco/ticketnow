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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.util.MzsCloneable;

/**
 * Represents the combination of an object that is written to the space and the
 * coordination data that is used for this write operation. Instances of
 * <code>Entry</code> are not directly stored in the space, only their value.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class Entry implements Serializable, MzsCloneable {

    private static final long serialVersionUID = 1L;

    private final Serializable value;
    private final Set<? extends CoordinationData> coordData;

    /**
     * Constructs an <code>Entry</code>.
     *
     * @param value
     *            the value, must not be <code>null</code>
     * @param coordData
     *            the coordination data collection, must not be <code>null</code>. Internally a HashSet is used.
     */
    public Entry(final Serializable value, final Collection<? extends CoordinationData> coordData) {
        this.value = value;
        if (this.value == null) {
            throw new NullPointerException("Value is null");
        }
        if (coordData == null) {
            throw new NullPointerException("Coordination data collection is null");
        }
        this.coordData = new HashSet<CoordinationData>(coordData);
    }

    /**
     * Constructs an <code>Entry</code>.
     *
     * @param value
     *            the value, must not be <code>null</code>
     * @param coordData
     *            the coordination data, must not be <code>null</code>, duplicate elements are included only once
     */
    public Entry(final Serializable value, final CoordinationData... coordData) {
        this(value, Arrays.asList(coordData));
    }

    /**
     * Constructs an <code>Entry</code> with an empty coordination data collection.
     *
     * @param value
     *            the value, must not be <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public Entry(final Serializable value) {
        this(value, Collections.EMPTY_SET);
    }

    // for serialization
    @SuppressWarnings("unused")
    private Entry() {
        this.coordData = null;
        this.value = null;
    }

    /**
     * @return the value
     */
    public Serializable getValue() {
        return value;
    }

    /**
     * @return an unmodifiable view of the coordination data set
     */
    public Set<CoordinationData> getCoordinationData() {
        return Collections.unmodifiableSet(coordData);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coordData == null) ? 0 : coordData.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Entry other = (Entry) obj;
        if (coordData == null) {
            if (other.coordData != null) {
                return false;
            }
        } else if (!coordData.equals(other.coordData)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entry [value=" + value + ", coordData=" + coordData + "]";
    }

    @Override
    public Entry clone() throws CloneNotSupportedException {
          Serializable clonedValue = null;
        try {
            clonedValue = (Serializable) ((MzsCloneable) value).clone();
        } catch (ClassCastException ex) {
            throw new CloneNotSupportedException(ex.toString());
        }
        return new Entry(clonedValue, coordData);
    }

}
