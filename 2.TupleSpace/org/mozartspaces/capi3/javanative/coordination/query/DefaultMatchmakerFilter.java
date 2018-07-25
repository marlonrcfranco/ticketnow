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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.mozartspaces.capi3.ComparableProperty.AllGreaterThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.AllGreaterThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.AllLessThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.AllLessThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.BetweenMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.GreaterThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.GreaterThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.LessThanMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.LessThanOrEqualToMatchmaker;
import org.mozartspaces.capi3.ComparableProperty.RegexMatchmaker;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.Matchmaker;
import org.mozartspaces.capi3.Matchmakers.AndMatchmaker;
import org.mozartspaces.capi3.Matchmakers.NotMatchmaker;
import org.mozartspaces.capi3.Matchmakers.OrMatchmaker;
import org.mozartspaces.capi3.Property.AllEqualMatchmaker;
import org.mozartspaces.capi3.Property.AllNotEqualMatchmaker;
import org.mozartspaces.capi3.Property.ElementOfMatchmaker;
import org.mozartspaces.capi3.Property.EqualMatchmaker;
import org.mozartspaces.capi3.Property.ExistsMatchmaker;
import org.mozartspaces.capi3.Property.ForAllMatchmaker;
import org.mozartspaces.capi3.Property.NotEqualMatchmaker;
import org.mozartspaces.capi3.Query.MatchmakerFilter;
import org.mozartspaces.capi3.javanative.operation.NativeEntry;

/**
 * Filter which evaluates every entry against a matchmaker.
 *
 * @author Martin Barisits
 * @author Martin Planer
 */
public final class DefaultMatchmakerFilter extends AbstractNativeFilter {

    private final NativeMatchmaker predicate;

    /**
     * Creates a new DefaultMatchmakerfilter.
     *
     * @param matchmakerFilter
     *            to base on
     */
    public DefaultMatchmakerFilter(final MatchmakerFilter matchmakerFilter, final DefaultQuery query) {
        super(query);
        this.predicate = DefaultMatchmakerFilter.transposeMatchmaker(matchmakerFilter.getPredicate(), this);
    }

    @Override
    public Iterator<NativeEntry> select(final Iterator<NativeEntry> entries) throws CountNotMetException {
        return new Iterator<NativeEntry>() {

            private NativeEntry next = null;

            @Override
            public boolean hasNext() {
                while (entries.hasNext()) {
                    next = entries.next();
                    if (predicate.evaluate(next.getData())) {
                        return true;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public NativeEntry next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove not supported in this implementation!");
            }
        };
    }

    /**
     * Transpose a Matchmaker into a NativeMatchmaker.
     *
     * @param matchmaker
     *            to transpose
     * @param filter
     *            the filter this matchmaker belongs to
     * @return resulting NativeMatchmaker
     */
    public static NativeMatchmaker transposeMatchmaker(final Matchmaker matchmaker, final NativeFilter filter) {

        if (matchmaker.getClass().equals(EqualMatchmaker.class)) {
            return new DefaultEqualMatchmaker((EqualMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(NotEqualMatchmaker.class)) {
            return new DefaultNotEqualMatchmaker((NotEqualMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(ExistsMatchmaker.class)) {
            return new DefaultExistsMatchmaker((ExistsMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AndMatchmaker.class)) {
            return new DefaultAndMatchmaker((AndMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(NotMatchmaker.class)) {
            return new DefaultNotMatchmaker((NotMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(OrMatchmaker.class)) {
            return new DefaultOrMatchmaker((OrMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(GreaterThanMatchmaker.class)) {
            return new DefaultGreaterThanMatchmaker((GreaterThanMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(GreaterThanOrEqualToMatchmaker.class)) {
            return new DefaultGreaterThanOrEqualToMatchmaker((GreaterThanOrEqualToMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(LessThanMatchmaker.class)) {
            return new DefaultLessThanMatchmaker((LessThanMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(LessThanOrEqualToMatchmaker.class)) {
            return new DefaultLessThanOrEqualToMatchmaker((LessThanOrEqualToMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(BetweenMatchmaker.class)) {
            return new DefaultBetweenMatchmaker((BetweenMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(ElementOfMatchmaker.class)) {
            return new DefaultElementOfMatchmaker((ElementOfMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(RegexMatchmaker.class)) {
            return new DefaultRegexMatchmaker((RegexMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllEqualMatchmaker.class)) {
            return new DefaultAllEqualMatchmaker((AllEqualMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllNotEqualMatchmaker.class)) {
            return new DefaultAllNotEqualMatchmaker((AllNotEqualMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllGreaterThanMatchmaker.class)) {
            return new DefaultAllGreaterThanMatchmaker((AllGreaterThanMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllGreaterThanOrEqualToMatchmaker.class)) {
            return new DefaultAllGreaterThanOrEqualToMatchmaker((AllGreaterThanOrEqualToMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllLessThanMatchmaker.class)) {
            return new DefaultAllLessThanMatchmaker((AllLessThanMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(AllLessThanOrEqualToMatchmaker.class)) {
            return new DefaultAllLessThanOrEqualToMatchmaker((AllLessThanOrEqualToMatchmaker) matchmaker, filter);
        } else if (matchmaker.getClass().equals(ForAllMatchmaker.class)) {
            return new DefaultForAllMatchmaker((ForAllMatchmaker) matchmaker, filter);
        } else {
            throw new IllegalArgumentException("Unsupported matchmaker " + matchmaker);
        }
    }

    @Override
    public String toString() {
        return "MATCHMAKER (" + predicate + ")";
    }

}
