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
package org.mozartspaces.core;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Contains the information necessary to write an answer to a non-virtual answer
 * container.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class AnswerContainerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ContainerReference container;
    private final String coordinationKey;

    /**
     * Constructs an <code>AnswerContainerInfo</code>.
     *
     * @param container
     *            the reference of the answer container
     * @param coordinationKey
     *            the coordination key, used for writing the answer, may be
     *            <code>null</code>
     */
    public AnswerContainerInfo(final ContainerReference container, final String coordinationKey) {
        this.container = container;
        assert this.container != null;
        this.coordinationKey = coordinationKey;
    }

    // for serialization
    @SuppressWarnings("unused")
    private AnswerContainerInfo() {
        this.container = null;
        this.coordinationKey = null;
    }

    /**
     * @return the reference of the answer container
     */
    public ContainerReference getContainer() {
        return container;
    }

    /**
     * @return the coordination key
     */
    public String getCoordinationKey() {
        return coordinationKey;
    }

    @Override
    public String toString() {
        return "AnswerContainerInfo [container=" + container + ", coordinationKey=" + coordinationKey + "]";
    }

}
