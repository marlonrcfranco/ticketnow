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
package org.mozartspaces.capi3.javanative.persistence.key;

import java.nio.ByteBuffer;

import org.mozartspaces.capi3.javanative.operation.LazyNativeEntry;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * A persistence key that uses the entry ID (long) as the key value.
 *
 * @author Jan Zarnikov
 */
public final class NativeEntryPersistenceKey extends LazyNativeEntry implements PersistenceKey<NativeEntry> {

    /**
     * Create a new persistence key from the given entry stored in a container.
     * @param wrapped the entry
     * @param container the container in which the entry is stored.
     */
    public NativeEntryPersistenceKey(final NativeEntry wrapped, final NativeContainer container) {
        super(wrapped, container);
    }

    /**
     * Create a new persistence key from the given entry ID stored in a container.
     * @param id the ID of the entry
     * @param container the container in which the entry is stored.
     */
    private NativeEntryPersistenceKey(final long id, final NativeContainer container) {
        super(id, container);
    }

    /**
     * Create a new persistence key from the serialized value of the ID which is stored in the given container.
     * @param data the serialized ID (long)
     * @param container the container in which the entry is stored
     * @return a new persistence key representing the entry
     */
    public static NativeEntryPersistenceKey fromByteArray(final byte[] data, final NativeContainer container) {
        return new NativeEntryPersistenceKey(ByteBuffer.wrap(data).getLong(), container);
    }

    @Override
    public NativeEntry getKey() {
        return this;
    }

    @Override
    public boolean isConvertibleToLong() {
        return true;
    }

    @Override
    public long asLong() {
        return getEntryId();
    }

    @Override
    public byte[] asByteArray() {
        return ByteBuffer.allocate(8).putLong(getEntryId()).array();
    }

    @Override
    public String asString() {
        return Long.toString(getEntryId());
    }

    @Override
    public boolean isConvertibleToString() {
        return true;
    }

    /**
     * A persistence key factory that can create persistence keys for entries stored in one container.
     */
    public static final class NativeEntryPersistenceKeyFactory implements PersistenceKeyFactory<NativeEntry> {

        private final NativeContainer container;

        /**
         * Create a new persistence key factory for the given container.
         * @param container a container with entries that shall be used as keys.
         */
        public NativeEntryPersistenceKeyFactory(final NativeContainer container) {
            this.container = container;
        }

        @Override
        public PersistenceKey<NativeEntry> createPersistenceKey(final NativeEntry key) {
            return new NativeEntryPersistenceKey(key, container);
        }

        @Override
        public PersistenceKey<NativeEntry> createPersistenceKeyFromByteArray(final byte[] data) {
            return NativeEntryPersistenceKey.fromByteArray(data, container);
        }

        @Override
        public PersistenceKey<NativeEntry> createPersistenceKeyFromLong(final long key) {
            return new NativeEntryPersistenceKey(key, container);
        }

        @Override
        public PersistenceKey<NativeEntry> createPersistenceKeyFromString(final String key) {
            return new NativeEntryPersistenceKey(Long.parseLong(key), container);
        }

        @Override
        public boolean canConvertFromLong() {
            return true;
        }

        @Override
        public boolean canConvertFromString() {
            return true;
        }
    }
}
