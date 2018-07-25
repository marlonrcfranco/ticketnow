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
package org.mozartspaces.util;

/**
 * Interface with cloning method. This is an alternative to cloning with
 * {@link Object#clone()} an the interface {@link Cloneable}, which is flawed as
 * Joshua Bloch explains in "Effective Java" (Item 11, 2nd edition).
 *
 * @author Tobias Doenz
 */
public interface MzsCloneable {

    /**
     * @return a clone of this object, or the object itself, if it is mutable
     *
     * @throws CloneNotSupportedException
     *             if the object cannot be cloned
     */
    Object clone() throws CloneNotSupportedException;
}
