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


/**
 * Represents a generic value object to access a space. It is sent with the Core
 * API to the embedded or a remote core and contains the parameters for the
 * desired operation on the space. There is an implementation for each operation
 * in the space, e.g., to read entries, create a container or transaction, or to
 * add an aspect.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type returned by the <code>RequestFuture</code> that is
 *            returned by {@link MzsCore} when the request is sent.
 */
public interface Request<R extends Serializable> extends Serializable {

    /**
     *
     * @author Tobias Doenz
     *
     * @param <T>
     */
    interface Builder<T> {

        /**
         * Builds the request with the parameters set in the builder methods.
         *
         * @return the built request
         */
        T build();
    }
}
