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

import org.mozartspaces.core.RequestContext;

/**
 * Constant parameter value that is not affected by request context.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 *
 * @param <T>
 *            the type of the constant value
 */
public final class Constant<T> implements DynamicParameter<T> {

    private static final long serialVersionUID = 1L;

    private final T value;

    /**
     * Create a new constant value.
     *
     * @param value
     *            the constant value
     */
    public Constant(final T value) {
        this.value = value;
        if (this.value == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public T computeValue(final RequestContext context) {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Constant<?> other = (Constant<?>) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Constant [value=" + value + "]";
    }

}