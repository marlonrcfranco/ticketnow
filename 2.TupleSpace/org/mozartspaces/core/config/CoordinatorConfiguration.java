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
package org.mozartspaces.core.config;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Configuration of a custom coordinator.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class CoordinatorConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String apiClassName;
    private final String apiSelectorClassName;
    private final String capi3TranslatorClassName;
    private final String capi3SelectorTranslatorClassName;

    /**
     * @param apiClassName
     *            the name of the API coordinator class
     * @param apiSelectorClassName
     *            the name of the API selector class
     * @param capi3TranslatorClassName
     *            the name of the CAPI-3 coordinator translator class
     * @param capi3SelectorTranslatorClassName
     *            the name of the CAPI-3 selector translator class
     */
    public CoordinatorConfiguration(final String apiClassName, final String apiSelectorClassName,
            final String capi3TranslatorClassName, final String capi3SelectorTranslatorClassName) {
        this.apiClassName = apiClassName;
        if (this.apiClassName == null) {
            throw new NullPointerException("API class name");
        }
        this.apiSelectorClassName = apiSelectorClassName;
        if (this.apiSelectorClassName == null) {
            throw new NullPointerException("API selector class name");
        }
        this.capi3TranslatorClassName = capi3TranslatorClassName;
        if (this.capi3TranslatorClassName == null) {
            throw new NullPointerException("CAPI-3 translator class name");
        }
        this.capi3SelectorTranslatorClassName = capi3SelectorTranslatorClassName;
        if (this.capi3SelectorTranslatorClassName == null) {
            throw new NullPointerException("CAPI-3 selector translator class name");
        }
    }

    /**
     * @return the name of the API coordinator class
     */
    public String getApiClassName() {
        return apiClassName;
    }

    /**
     * @return the name of the API selector class
     */
    public String getApiSelectorClassName() {
        return apiSelectorClassName;
    }

    /**
     * @return the name of the CAPI-3 coordinator translator class
     */
    public String getCapi3TranslatorClassName() {
        return capi3TranslatorClassName;
    }

    /**
     * @return the name of the CAPI-3 selector translator class
     */
    public String getCapi3SelectorTranslatorClassName() {
        return capi3SelectorTranslatorClassName;
    }

    @Override
    public String toString() {
        return "CoordinatorConfiguration [apiClassName=" + apiClassName + ", apiSelectorClassName="
                + apiSelectorClassName + ", capi3TranslatorClassName=" + capi3TranslatorClassName
                + ", capi3SelectorTranslatorClassName=" + capi3SelectorTranslatorClassName + "]";
    }

}
