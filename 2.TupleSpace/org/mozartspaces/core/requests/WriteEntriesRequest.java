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
package org.mozartspaces.core.requests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.util.Nothing;

/**
 * A <code>Request</code> to write (insert) entries to a container.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class WriteEntriesRequest extends EntriesRequest<Nothing> {

    private static final long serialVersionUID = 1L;

    private volatile List<Entry> entries;

    /**
     * Constructs a <code>WriteEntriesRequest</code>. Consider to use a
     * {@link #withContainer(ContainerReference) Builder}.
     *
     * @param entries
     *            the entry list
     * @param container
     *            the reference of the container where entries should be written
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public WriteEntriesRequest(final List<Entry> entries, final ContainerReference container,
            final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context) {

        super(container, timeoutInMilliseconds, transaction, isolation, context);
        setEntries(entries);
    }

    // for serialization
    private WriteEntriesRequest() {
        super(ContainerReference.DUMMY, 0, null, null, null);
        this.entries = null;
    }

    /**
     * Gets the entry list.
     *
     * @return the entry list
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Sets the entry list.
     *
     * @param entries
     *            the entry list
     */
    public void setEntries(final List<Entry> entries) {
        if (entries == null) {
            throw new NullPointerException("Entry list is null");
        }
        this.entries = new ArrayList<Entry>(entries);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entries == null) ? 0 : entries.hashCode());
        // from superclasses
        result = prime * result + ((getContainer() == null) ? 0 : getContainer().hashCode());
        result = prime * result + (int) (getTimeout() ^ (getTimeout() >>> 32));
        result = prime * result + ((getIsolation() == null) ? 0 : getIsolation().hashCode());
        result = prime * result + ((getTransaction() == null) ? 0 : getTransaction().hashCode());
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WriteEntriesRequest)) {
            return false;
        }
        WriteEntriesRequest other = (WriteEntriesRequest) obj;
        if (entries == null) {
            if (other.entries != null) {
                return false;
            }
        } else if (!entries.equals(other.entries)) {
            return false;
        }
        // from superclasses
        if (getContainer() == null) {
            if (other.getContainer() != null) {
                return false;
            }
        } else if (!getContainer().equals(other.getContainer())) {
            return false;
        }
        if (getTimeout() != other.getTimeout()) {
            return false;
        }
        if (getIsolation() == null) {
            if (other.getIsolation() != null) {
                return false;
            }
        } else if (!getIsolation().equals(other.getIsolation())) {
            return false;
        }
        if (getTransaction() == null) {
            if (other.getTransaction() != null) {
                return false;
            }
        } else if (!getTransaction().equals(other.getTransaction())) {
            return false;
        }
        if (getContext() == null) {
            if (other.getContext() != null) {
                return false;
            }
        } else if (!getContext().equals(other.getContext())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return "WriteEntriesRequest [entries="
                + (entries != null ? entries.subList(0, Math.min(entries.size(), maxLen)) : null) + ", container="
                + getContainer() + ", timeout=" + getTimeout() + ", isolation=" + getIsolation() + ", transaction="
                + getTransaction() + ", context=" + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a container reference.
     *
     * @param container
     *            the reference of the container where entries should be
     *            written. This parameter must not be <code>null</code>.
     * @return the builder with the container reference set
     */
    public static Builder withContainer(final ContainerReference container) {
        return new Builder(container);
    }

    /**
     * A class that helps to build a <code>WriteEntriesRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends EntriesRequest.Builder<WriteEntriesRequest.Builder, WriteEntriesRequest> {

        private List<Entry> entries;

        /**
         * Protected constructor, use the static factory method
         * {@link WriteEntriesRequest#withContainer(ContainerReference)}.
         */
        protected Builder(final ContainerReference container) {
            super(container);
        }

        /**
         * Sets the entries.
         *
         * @param entries
         *            the entry list. This parameter must not be <code>null</code>.
         * @return the builder
         */
        public Builder entries(final List<Entry> entries) {
            this.entries = entries;
            return this;
        }

        /**
         * Sets the entries.
         *
         * @param entries
         *            the entry array or single entry. This parameter must not
         *            be <code>null</code>.
         * @return the builder
         */
        public Builder entries(final Entry... entries) {
            this.entries = Arrays.asList(entries);
            return this;
        }

        /**
         * Creates and sets the entries. For each entry value an
         * <code>Entry</code> with the passed selector list is constructed and
         * added to the internal entry list. Note, that for all entries the same
         * selector list is used which may not be compatible with the semantics
         * of all coordinators (e.g., for the Key Coordinator the key, which is
         * passed with the selector, must be unique and cannot be used for more
         * than one entry).
         *
         * @param entryValues
         *            the entry value list. This parameter must not be
         *            <code>null</code>.
         * @param coordData
         *            the coordination data list. This parameter must not be
         *            <code>null</code> or empty.
         * @return the builder
         */
        public Builder entryValues(final List<? extends Serializable> entryValues,
                final List<? extends CoordinationData> coordData) {
            entries = new ArrayList<Entry>();
            if (entryValues == null) {
                throw new NullPointerException("Entry value list is null");
            }
            for (Serializable entryValue : entryValues) {
                entries.add(new Entry(entryValue, coordData));
            }
            return this;
        }

        /**
         * Creates and sets the entries. For each entry value an
         * <code>Entry</code> with the passed selector array is constructed and
         * added to the internal entry list. Note, that for all entries the same
         * selector array is used which may not be compatible with the semantics
         * of all coordinators (e.g., for the Key Coordinator the key, which is
         * passed with the selector, must be unique and cannot be used for more
         * than one entry).
         *
         * @param entryValues
         *            the entry value list. This parameter must not be
         *            <code>null</code>.
         * @param coordData
         *            the coordination data array. This parameter must not be
         *            <code>null</code> or empty.
         * @return the builder
         */
        public Builder entryValues(final List<? extends Serializable> entryValues,
                final CoordinationData... coordData) {
            entries = new ArrayList<Entry>();
            if (entryValues == null) {
                throw new NullPointerException("Entry value list is null");
            }
            for (Serializable entryValue : entryValues) {
                entries.add(new Entry(entryValue, coordData));
            }
            return this;
        }

        /**
         * Creates and sets the entries with the random selector.
         *
         * @param entryValues
         *            the entry value list. This parameter must not be
         *            <code>null</code>.
         * @return the builder
         */
        public Builder entryValues(final List<? extends Serializable> entryValues) {
            entries = new ArrayList<Entry>();
            if (entryValues == null) {
                throw new NullPointerException("Entry value list is null");
            }
            for (Serializable entryValue : entryValues) {
                entries.add(new Entry(entryValue));
            }
            return this;
        }

        /**
         * Creates and sets a single entry.
         *
         * @param entryValue
         *            the entry value. This parameter must not be
         *            <code>null</code>.
         * @param coordData
         *            the coordination data list. This parameter must not be
         *            <code>null</code> or empty.
         * @return the builder
         */
        public Builder entryValue(final Serializable entryValue, final List<? extends CoordinationData> coordData) {
            this.entries = Collections.singletonList(new Entry(entryValue, coordData));
            return this;
        }

        /**
         * Creates and sets a single entry.
         *
         * @param entryValue
         *            the entry value. This parameter must not be
         *            <code>null</code>.
         * @param coordData
         *            the coordination data array. This parameter must not be
         *            <code>null</code> or empty.
         * @return the builder
         */
        public Builder entryValue(final Serializable entryValue, final CoordinationData... coordData) {
            this.entries = Collections.singletonList(new Entry(entryValue, coordData));
            return this;
        }

        /**
         * Creates and sets a single entry with the random selector.
         *
         * @param entryValue
         *            the entry value. This parameter must not be
         *            <code>null</code>.
         * @return the builder
         */
        public Builder entryValue(final Serializable entryValue) {
            this.entries = Collections.singletonList(new Entry(entryValue));
            return this;
        }

        @Override
        public WriteEntriesRequest build() {
            return new WriteEntriesRequest(entries, getContainer(), getTimeout(), getTransaction(), getIsolation(),
                    getContext());
        }
    }
}
