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
package org.mozartspaces.capi3.javanative.coordination.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.mozartspaces.capi3.Property.ElementOfMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The Equal Matchmaker evaluates to true if the Property equals the value.
 *
 * @author Martin Planer
 */
public final class DefaultElementOfMatchmaker implements NativeMatchmaker {

    private final NativeProperty property;
    private final List<Object> values;

    /**
     * Create a new ElementOfMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultElementOfMatchmaker(final ElementOfMatchmaker matchmaker, final NativeFilter filter) {
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.values = matchmaker.getValues();
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);

        if (values == null || values.isEmpty()) {
            return false;
        }

        if (propertyValue == null) {
            return values.contains(null);
        }

        // if (propertyValue == null) {
        // if (this.values == null) {
        // return true;
        // }
        //
        // return falsew;
        // }

        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return false;
        }

        if (Collection.class.isAssignableFrom(propertyValue.getClass())) {
            Collection<?> collection = (Collection<?>) propertyValue;
            return values.containsAll(collection);
        }

        return values.contains(propertyValue);
    }

    @Override
    public String toString() {
        return property + " IN (" + values.toString() + ")";
    }

}
