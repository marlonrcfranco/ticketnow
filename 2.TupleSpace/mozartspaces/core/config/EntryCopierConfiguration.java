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

/**
 * Configuration of an entry copier.
 *
 * @author Tobias Doenz
 */
public class EntryCopierConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Dummy name for no entry copier.
     */
    public static final String NAME_NONE = "none";

    /**
     * Name of the entry copier that uses serialization.
     */
    public static final String NAME_SERIALIZING = "serializing";

    /**
     * Name of the entry copier that uses {@code clone()}.
     */
    public static final String NAME_CLONING = "cloning";

    /**
     * Default entry copier name.
     */
    public static final String NAME_DEFAULT = NAME_NONE;
    private volatile String name = NAME_DEFAULT;

    /**
     * Default value of the copy context flag.
     */
    public static final boolean COPY_CONTEXT = false;
    private volatile boolean copyContext = COPY_CONTEXT;

    /**
     * Default constructor.
     */
    public EntryCopierConfiguration() {
    }

    /**
     * Copy constructor.
     *
     * @param config
     *            the configuration to copy
     */
    public EntryCopierConfiguration(final EntryCopierConfiguration config) {
        this.copyContext = config.copyContext;
        this.name = config.name;
    }

    /**
     * @param name the entry copier name
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the entry copier name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param copyContext
     *            the flag to determine whether the request context should be
     *            copied
     */
    public final void setCopyContext(final boolean copyContext) {
        this.copyContext = copyContext;
    }

    /**
     * @return {@code true} if the context should be copied
     */
    public final boolean isCopyContext() {
        return copyContext;
    }

    @Override
    public String toString() {
        return "EntryCopierConfiguration [name=" + name + ", copyContext=" + copyContext + "]";
    }

}
