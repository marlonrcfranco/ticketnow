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

import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.RequestContext;

/**
 * Simple ContextAwareSelector that wraps a static selector and does not make context-based variable replacements.
 *
 * @author Stefan Crass
 *
 * @param <T>
 *            the type of the generated selector
 */
public final class SimpleContextAwareSelector<T extends Selector> implements ContextAwareSelector<T> {

    private static final long serialVersionUID = 1L;

    private final T selector;

    /**
     * @param selector
     *            the wrapped selector
     */
    public SimpleContextAwareSelector(final T selector) {
        this.selector = selector;
        if (this.selector == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public T computeSelector(final RequestContext context) {
        // no variables set, thus replacements do not apply
        return selector;
    }

    @Override
    public String toString() {
        return "SimpleContextAwareSelector [selector=" + selector + "]";
    }

}
