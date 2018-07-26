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
package org.mozartspaces.core.requests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

/**
 * An abstract <code>Request</code> for requests that select entries from a
 * container.
 *
 * @author Tobias Doenz
 *
 * @param <R>
 *            the result type of this request
 */
@ThreadSafe
public abstract class SelectingEntriesRequest<R extends Serializable> extends EntriesRequest<R> {

    private static final long serialVersionUID = 1L;

    protected static final List<DummySelector> DUMMY_SELECTORS = Collections.singletonList(new DummySelector());
    /**
     * Dummy selector class for list above.
     */
    private static class DummySelector implements Selector {
        private static final long serialVersionUID = 1L;
        @Override
        public String getName() {
            return null;
        }
        @Override
        public int getCount() {
            return 0;
        }
    }

    private final List<Selector> selectors;

    protected SelectingEntriesRequest(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context) {

        super(container, timeoutInMilliseconds, transaction, isolation, context);

        if (selectors == null) {
            throw new NullPointerException("Selector list is null");
        }
        this.selectors = new ArrayList<Selector>(selectors);
        if (this.selectors.isEmpty()) {
            throw new IllegalArgumentException("Selector list is empty");
        }
    }


    /**
     * @return an unmodifiable view of the entry selector list
     */
    public final List<Selector> getSelectors() {
        return Collections.unmodifiableList(selectors);
    }

    /**
     * A class that helps to build an <code>SelectingEntriesRequest</code>.
     *
     * @author Tobias Doenz
     *
     * @param <B>
     *            the type of the builder
     * @param <T>
     *            the type of the request this builder constructs
     */
    public abstract static class Builder<B, T> extends EntriesRequest.Builder<B, T> {

        private List<? extends Selector> selectors = Collections.singletonList(AnyCoordinator.newSelector());

        /**
         * Constructs a <code>Builder</code>.
         *
         * @param container
         *            the reference of the container that should be used
         */
        protected Builder(final ContainerReference container) {
            super(container);
        }

        /**
         * Sets the entry selectors. The default value is the
         * {@link org.mozartspaces.capi3.AnyCoordinator.AnySelector
         * AnySelector}, if not explicitly set. This parameter must not be
         * null or an empty list.
         *
         * @param selectors
         *            the entry selector list
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B selectors(final List<? extends Selector> selectors) {
            this.selectors = selectors;
            return (B) this;
        }

        /**
         * Sets the entry selectors. The default value is the
         * {@link org.mozartspaces.capi3.AnyCoordinator.AnySelector
         * AnySelector}, if not explicitly set. This parameter must not be
         * null or an empty array.
         *
         * @param selectors
         *            the entry selector array or single selector
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        public final B selectors(final Selector... selectors) {
            this.selectors = Arrays.asList(selectors);
            return (B) this;
        }

        // methods used in sub-class when the request is constructed
        protected final List<? extends Selector> getSelectors() {
            return selectors;
        }

    }
}
