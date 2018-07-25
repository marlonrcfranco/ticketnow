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
 * Variable parameter that has a defined name.
 *
 * The value is computed by searching for the value of an item in the request context with this name. If no item with
 * corresponding type is found, a given default value is used.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 *
 * @param <T>
 *            the variable type
 */
public final class Variable<T> implements DynamicParameter<T> {

    private static final long serialVersionUID = 1L;
    private final String name;
    private T value;
    private final Class<T> type;

    /**
     *
     * @param name
     *            the variable name
     * @param type
     *            the type of the variable
     * @param defaultValue
     *            the default value of the variable (may be null if no default should be used)
     */
    public Variable(final String name, final Class<T> type, final T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.type = type;
    }

    /**
     *
     * @return the variable name
     */
    public String getName() {
        return name;
    }

    @Override
    public T getValue() {
        return value;
    }

    /**
     *
     * @return the variable type
     */
    public Class<T> getType() {
        return type;
    }

    @Override
    public T computeValue(final RequestContext context) {
        if (context.containsProperty(this.name)) {
            Object varValue = context.getProperty(this.name);
            if (varValue == null || type.isInstance(varValue)) {
                this.value = type.cast(varValue);
                return this.getValue();
            } else {
                // TODO wrong type, use default value
                return this.getValue();
            }
        } else {
            // TODO missing variable value, use default value
            return this.getValue();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Variable<?> other = (Variable<?>) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
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
        return "Variable [name=" + name + ", value=" + value + ", type=" + type + "]";
    }


}
