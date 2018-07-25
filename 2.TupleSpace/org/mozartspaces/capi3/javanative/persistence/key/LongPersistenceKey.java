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

/**
 * A persistence key that can wrap long values.
 *
 * @author Jan Zarnikov
 */
public final class LongPersistenceKey implements PersistenceKey<Long> {

    private final long value;

    /**
     * Create a new persistence key from a long value.
     *
     * @param value
     *            the actual key.
     */
    public LongPersistenceKey(final long value) {
        this.value = value;
    }

    /**
     * Create a new persistence key from its binary representation (according to {@link java.nio.ByteBuffer#getLong()}).
     *
     * @param data
     *            the binary representation of the key.
     * @return a new persistence key.
     */
    public static LongPersistenceKey fromByteArray(final byte[] data) {
        return new LongPersistenceKey(ByteBuffer.wrap(data).getLong());
    }

    @Override
    public byte[] asByteArray() {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    @Override
    public boolean isConvertibleToLong() {
        return true;
    }

    @Override
    public long asLong() {
        return value;
    }

    @Override
    public String asString() {
        return Long.toString(value);
    }

    @Override
    public boolean isConvertibleToString() {
        return true;
    }

    @Override
    public Long getKey() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LongPersistenceKey that = (LongPersistenceKey) o;
        if (value != that.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    /**
     * A persistence key factory for creating new long persistence keys.
     */
    public static final class LongPersistenceKeyFactory implements PersistenceKeyFactory<Long> {

        @Override
        public PersistenceKey<Long> createPersistenceKey(final Long key) {
            return new LongPersistenceKey(key);
        }

        @Override
        public PersistenceKey<Long> createPersistenceKeyFromByteArray(final byte[] data) {
            return LongPersistenceKey.fromByteArray(data);
        }

        @Override
        public PersistenceKey<Long> createPersistenceKeyFromLong(final long key) {
            return new LongPersistenceKey(key);
        }

        @Override
        public PersistenceKey<Long> createPersistenceKeyFromString(final String key) {
            return new LongPersistenceKey(Long.parseLong(key));
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
