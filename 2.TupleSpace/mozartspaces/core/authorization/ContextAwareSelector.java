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

import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.RequestContext;

/**
 * Wrapper interface for selectors that allows dynamic computation of selector fields
 * based on context data.
 * Used for access control.
 *
 * @author Stefan Crass
 *
 * @param <T> type of the selector that the context aware selector produces
 */
public interface ContextAwareSelector<T extends Selector> extends Serializable {

    /**
     * Computes a new Selector based on variables passed via request context.
     * @param context the context of the request
     * @return the generated selector with fixed fields (null if generation failed)
     */
    T computeSelector(RequestContext context);

}
