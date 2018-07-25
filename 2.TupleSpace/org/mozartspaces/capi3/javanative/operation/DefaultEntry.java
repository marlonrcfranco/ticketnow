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

import java.io.Serializable;

/**
 * Implementation of {@code NativeEntry} with a reference to the container it
 * belongs to.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 */
public final class DefaultEntry extends AbstractNativeEntry {

    private static final long serialVersionUID = 1L;

    private final transient NativeContainer container;
    private final Serializable data;

    /**
     * Constructor for the <code>NativeEntry</code>.
     *
     * @param container
     *            The container this Entry belongs to
     * @param data
     *            The data the Entry is holding
     */
    DefaultEntry(final NativeContainer container, final Serializable data, final long entryId) {
        super(entryId);
        this.container = container;
        assert this.container != null;
        this.data = data;
        assert this.data != null;
    }

    @Override
    public Serializable getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "DefaultEntry [id=" + getEntryId() + ", data=" + data + "]";
    }

    @Override
    public NativeContainer getContainer() {
        return container;
    }
}
