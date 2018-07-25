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

import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;

/**
 * A <code>Request</code> to add an aspect to a container.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class AddAspectRequest extends AspectRequest<AspectReference> {

    private static final long serialVersionUID = 1L;

    private final ContainerAspect aspect;
    private final ContainerReference container;

    /**
     * Constructs an <code>AddAspectRequest</code>. Consider to use a
     * {@link #withAspect(ContainerAspect) Builder}.
     *
     * @param aspect
     *            the container aspect that should be added
     * @param container
     *            the reference of the container where the aspect should be
     *            added
     * @param iPoints
     *            the interception points where this aspect should be invoked
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param context
     *            the request context
     */
    public AddAspectRequest(final ContainerAspect aspect, final ContainerReference container,
            final Set<? extends InterceptionPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) {

        super(iPoints, transaction, isolation, context);

        this.aspect = aspect;
        if (this.aspect == null) {
            throw new NullPointerException("Aspect is null");
        }
        this.container = container;
        if (this.container == null && !(aspect instanceof SpaceAspect)) {
            throw new NullPointerException("Container is null for ContainerAspect");
        }

        // check ipoints
        Set<? extends InterceptionPoint> points = getIPoints();
        if (points == null) {
            throw new NullPointerException("Interception point set is null");
        }
        if (this.container == null) {
            for (InterceptionPoint point : getIPoints()) {
                if (!(point instanceof SpaceIPoint)) {
                    throw new IllegalArgumentException("ipoint " + point + " is not a SpaceIPoint");
                }
            }
        } else {
            for (InterceptionPoint point : getIPoints()) {
                if (!(point instanceof ContainerIPoint)) {
                    throw new IllegalArgumentException("ipoint " + point + " is not a ContainerIPoint");
                }
            }
        }
    }

    // for serialization
    private AddAspectRequest() {
        super(null, null, null, null);
        this.aspect = null;
        this.container = null;
    }

    /**
     * @return the aspect
     */
    public ContainerAspect getAspect() {
        return aspect;
    }

    /**
     * @return the container
     */
    public ContainerReference getContainer() {
        return container;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aspect == null) ? 0 : aspect.hashCode());
        result = prime * result + ((container == null) ? 0 : container.hashCode());
        // from superclasses
        result = prime * result + ((getIPoints() == null) ? 0 : getIPoints().hashCode());
        result = prime * result + ((getIsolation() == null) ? 0 : getIsolation().hashCode());
        result = prime * result + ((getTransaction() == null) ? 0 : getTransaction().hashCode());
        result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddAspectRequest)) {
            return false;
        }
        AddAspectRequest other = (AddAspectRequest) obj;
        if (aspect == null) {
            if (other.aspect != null) {
                return false;
            }
        } else if (!aspect.equals(other.aspect)) {
            return false;
        }
        if (container == null) {
            if (other.container != null) {
                return false;
            }
        } else if (!container.equals(other.container)) {
            return false;
        }
        // from superclasses
        if (getIPoints() == null) {
            if (other.getIPoints() != null) {
                return false;
            }
        } else if (!getIPoints().equals(other.getIPoints())) {
            return false;
        }
        if (getIsolation() == null) {
            if (other.getIsolation() != null) {
                return false;
            }
        } else if (!getIsolation().equals(other.getIsolation())) {
            return false;
        }
        if (getTransaction() == null) {
            if (other.getTransaction() != null) {
                return false;
            }
        } else if (!getTransaction().equals(other.getTransaction())) {
            return false;
        }
        if (getContext() == null) {
            if (other.getContext() != null) {
                return false;
            }
        } else if (!getContext().equals(other.getContext())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AddAspectRequest [aspect=" + aspect + ", container=" + container + ", iPoints=" + getIPoints()
                + ", isolation=" + getIsolation() + ", transaction=" + getTransaction() + ", context=" + getContext()
                + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder with a container aspect.
     *
     * @param aspect
     *            the container aspect that should be added. This parameter must
     *            not be <code>null</code>.
     * @return the builder with the container aspect set
     */
    public static Builder withAspect(final ContainerAspect aspect) {
        return new Builder(aspect);
    }

    /**
     * A class that helps to build an <code>AddAspectRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends
            AspectRequest.Builder<AddAspectRequest.Builder, AddAspectRequest> {

        private final ContainerAspect aspect;
        private ContainerReference container;

        /**
         * Protected constructor, use the static factory method
         * {@link AddAspectRequest#withAspect(ContainerAspect)}.
         */
        protected Builder(final ContainerAspect aspect) {
            this.aspect = aspect;
        }

        /**
         * Sets the container reference.
         *
         * @param container
         *            the container reference. This parameter must not be
         *            <code>null</code>.
         * @return the builder
         */
        public Builder container(final ContainerReference container) {
            this.container = container;
            return this;
        }

        @Override
        public AddAspectRequest build() {
            return new AddAspectRequest(aspect, container, getIPoints(), getTransaction(), getIsolation(),
                    getContext());
        }
    }

}
