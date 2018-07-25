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
package org.mozartspaces.core.authorization;

import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.QueryCoordinator.QuerySelector;
import org.mozartspaces.core.RequestContext;

/**
 * Context aware version of QuerySelector.
 *
 * @author Stefan Crass
 */
public final class ContextAwareQuerySelector extends AbstractContextAwareSelector<QuerySelector> {

    private static final long serialVersionUID = 1L;

    private final DynamicParameter<Query> queryParam;

    /**
     *
     * @param query dynamic parameter for selection query
     * @param count dynamic parameter for selection count
     * @param name dynamic parameter for selector name
     */
    //TODO overloaded versions analogous to QuerySelector?
    //TODO overloaded version for dynamic evaluation of individual properties within query filters?
    public ContextAwareQuerySelector(final DynamicParameter<Query> query, final DynamicParameter<Integer> count,
            final DynamicParameter<String> name) {
        super(name, count);
        this.queryParam = query;
    }

    /**
     *
     * @return the dynamic query parameter
     */
    public DynamicParameter<Query> getQueryParam() {
        return this.queryParam;
    }

    @Override
    public QuerySelector computeSelector(final RequestContext context) {
        Integer count = this.getCountParam().computeValue(context);
        String name = this.getNameParam().computeValue(context);
        Query query = this.queryParam.computeValue(context);

        //TODO support for overloaded versions?
        if (query != null && count != null && name != null) {
            return QueryCoordinator.newSelector(query, count, name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ContextAwareQuerySelector [query=" + queryParam + ", count=" + getCountParam() + ", name="
                + getNameParam() + "]";
    }

}
