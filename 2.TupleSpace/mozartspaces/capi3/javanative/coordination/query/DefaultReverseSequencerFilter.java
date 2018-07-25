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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Sequencer which reverses the order.
 *
 * @author Martin Barisits
 */
public final class DefaultReverseSequencerFilter extends AbstractNativeFilter {

    /**
     * Creates a new DefaultReverseSequencerFilter.
     */
    public DefaultReverseSequencerFilter(final DefaultQuery query) {
        super(query);
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {

        List<NativeEntry> entryList = new ArrayList<NativeEntry>();
        while (entries.hasNext()) {
            entryList.add(entries.next());
        }

        Collections.reverse(entryList);

        return entryList.iterator();
    }

    @Override
    public String toString() {
        return "REVERSE";
    }

}
