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

import java.util.HashMap;
import java.util.Iterator;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Query.DistinctSequencerFilter;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * @author Martin Planer
 */
public final class DefaultDistinctSequencerFilter extends AbstractNativeFilter {

    private final NativeProperty distinctProperty;

    public DefaultDistinctSequencerFilter(final DistinctSequencerFilter filter, final DefaultQuery query) {
        super(query);
        DefaultQueryCoordinator coordinator = query.getSelector().getCoordinator();
        distinctProperty = PropertyFactory.createProperty(filter.getDistinctProperty(),
                coordinator.getPropertyValueCache());
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {

        HashMap<Object, NativeEntry> distinctObjects = new HashMap<Object, NativeEntry>();

        while (entries.hasNext()) {
            NativeEntry entry = entries.next();
            Object distinctValue = distinctProperty.getValue(entry.getData());
            distinctObjects.put(distinctValue, entry);
        }

        return distinctObjects.values().iterator();
    }

    @Override
    public String toString() {
        return "DISTINCT (" + distinctProperty + ")";
    }

}
