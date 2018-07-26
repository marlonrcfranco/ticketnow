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

import java.io.Serializable;
import java.util.Properties;

/**
 * Configuration of the persistence profile.
 *
 * @author Tobias Doenz
 */
// TODO @Immutable
public final class PersistenceConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default persistence profile.
     */
    public static final String PERSISTENCE_PROFILE_DEFAULT = "in-memory";
    private volatile String persistenceProfile = PERSISTENCE_PROFILE_DEFAULT;

    /**
     * Default configuration of implementation-specific properties (cache, location etc.).
     */
    public static final Properties PERSISTENCE_PROPERTIES_DEFAULT = new Properties();
    private volatile Properties persistenceProperties = PERSISTENCE_PROPERTIES_DEFAULT;

    /**
     * Default persistence serializer.
     */
    public static final String PERSISTENCE_SERIALIZER_DEFAULT = Configuration.SERIALIZER_DEFAULT;
    private volatile String persistenceSerializer = PERSISTENCE_SERIALIZER_DEFAULT;

    /**
     * Default persistence serializer cache size.
     */
    public static final int PERSISTENCE_SERIALIZER_CACHE_SIZE_DEFAULT = 0;
    private volatile int persistenceSerializerCacheSize = PERSISTENCE_SERIALIZER_CACHE_SIZE_DEFAULT;

    /**
     * Default configuration.
     */
    public PersistenceConfiguration() {
    }

    /**
     * Copy constructor.
     *
     * @param config
     *            the configuration to copy
     */
    public PersistenceConfiguration(final PersistenceConfiguration config) {
        this.persistenceProfile = config.persistenceProfile;
        this.persistenceSerializer = config.persistenceSerializer;
        this.persistenceSerializerCacheSize = config.persistenceSerializerCacheSize;
        this.persistenceProperties = config.persistenceProperties;
    }

    /**
     * @return the persistence profile ID
     */
    public String getPersistenceProfile() {
        return persistenceProfile;
    }

    /**
     * @param persistenceProfile
     *            the persistence profile ID
     */
    public void setPersistenceProfile(final String persistenceProfile) {
        this.persistenceProfile = persistenceProfile;
    }

    /**
     * @return the persistence properties
     */
    public Properties getPersistenceProperties() {
        return persistenceProperties;
    }

    /**
     * @param persistenceProperties
     *            the persistence properties
     */
    public void setPersistenceProperties(final Properties persistenceProperties) {
        this.persistenceProperties = persistenceProperties;
    }

    /**
     * @return the persistence serializer ID
     */
    public String getPersistenceSerializer() {
        return persistenceSerializer;
    }

    /**
     * @param persistenceSerializer
     *            the persistence serializer ID
     */
    public void setPersistenceSerializer(final String persistenceSerializer) {
        this.persistenceSerializer = persistenceSerializer;
    }

    /**
     * @return the persistence serializer cache size (in byte)
     */
    public int getPersistenceSerializerCacheSize() {
        return persistenceSerializerCacheSize;
    }

    /**
     * @param persistenceSerializerCacheSize
     *            the persistence serializer cache size (in byte)
     */
    public void setPersistenceSerializerCacheSize(final int persistenceSerializerCacheSize) {
        this.persistenceSerializerCacheSize = persistenceSerializerCacheSize;
    }

    @Override
    public String toString() {
        return "PersistenceConfiguration [persistenceProfile=" + persistenceProfile + ", persistenceProperties="
                + persistenceProperties + ", persistenceSerializer=" + persistenceSerializer
                + ", persistenceSerializerCacheSize=" + persistenceSerializerCacheSize + "]";
    }

}
