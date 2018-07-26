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
package org.mozartspaces.capi3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to translate coordination API classes to the CAPI-3 implementation classes.
 *
 * @author Tobias Doenz
 */
public final class CoordinationTranslationFactory {

    private final Map<Class<? extends Coordinator>, CoordinatorTranslator<?, ?>> coordinatorTranslators;
    private final Map<Class<? extends Selector>, SelectorTranslator<?, ?>> selectorTranslators;

    /**
     * Constructs a {@code CoordinationTranslationFactory}.
     *
     * @param coordinatorTranslators
     *            the coordinator translators
     * @param selectorTranslators
     *            the selector translators
     */
    public CoordinationTranslationFactory(
            final Map<Class<? extends Coordinator>, CoordinatorTranslator<?, ?>> coordinatorTranslators,
            final Map<Class<? extends Selector>, SelectorTranslator<?, ?>> selectorTranslators) {

        this.coordinatorTranslators = new ConcurrentHashMap<Class<? extends Coordinator>, CoordinatorTranslator<?, ?>>(
                coordinatorTranslators);
        this.selectorTranslators = new ConcurrentHashMap<Class<? extends Selector>, SelectorTranslator<?, ?>>(
                selectorTranslators);
    }

    /**
     * Translates a coordinator from an instance of the API class to an instance of the internal implementation.
     *
     * @param coordinator
     *            the coordinator
     * @param <I>
     *            the input type
     * @param <O>
     *            the output type
     * @return the translated coordinator
     */
    public <I extends Coordinator, O extends Coordinator> O createCoordinator(final I coordinator) {
        assert coordinator != null;

        @SuppressWarnings("unchecked")
        CoordinatorTranslator<I, O> translator = (CoordinatorTranslator<I, O>) coordinatorTranslators.get(coordinator
                .getClass());
        if (translator == null) {
            throw new IllegalArgumentException("No translator for coordinator class "
                    + coordinator.getClass().getName());
        }
        return translator.translateCoordinator(coordinator);
    }

    /**
     * Translates a selector from an instance of the API class to an instance of the internal implementation.
     *
     * @param selector
     *            the selector
     * @param <I>
     *            the input type
     * @param <O>
     *            the output type
     * @return the translated selector
     */
    public <I extends Selector, O extends Selector> O createSelector(final I selector) {
        assert selector != null;

        @SuppressWarnings("unchecked")
        SelectorTranslator<I, O> translator = (SelectorTranslator<I, O>) selectorTranslators.get(selector.getClass());
        if (translator == null) {
            throw new IllegalArgumentException("No translator for selector class " + selector.getClass().getName());
        }
        return translator.translateSelector(selector);
    }

}
