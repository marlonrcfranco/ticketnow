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

import org.mozartspaces.capi3.Property.AllNotEqualMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The Bit Equal Matchmaker evaluates to true if the Property does not equals the value.
 *
 * @author Martin Planer
 */
public final class DefaultAllNotEqualMatchmaker extends AbstractDefaultValuePropertyMatchmaker {

    private final NativeProperty property;
    private final Object value;

    /**
     * Create a new NotEqualMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultAllNotEqualMatchmaker(final AllNotEqualMatchmaker matchmaker, final NativeFilter filter) {
        super(matchmaker, filter);
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.value = matchmaker.getValue();
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);
        Object currentValue = getCurrentValue(entry, this.value);

        if (propertyValue == null) {
            if (currentValue == null) {
                return false;
            }
            return true;
        }

        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return true;
        }

        if (Collection.class.isAssignableFrom(propertyValue.getClass())) {
            for (Object item : (Collection<?>) propertyValue) {
                if (item.equals(currentValue)) {
                    return false;
                }
            }
            return true;
        }
        return !propertyValue.equals(currentValue);
    }

    @Override
    public String toString() {
        return "ALL(" + property + ") != " + value;
    }

}
