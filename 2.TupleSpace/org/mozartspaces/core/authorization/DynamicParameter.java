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
package org.mozartspaces.core.authorization;

import java.io.Serializable;

import org.mozartspaces.core.RequestContext;

/**
 * Interface for dynamic parameters that are used in dynamic selectors.
 *
 * @author Stefan Crass
 *
 * @param <T> the type of the dynamic parameter
 */
public interface DynamicParameter<T> extends Serializable {

    /**
     * Gets the current value.
     *
     * Does not dynamically compute the value.
     * @return the current parameter value
     */
    T getValue();

    /**
     * Computes the current value of the parameter based on the given request context.
     *
     * If the context does not provide enough information to compute the value, a default
     * value (if available) or null are returned.
     * Stores the value so that subsequent calls to <code>getValue</code> return it.
     *
     * @param context the request context used for resolving the parameter
     * @return the computed value
     */
    T computeValue(final RequestContext context);

}