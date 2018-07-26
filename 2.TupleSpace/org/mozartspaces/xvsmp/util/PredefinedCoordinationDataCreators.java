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

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnyData;
import org.mozartspaces.capi3.FifoCoordinator.FifoData;
import org.mozartspaces.capi3.KeyCoordinator.KeyData;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LifoCoordinator.LifoData;
import org.mozartspaces.capi3.LindaCoordinator.LindaData;
import org.mozartspaces.capi3.QueryCoordinator.QueryData;
import org.mozartspaces.capi3.RandomCoordinator.RandomData;
import org.mozartspaces.capi3.VectorCoordinator.VectorData;

/**
 * Contains the creators for the predefined coordination data types. They all
 * have a name, some also an additional parameter.
 *
 * @author Tobias Doenz
 */
public final class PredefinedCoordinationDataCreators {

    /**
     * Creates instances of {@code AnyData}.
     *
     * @author Tobias Doenz
     */
    public static final class AnyCoordinationDataCreator implements CoordinationDataCreator<AnyData> {
        @Override
        public AnyData newCoordinationData(final String name, final Object... params) {
            String coordinatorName = (name == null) ? AnyCoordinator.DEFAULT_NAME : name;
            return AnyCoordinator.newCoordinationData(coordinatorName);
        }
    }

    /**
     * Creates instances of {@code FifoData}.
     *
     * @author Tobias Doenz
     */
    public static final class FifoCoordinationDataCreator implements CoordinationDataCreator<FifoData> {
        @Override
        public FifoData newCoordinationData(final String name, final Object... params) {
            String coordinatorName = (name == null) ? FifoCoordinator.DEFAULT_NAME : name;
            return FifoCoordinator.newCoordinationData(coordinatorName);
        }
    }

    /**
     * Creates instances of {@code KeyData}.
     *
     * @author Tobias Doenz
     */
    public static final class KeyCoordinationDataCreator implements CoordinationDataCreator<KeyData> {
        @Override
        public KeyData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? KeyCoordinator.DEFAULT_NAME : name;
            String key = (String) params[0];
            return KeyCoordinator.newCoordinationData(key, selectorName);
        }
    }

    /**
     * Creates instances of {@code LabelData}.
     *
     * @author Tobias Doenz
     */
    public static final class LabelCoordinationDataCreator implements CoordinationDataCreator<LabelData> {
        @Override
        public LabelData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? LabelCoordinator.DEFAULT_NAME : name;
            String label = (String) params[0];
            return LabelCoordinator.newCoordinationData(label, selectorName);
        }
    }

    /**
     * Creates instances of {@code LifoData}.
     *
     * @author Tobias Doenz
     */
    public static final class LifoCoordinationDataCreator implements CoordinationDataCreator<LifoData> {
        @Override
        public LifoData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? LifoCoordinator.DEFAULT_NAME : name;
            return LifoCoordinator.newCoordinationData(selectorName);
        }
    }

    /**
     * Creates instances of {@code LindaData}.
     *
     * @author Tobias Doenz
     */
    public static final class LindaCoordinationDataCreator implements CoordinationDataCreator<LindaData> {
        @Override
        public LindaData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? LindaCoordinator.DEFAULT_NAME : name;
            return LindaCoordinator.newCoordinationData(selectorName);
        }
    }

    /**
     * Creates instances of {@code QueryData}.
     *
     * @author Tobias Doenz
     */
    public static final class QueryCoordinationDataCreator implements CoordinationDataCreator<QueryData> {
        @Override
        public QueryData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? QueryCoordinator.DEFAULT_NAME : name;
            return QueryCoordinator.newCoordinationData(selectorName);
        }
    }

    /**
     * Creates instances of {@code RandomData}.
     *
     * @author Tobias Doenz
     */
    public static final class RandomCoordinationDataCreator implements CoordinationDataCreator<RandomData> {
        @Override
        public RandomData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? RandomCoordinator.DEFAULT_NAME : name;
            return RandomCoordinator.newCoordinationData(selectorName);
        }
    }

    /**
     * Creates instances of {@code VectorData}.
     *
     * @author Tobias Doenz
     */
    public static final class VectorCoordinationDataCreator implements CoordinationDataCreator<VectorData> {
        @Override
        public VectorData newCoordinationData(final String name, final Object... params) {
            String selectorName = (name == null) ? VectorCoordinator.DEFAULT_NAME : name;
            int index = (Integer) params[0];
            return VectorCoordinator.newCoordinationData(index, selectorName);
        }
    }

    private PredefinedCoordinationDataCreators() {
    }
}
