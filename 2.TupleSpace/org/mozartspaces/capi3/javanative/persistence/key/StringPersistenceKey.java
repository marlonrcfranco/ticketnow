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

import java.nio.charset.Charset;

/**
 * A persistence key that wraps a String value.
 *
 * @author Jan Zarnikov
 */
public final class StringPersistenceKey implements PersistenceKey<String> {

    private final String key;

    /**
     * Create a new persistence key from a string.
     *
     * @param key
     *            a string key
     */
    public StringPersistenceKey(final String key) {
        this.key = key;
    }

    /**
     * Create a new persistence key from the {@code byte[]} using the default encoding.
     *
     * @param data
     *            the serialized data of the string key
     * @return a new persistence key wrapping the string
     */
    public static StringPersistenceKey fromByteArray(final byte[] data) {
        return new StringPersistenceKey(new String(data, Charset.defaultCharset()));
    }

    @Override
    public byte[] asByteArray() {
        return key.getBytes(Charset.defaultCharset());
    }

    @Override
    public boolean isConvertibleToLong() {
        return false;
    }

    @Override
    public long asLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String asString() {
        return key;
    }

    @Override
    public boolean isConvertibleToString() {
        return true;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringPersistenceKey that = (StringPersistenceKey) o;
        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    /**
     * An implementation of persistence key factory for creating string persistence keys.
     */
    public static final class StringPersistenceKeyFactory implements PersistenceKeyFactory<String> {

        @Override
        public PersistenceKey<String> createPersistenceKey(final String key) {
            return new StringPersistenceKey(key);
        }

        @Override
        public PersistenceKey<String> createPersistenceKeyFromByteArray(final byte[] data) {
            return StringPersistenceKey.fromByteArray(data);
        }

        @Override
        public PersistenceKey<String> createPersistenceKeyFromLong(final long key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PersistenceKey<String> createPersistenceKeyFromString(final String key) {
            return new StringPersistenceKey(key);
        }

        @Override
        public boolean canConvertFromLong() {
            return false;
        }

        @Override
        public boolean canConvertFromString() {
            return true;
        }
    }
}
