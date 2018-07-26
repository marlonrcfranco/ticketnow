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

/**
 * A persistence key that wraps a class as the actual key object.
 *
 * @author Jan Zarnikov
 */
public final class ClassPersistenceKey implements PersistenceKey<Class<?>> {

    private final Class<?> clazz;

    private final StringPersistenceKey stringPeristenceKey;

    /**
     * Create a new persistence key from a class.
     *
     * @param clazz
     *            the actual key object
     */
    public ClassPersistenceKey(final Class<?> clazz) {
        this.clazz = clazz;
        this.stringPeristenceKey = new StringPersistenceKey(clazz.getName());
    }

    private ClassPersistenceKey(final StringPersistenceKey stringPersistenceKey) throws ClassNotFoundException {
        this.stringPeristenceKey = stringPersistenceKey;
        this.clazz = Class.forName(this.stringPeristenceKey.getKey());
    }

    /**
     * Create a new persistence key from a class name serialized as {@code byte[]}.
     *
     * @param data
     *            a name of the class serialized with default encoding.
     * @return a new persistence key or {@code null} if the class name cannot be resolved.
     */
    public static ClassPersistenceKey fromByteArray(final byte[] data) {
        try {
            return new ClassPersistenceKey(StringPersistenceKey.fromByteArray(data));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] asByteArray() {
        return stringPeristenceKey.asByteArray();
    }

    @Override
    public boolean isConvertibleToLong() {
        return false;
    }

    @Override
    public boolean isConvertibleToString() {
        return true;
    }

    @Override
    public String asString() {
        return stringPeristenceKey.asString();
    }

    @Override
    public long asLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getKey() {
        return clazz;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassPersistenceKey that = (ClassPersistenceKey) o;
        if (stringPeristenceKey != null ? !stringPeristenceKey.equals(that.stringPeristenceKey)
                : that.stringPeristenceKey != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return stringPeristenceKey != null ? stringPeristenceKey.hashCode() : 0;
    }

    /**
     * A persistence key factory for creating class peristence keys from their serialized from.
     */
    public static final class ClassPersistenceKeyFactory implements PersistenceKeyFactory<Class<?>> {

        @Override
        public PersistenceKey<Class<?>> createPersistenceKey(final Class<?> key) {
            return new ClassPersistenceKey(key);
        }

        @Override
        public PersistenceKey<Class<?>> createPersistenceKeyFromByteArray(final byte[] data) {
            return ClassPersistenceKey.fromByteArray(data);
        }

        @Override
        public PersistenceKey<Class<?>> createPersistenceKeyFromLong(final long key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PersistenceKey<Class<?>> createPersistenceKeyFromString(final String key) {
            try {
                return new ClassPersistenceKey(new StringPersistenceKey(key));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not reload key for class name \"" + key + "\"", e);
            }
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
