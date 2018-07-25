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

import org.mozartspaces.capi3.ComparableProperty.AllLessThanMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The DefaultAllLessThanMatchmaker evaluates to true if all Property values are less than the comparison value.
 *
 * @author Martin Planer
 */
public final class DefaultAllLessThanMatchmaker extends AbstractDefaultValuePropertyMatchmaker {

    private final NativeProperty property;
    private final Comparable<?> value;

    /**
     * Create a new DefaultAllLessThanMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultAllLessThanMatchmaker(final AllLessThanMatchmaker matchmaker, final NativeFilter filter) {
        super(matchmaker, filter);
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.value = matchmaker.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(final Serializable entry) {
        Object propertyValue = (this.property).getValue(entry);
        Object currentValue = getCurrentValue(entry, this.value);

        if (propertyValue == null) {
            return false;
        }

        if (propertyValue.getClass().equals(NoPathMatch.class)) {
            return false;
        }

        if ((Comparable.class.isAssignableFrom(propertyValue.getClass()))
                && (Comparable.class.isAssignableFrom(currentValue.getClass()))
                && propertyValue.getClass().equals(currentValue.getClass())) {
            int compare = (((Comparable<Object>) propertyValue).compareTo(currentValue));
            if (compare < 0) {
                return true;
            }

        }

        if (Collection.class.isAssignableFrom(propertyValue.getClass())
                && Comparable.class.isAssignableFrom(currentValue.getClass())) {

            for (Object item : (Collection<?>) propertyValue) {
                if (Comparable.class.isAssignableFrom(item.getClass())) {
                    int compare = (((Comparable<Object>) item).compareTo(currentValue));

                    if (compare >= 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        if (valueProperty == null) {
            return property + " > " + value;
        }

        return "ALL(" + property + ") < " + valueProperty;
    }

}
