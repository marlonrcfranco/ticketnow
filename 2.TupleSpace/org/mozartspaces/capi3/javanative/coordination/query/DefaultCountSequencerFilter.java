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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.Query.CountSequencerFilter;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Sequencer which selects only a certain count of entries.
 *
 * @author Martin Barisits
 * @author Martin Planer
 */
public final class DefaultCountSequencerFilter extends AbstractNativeFilter {

    private static final int ALL = Query.ALL;

    private final int minCount;
    private final int maxCount;
    private final String selectorName;

    /**
     * Create a new DefaultCountSequencerFilter.
     *
     * @param filter
     *            to extract the count of
     */
    public DefaultCountSequencerFilter(final CountSequencerFilter filter, final DefaultQuery query) {
        super(query);
        this.minCount = (filter.getMinCount() < 0) ? ALL : filter.getMinCount();
        this.maxCount = (filter.getMaxCount() < 0) ? ALL : filter.getMaxCount();
        this.selectorName = getQuery().getSelector().getName();
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {

        List<NativeEntry> selectedEntries = new ArrayList<NativeEntry>();

        // Error checking
        if (maxCount >= 0 && maxCount < minCount) {
            throw new CountNotMetException(selectorName, minCount, selectedEntries.size());
        }

        boolean hasNext = entries.hasNext();

        // Try to get minimum count
        while (hasNext) {
            if (selectedEntries.size() < minCount || minCount == ALL) {
                NativeEntry entry = entries.next();
                if (getQuery().isAccessible(entry)) {
                    selectedEntries.add(entry);
                } else if (minCount == ALL) {
                    throw new CountNotMetException(selectorName, minCount, selectedEntries.size());
                }
            } else {
                break;
            }
            hasNext = entries.hasNext();
        }

        // Check minimum count
        if (selectedEntries.size() < minCount) {
            throw new CountNotMetException(selectorName, minCount, selectedEntries.size());
        }

        // Try to get up to maximum
        while (hasNext) {
            if (selectedEntries.size() < maxCount || maxCount == ALL) {
                NativeEntry entry = entries.next();
                if (getQuery().isAccessible(entry)) {
                    selectedEntries.add(entry);
                }
            } else {
                break;
            }
            hasNext = entries.hasNext();
        }

        return selectedEntries.iterator();
    }

    @Override
    public String toString() {
        return "COUNT (minCount=" + minCount + ", maxCount=" + maxCount + ")";
    }

}
