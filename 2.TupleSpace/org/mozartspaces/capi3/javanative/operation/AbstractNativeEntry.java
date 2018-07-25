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
package org.mozartspaces.capi3.javanative.operation;

/**
 * An abstract implementation of {@link NativeEntry} that takes care of {@link Object#equals(Object)} and
 * {@link Object#hashCode()} based on the entry ID.
 *
 * @author Jan Zarnikov
 */
public abstract class AbstractNativeEntry implements NativeEntry {

    private static final long serialVersionUID = 1L;

    private long entryId;

    protected AbstractNativeEntry() {
    }

    /**
     * Create a new entry with the given ID.
     * @param entryId the ID of the entry
     */
    protected AbstractNativeEntry(final long entryId) {
        this.entryId = entryId;
    }

    @Override
    public final long getEntryId() {
        return entryId;
    }

    /**
     * Set the entry ID.
     * The entry should be treated as immutable. This method should be called only during deserialization.
     * @param entryId
     */
    protected final void setEntryId(final long entryId) {
        this.entryId = entryId;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (entryId ^ (entryId >>> 32));
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NativeEntry)) {
            return false;
        }
        NativeEntry other = (NativeEntry) obj;
        if (entryId != other.getEntryId()) {
            return false;
        }
        return true;
    }
}
