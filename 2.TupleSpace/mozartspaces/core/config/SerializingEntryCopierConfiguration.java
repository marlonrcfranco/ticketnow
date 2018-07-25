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
package org.mozartspaces.core.config;

/**
 * Configuration of an entry copier that uses a serializer to copy objects.
 *
 * @author Tobias Doenz
 */
public final class SerializingEntryCopierConfiguration extends EntryCopierConfiguration {

    private static final long serialVersionUID = 1L;

    /**
     * Default serializing entry copier name.
     */
    public static final String NAME_DEFAULT = "serializing";

    /**
     * Default serializer (ID).
     */
    public static final String SERIALIZER_ID_DEFAULT = "javabuiltin";
    private volatile String serializerId = SERIALIZER_ID_DEFAULT;

    /**
     * Construct a {@code SerializingEntryCopierConfiguration}.
     */
    public SerializingEntryCopierConfiguration() {
        setName(NAME_DEFAULT);
    }

    /**
     * @param serializerId the serializer ID
     */
    public void setSerializerId(final String serializerId) {
        this.serializerId = serializerId;
    }

    /**
     * @return the serializer ID
     */
    public String getSerializerId() {
        return serializerId;
    }

    @Override
    public String toString() {
        return "SerializingEntryCopierConfiguration [serializerId=" + serializerId + ", getName()=" + getName()
                + ", isCopyContext()=" + isCopyContext() + "]";
    }

}
