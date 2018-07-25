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

import org.mozartspaces.capi3.Property.ForAllMatchmaker;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;

/**
 * The DefaultForAllMatchmaker evaluates to true if the all property values evaluate to true with the given matchmaker.
 *
 * @author Martin Planer
 */
public final class DefaultForAllMatchmaker implements NativeMatchmaker {

    private final NativeProperty property;
    private final NativeMatchmaker matchmaker;

    /**
     * Create a new DefaultAllLessThanOrEqualToMatchmaker.
     *
     * @param matchmaker
     *            to base on
     * @param filter
     */
    public DefaultForAllMatchmaker(final ForAllMatchmaker matchmaker, final NativeFilter filter) {
        DefaultQueryCoordinator coordinator = filter.getQuery().getSelector().getCoordinator();
        this.property = PropertyFactory.createProperty(matchmaker.getProperty(),
                coordinator.getPropertyValueCache());
        this.matchmaker = DefaultMatchmakerFilter.transposeMatchmaker(matchmaker.getMatchmaker(), filter);
    }

    @Override
    public boolean evaluate(final Serializable entry) {
        Object value = this.property.getValue(entry);

        if (value == null) {
            return matchmaker.evaluate(null);
        }

        if (value.getClass().equals(NoPathMatch.class)) {
            return false;
        }

        if (Collection.class.isAssignableFrom(value.getClass())) {
            for (Object item : (Collection<?>) value) {
                if (Serializable.class.isAssignableFrom(item.getClass())) {
                    if (!matchmaker.evaluate((Serializable) item)) {
                        return false;
                    }
                }
            }

            return true;
        }

        if (Serializable.class.isAssignableFrom(value.getClass())) {
            return matchmaker.evaluate((Serializable) value);
        }

        return false;
    }

    @Override
    public String toString() {
        return "ALL(" + property + "[" + matchmaker + "])";
    }

}
