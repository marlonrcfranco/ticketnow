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
package org.mozartspaces.xvsmp.util;

import java.io.Serializable;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.LifoCoordinator.LifoSelector;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.QueryCoordinator.QuerySelector;
import org.mozartspaces.capi3.RandomCoordinator.RandomSelector;
import org.mozartspaces.capi3.VectorCoordinator.VectorSelector;

/**
 * Contains the creators for the predefined selectors. They all have a name and
 * an entry count, some also an additional parameter.
 *
 * @author Tobias Doenz
 */
public final class PredefinedSelectorCreators {

    private static int getCount(final Integer count) {
        if (count == null) {
            return 1;
        }
        return count;
    }

    /**
     * Creates instances of {@code AnySelector}.
     *
     * @author Tobias Doenz
     */
    public static final class AnySelectorCreator implements SelectorCreator<AnySelector> {
        @Override
        public AnySelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? AnyCoordinator.DEFAULT_NAME : name;
            return AnyCoordinator.newSelector(getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code FifoSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class FifoSelectorCreator implements SelectorCreator<FifoSelector> {
        @Override
        public FifoSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? FifoCoordinator.DEFAULT_NAME : name;
            return FifoCoordinator.newSelector(getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code KeySelector}.
     *
     * @author Tobias Doenz
     */
    public static final class KeySelectorCreator implements SelectorCreator<KeySelector> {
        @Override
        public KeySelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? KeyCoordinator.DEFAULT_NAME : name;
            String key = (String) params[0];
            return KeyCoordinator.newSelector(key, getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code LabelSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class LabelSelectorCreator implements SelectorCreator<LabelSelector> {
        @Override
        public LabelSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? LabelCoordinator.DEFAULT_NAME : name;
            String label = (String) params[0];
            return LabelCoordinator.newSelector(label, getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code LifoSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class LifoSelectorCreator implements SelectorCreator<LifoSelector> {
        @Override
        public LifoSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? LifoCoordinator.DEFAULT_NAME : name;
            return LifoCoordinator.newSelector(getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code LindaSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class LindaSelectorCreator implements SelectorCreator<LindaSelector> {
        @Override
        public LindaSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? LindaCoordinator.DEFAULT_NAME : name;
            Serializable template = (Serializable) params[0];
            return LindaCoordinator.newSelector(template, getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code QuerySelector}.
     *
     * @author Tobias Doenz
     */
    public static final class QuerySelectorCreator implements SelectorCreator<QuerySelector> {
        @Override
        public QuerySelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? QueryCoordinator.DEFAULT_NAME : name;
            Query query = (Query) params[0];
            return QueryCoordinator.newSelector(query, getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code RandomSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class RandomSelectorCreator implements SelectorCreator<RandomSelector> {
        @Override
        public RandomSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? RandomCoordinator.DEFAULT_NAME : name;
            return RandomCoordinator.newSelector(getCount(count), coordinatorName);
        }
    }

    /**
     * Creates instances of {@code VectorSelector}.
     *
     * @author Tobias Doenz
     */
    public static final class VectorSelectorCreator implements SelectorCreator<VectorSelector> {
        @Override
        public VectorSelector newSelector(final String name, final Integer count, final Object... params) {
            String coordinatorName = (name == null) ? VectorCoordinator.DEFAULT_NAME : name;
            int index = (Integer) params[0];
            return VectorCoordinator.newSelector(index, getCount(count), coordinatorName);
        }
    }

    private PredefinedSelectorCreators() {
    }
}
