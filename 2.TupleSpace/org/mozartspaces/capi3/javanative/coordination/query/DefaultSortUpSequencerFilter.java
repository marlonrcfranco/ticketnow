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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Query.SortUpSequencerFilter;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.query.NativeProperty.NoPathMatch;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Sequencer which orders the entries by a given property.
 *
 * @author Martin Barisits
 */
public final class DefaultSortUpSequencerFilter extends AbstractNativeFilter {

    private final Comparator<Object> sortUpComparator;

    /**
     * Create a new DefaultSortUpSequencerFilter.
     *
     * @param filter
     *            to base on
     */
    public DefaultSortUpSequencerFilter(final SortUpSequencerFilter filter, final DefaultQuery query) {
        super(query);
        DefaultQueryCoordinator coordinator = query.getSelector().getCoordinator();
        this.sortUpComparator = new SortUpComparator(PropertyFactory.createProperty(
                filter.getSortUpProperty(), coordinator.getPropertyValueCache()));
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {

        List<NativeEntry> entryList = new ArrayList<NativeEntry>();
        while (entries.hasNext()) {
            entryList.add(entries.next());
        }

        Collections.sort(entryList, this.sortUpComparator);

        return entryList.iterator();
    }

    @Override
    public String toString() {
        return "SORT-UP (" + sortUpComparator + ")";
    }

    /**
     * SortUpComparator. Note: this comparator imposes orderings that are inconsistent with equals
     *
     * @author Martin Barisits
     */
    private static class SortUpComparator implements Comparator<Object>, Serializable {

        private static final long serialVersionUID = 1L;

        private final NativeProperty sortupProperty;

        public SortUpComparator(final NativeProperty sortupProperty) {
            this.sortupProperty = sortupProperty;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public int compare(final Object arg0, final Object arg1) {
            Object obj0 = this.sortupProperty.getValue(((NativeEntry) arg0).getData());
            Object obj1 = this.sortupProperty.getValue(((NativeEntry) arg1).getData());
            if (obj0 == null || obj1 == null) {
                return 0;
            }

            if (obj0 == NoPathMatch.INSTANCE && obj1 != NoPathMatch.INSTANCE) {
                return 1;
            }
            if (obj0 != NoPathMatch.INSTANCE && obj1 == NoPathMatch.INSTANCE) {
                return -1;
            }
            if (obj0 == NoPathMatch.INSTANCE && obj1 == NoPathMatch.INSTANCE) {
                return 0;
            }

            if ((Comparable.class.isAssignableFrom(obj0.getClass()))
                    && (Comparable.class.isAssignableFrom(obj1.getClass()))) {
                return ((Comparable) obj0).compareTo(obj1);
            }
            return 0;
        }

        @Override
        public String toString() {
            return sortupProperty.toString();
        }

    }

}
