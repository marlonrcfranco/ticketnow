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

import net.jcip.annotations.Immutable;

/**
 * The factories used for marshalling.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class MarshalFactories {

    private final ExceptionFactory exceptionFactory;
    private final CoordinatorFactory coordinatorFactory;
    private final CoordinationDataFactory coordinationDataFactory;
    private final SelectorFactory selectorFactory;

    /**
     * Constructs an instance of {@code MarshalFactories}.
     */
    public MarshalFactories() {
        exceptionFactory = new ExceptionFactory();
        FactoryConfigurations.addPredefinedExceptionCreators(exceptionFactory);
        coordinatorFactory = new CoordinatorFactory();
        FactoryConfigurations.addPredefinedCoordinatorCreators(coordinatorFactory);
        coordinationDataFactory = new CoordinationDataFactory();
        FactoryConfigurations.addPredefinedCoordinationDataCreators(coordinationDataFactory);
        selectorFactory = new SelectorFactory();
        FactoryConfigurations.addPredefinedSelectorCreators(selectorFactory);
    }

    /**
     * @return the exception factory
     */
    public ExceptionFactory getExceptionFactory() {
        return exceptionFactory;
    }

    /**
     * @return the coordinator factory
     */
    public CoordinatorFactory getCoordinatorFactory() {
        return coordinatorFactory;
    }

    /**
     * @return the coordination data factory
     */
    public CoordinationDataFactory getCoordinationDataFactory() {
        return coordinationDataFactory;
    }

    /**
     * @return the selector factory
     */
    public SelectorFactory getSelectorFactory() {
        return selectorFactory;
    }

}
