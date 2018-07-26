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
package org.mozartspaces.capi3.javanative.persistence.berkeley;

import java.io.File;

/**
 * This interface defines constants used by the configuration of the Berkeley DB.
 *
 * @author Jan Zarnikov
 */
public final class BerkeleyConfig {

    /**
     * The key for the preference (in mozartspaces.xml) that defines the path where the data will be stored.
     */
    public static final String LOCATION = "berkeley-location";

    /**
     * The default value of the preference {@link BerkeleyConfig#LOCATION}. The default location where the data are
     * stored is a directory called "xvsm" in the temp.-directory of the OS.
     */
    public static final String LOCATION_DEFAULT = System.getProperty("java.io.tmpdir") + File.separator + "xvsm";

    /**
     * The key for the preference (in mozartspaces.xml) that defines the size of internal cache used by Berkeley DB
     * in bytes.
     */
    public static final String CACHE_SIZE = "berkeley-cache-size";

    /**
     * The default value of the preference {@link BerkeleyConfig#CACHE_SIZE}. The default size is 10.000.000 (10MB).
     */
    public static final long CACHE_SIZE_DEFAULT = 10000000;

    private BerkeleyConfig() {
    }
}
