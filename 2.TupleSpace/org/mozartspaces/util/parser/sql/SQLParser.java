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
package org.mozartspaces.util.parser.sql;

import java.io.StringReader;

import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.util.parser.sql.javacc.CCOqlParser;
import org.mozartspaces.util.parser.sql.javacc.ParseException;

/**
 * Parser that parses SQL-like query statements into Mozartspaces queries for the QueryCoordinator.
 *
 * @author Martin Planer
 */
public final class SQLParser {

    /**
     * Parses an SQL-like query statement into Mozartspaces queries.
     *
     * @param statement
     *            the statement
     * @return the MozartSpaces query
     * @throws ParseException
     *             if parsing the statement fails
     */
    public static Query parse(final String statement) throws ParseException {

        Query query = new Query();

        CCOqlParser parser = new CCOqlParser(new StringReader(statement));
        SQLParserResult result = parser.statement();

        Matchmaker matchmaker = result.getMatchmaker();
        if (matchmaker != null) {
            query = query.filter(matchmaker);
        }

        Integer limit = result.getLimit();
        if (limit != null) {
            query = query.cnt(0, limit);
        }

        return query;
    }

    private SQLParser() {
    }
}
