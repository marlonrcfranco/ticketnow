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

/**
 * Contains the creators for the predefined coordinators. They all have only a
 * name, additional parameters are ignored.
 *
 * @author Tobias Doenz
 */
public final class PredefinedCoordinatorCreators {

    /**
     * Creates instances of {@code AnyCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class AnyCoordinatorCreator implements CoordinatorCreator<AnyCoordinator> {
        @Override
        public AnyCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new AnyCoordinator();
            }
            return new AnyCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code FifoCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class FifoCoordinatorCreator implements CoordinatorCreator<FifoCoordinator> {
        @Override
        public FifoCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new FifoCoordinator();
            }
            return new FifoCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code KeyCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class KeyCoordinatorCreator implements CoordinatorCreator<KeyCoordinator> {
        @Override
        public KeyCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new KeyCoordinator();
            }
            return new KeyCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code LabelCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class LabelCoordinatorCreator implements CoordinatorCreator<LabelCoordinator> {
        @Override
        public LabelCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new LabelCoordinator();
            }
            return new LabelCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code LifoCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class LifoCoordinatorCreator implements CoordinatorCreator<LifoCoordinator> {
        @Override
        public LifoCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new LifoCoordinator();
            }
            return new LifoCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code LindaCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class LindaCoordinatorCreator implements CoordinatorCreator<LindaCoordinator> {
        @Override
        public LindaCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new LindaCoordinator();
            }
            boolean onlyAnnotatedEntries = (Boolean) params[0];
            return new LindaCoordinator(name, onlyAnnotatedEntries);
        }
    }

    /**
     * Creates instances of {@code QueryCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class QueryCoordinatorCreator implements CoordinatorCreator<QueryCoordinator> {
        @Override
        public QueryCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new QueryCoordinator();
            }
            return new QueryCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code RandomCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class RandomCoordinatorCreator implements CoordinatorCreator<RandomCoordinator> {
        @Override
        public RandomCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new RandomCoordinator();
            }
            return new RandomCoordinator(name);
        }
    }

    /**
     * Creates instances of {@code VectorCoordinator}.
     *
     * @author Tobias Doenz
     */
    public static final class VectorCoordinatorCreator implements CoordinatorCreator<VectorCoordinator> {
        @Override
        public VectorCoordinator newCoordinator(final String name, final Object... params) {
            if (name == null) {
                return new VectorCoordinator();
            }
            return new VectorCoordinator(name);
        }
    }

    private PredefinedCoordinatorCreators() {
    }
}
