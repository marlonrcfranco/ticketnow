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
package org.mozartspaces.xvsmp.util;


/**
 * A {@code ExceptionCreator} creates exceptions, instances of a specific exception
 * type.
 *
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the exception this creator creates
 */
public interface ExceptionCreator<T extends Exception> {

    /**
     * Creates a new exception.
     *
     * @param message
     *            the exception message
     * @return the created exception
     */
    T newException(final String message);

}