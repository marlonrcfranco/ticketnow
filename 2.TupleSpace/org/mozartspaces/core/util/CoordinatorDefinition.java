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
package org.mozartspaces.core.util;

import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CoordinatorTranslator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.SelectorTranslator;

/**
 * Definition of a (custom) coordinator.
 *
 * @author Tobias Doenz
 */
public final class CoordinatorDefinition {

    private final Class<? extends Coordinator> apiClass;
    private final Class<? extends Selector> apiSelectorClass;
    private final Class<? extends CoordinatorTranslator<?, ?>> capi3TranslatorClass;
    private final Class<? extends SelectorTranslator<?, ?>> capi3SelectorTranslatorClass;

    /**
     * @param apiClass
     *            the coordinator API class
     * @param apiSelectorClass
     *            the selector API class
     * @param capi3TranslatorClass
     *            the coordinator CAPI-3 translator
     * @param capi3SelectorTranslatorClass
     *            the selector CAPI-3 translator
     */
    public CoordinatorDefinition(final Class<? extends Coordinator> apiClass,
            final Class<? extends Selector> apiSelectorClass,
            final Class<? extends CoordinatorTranslator<?, ?>> capi3TranslatorClass,
            final Class<? extends SelectorTranslator<?, ?>> capi3SelectorTranslatorClass) {
        this.apiClass = apiClass;
        this.apiSelectorClass = apiSelectorClass;
        this.capi3TranslatorClass = capi3TranslatorClass;
        this.capi3SelectorTranslatorClass = capi3SelectorTranslatorClass;
    }

    /**
     * @return the apiClass
     */
    public Class<? extends Coordinator> getApiClass() {
        return apiClass;
    }

    /**
     * @return the apiSelectorClass
     */
    public Class<? extends Selector> getApiSelectorClass() {
        return apiSelectorClass;
    }

    /**
     * @return the capi3TranslatorClass
     */
    public Class<? extends CoordinatorTranslator<?, ?>> getCapi3TranslatorClass() {
        return capi3TranslatorClass;
    }

    /**
     * @return the capi3SelectorTranslatorClass
     */
    public Class<? extends SelectorTranslator<?, ?>> getCapi3SelectorTranslatorClass() {
        return capi3SelectorTranslatorClass;
    }

}
