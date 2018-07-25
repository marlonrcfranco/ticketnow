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
package org.mozartspaces.capi3;

/**
 * To classify Operations.
 *
 * @author Martin Barisits
 */
public enum OperationType {

    /**
     * Writing an Entry to a Container.
     */
    WRITE,

    /**
     * Reading an Entry from a Container.
     */
    READ,

    /**
     * Taking an Entry from a Container.
     */
    TAKE,

    /**
     * Creating a Container.
     */
    CREATECONTAINER,

    /**
     * Destroy a Container.
     */
    DESTROYCONTAINER,

    /**
     * Lookup a Container.
     */
    LOOKUPCONTAINER,

    /**
     * Setting an exclusive Lock on a Container.
     */
    LOCKCONTAINER,

    /**
     * Register an Aspect to a Container.
     */
    REGISTERASPECT,

    /**
     * Unregister an Aspect on a Container.
     */
    UNREGISTERASPECT
}
