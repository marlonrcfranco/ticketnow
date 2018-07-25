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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.ref.WeakReference;

import org.mozartspaces.core.MzsCoreRuntimeException;

/**
 * @author Jan Zarnikov
 */
public class LazyNativeEntry extends AbstractNativeEntry implements Externalizable, Comparable<NativeEntry> {

    private transient WeakReference<NativeEntry> wrapped;

    private transient NativeContainer container;

    /**
     * For externalization.
     */
    public LazyNativeEntry() {
        // for Externalizable
    }

    /**
     *
     * @param wrapped
     *            the entry to wrap
     * @param container
     *            the container
     */
    public LazyNativeEntry(final NativeEntry wrapped, final NativeContainer container) {
        super(wrapped.getEntryId());
        // avoid double-wrapping
        if (wrapped instanceof LazyNativeEntry) {
            this.wrapped = new WeakReference<NativeEntry>(((LazyNativeEntry) wrapped).getWrapped());
        } else {
            this.wrapped = new WeakReference<NativeEntry>(wrapped);
        }
        this.container = container;
    }

    /**
     * @param wrapped
     *            the entry to wrap
     */
    public LazyNativeEntry(final NativeEntry wrapped) {
        this(wrapped, wrapped.getContainer());
    }

    /**
     * @param entryId
     *            the ID of the entry
     * @param container
     *            the container
     */
    public LazyNativeEntry(final long entryId, final NativeContainer container) {
        super(entryId);
        this.container = container;
    }

    protected final NativeEntry getWrapped() {
        NativeEntry entry = (wrapped == null) ? null : wrapped.get();
        if (entry == null) {
            entry = container.getEntry(getEntryId());
            if (entry == null) {
                throw new MzsCoreRuntimeException("Entry " + getEntryId() + " not found in container "
                        + container.getId());
            }
            wrapped = new WeakReference<NativeEntry>(entry);
        }
        return entry;
    }

    @Override
    public final Serializable getData() {
        return getWrapped().getData();
    }

    @Override
    public final NativeContainer getContainer() {
        return container;
    }

    @Override
    public final int compareTo(final NativeEntry nativeEntry) {
        return (int) (getEntryId() - nativeEntry.getEntryId());
    }

    @Override
    public final String toString() {
        return "LazyNativeEntry [wrapped=" + getWrapped() + "]";
    }

    // custom serialization for more performance!

    @Override
    public final void writeExternal(final ObjectOutput objectOutput) throws IOException {
        objectOutput.writeLong(getEntryId());
    }

    @Override
    public final void readExternal(final ObjectInput objectInput) throws IOException, ClassNotFoundException {
        setEntryId(objectInput.readLong());
    }


}
