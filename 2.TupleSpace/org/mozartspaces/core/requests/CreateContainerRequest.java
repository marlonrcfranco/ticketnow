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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.authorization.AuthorizationLevel;

/**
 * A <code>Request</code> to create a container in a space.
 *
 * @author Tobias Doenz
 * @author Stefan Crass
 */
@ThreadSafe
public final class CreateContainerRequest extends TransactionalRequest<ContainerReference> {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final int size;
    private final List<Coordinator> obligatoryCoords;
    private final List<Coordinator> optionalCoords;
    private final AuthorizationLevel authLevel;
    private final boolean forceInMemory;

    /**
     * Constructs a <code>CreateContainerRequest</code>. Consider to use a {@link #withBuilder() Builder}.
     *
     * @param name
     *            the name for the container
     * @param size
     *            the maximal number of entries in the container
     * @param obligatoryCoords
     *            the obligatory coordinator list
     * @param optionalCoords
     *            the optional coordinator list
     * @param transaction
     *            the transaction reference
     * @param isolation
     *            the transaction isolation level for this request
     * @param forceInMemory
     *            specifies whether the container should be in-memory (regardless of the persistence configuration)
     * @param authLevel
     *            the authorization level for the created container
     * @param context
     *            the request context
     */
    public CreateContainerRequest(final String name, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final AuthorizationLevel authLevel,
            final boolean forceInMemory, final RequestContext context) {

        super(transaction, isolation, context);
        this.name = name;
        // TODO? container name checking
        this.size = size;
        this.authLevel = authLevel;
        if (this.size < 0) {
            throw new IllegalArgumentException("Negative size " + this.size);
        }
        if (this.authLevel == null) {
            throw new IllegalArgumentException("Authorization level must be specified");
        }
        if (obligatoryCoords != null) {
            this.obligatoryCoords = new ArrayList<Coordinator>(obligatoryCoords);
        } else {
            this.obligatoryCoords = Collections.emptyList();
        }
        if (optionalCoords != null) {
            this.optionalCoords = new ArrayList<Coordinator>(optionalCoords);
        } else {
            this.optionalCoords = Collections.emptyList();
        }
        if (this.obligatoryCoords.isEmpty() && this.optionalCoords.isEmpty()) {
            throw new IllegalArgumentException("No coordinator specified");
        }
        this.forceInMemory = forceInMemory;
    }

    // for serialization
    private CreateContainerRequest() {
        super(null, null, null);
        this.name = null;
        this.size = 0;
        this.obligatoryCoords = null;
        this.optionalCoords = null;
        this.authLevel = null;
        this.forceInMemory = false;
    }

    /**
     * Gets the container name.
     *
     * @return the container name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the maximal number of entries in the container.
     *
     * @return the maximal number of entries in the container
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the obligatory coordinators.
     *
     * @return an unmodifiable view of the obligatory coordinators
     */
    public List<Coordinator> getObligatoryCoords() {
        return Collections.unmodifiableList(obligatoryCoords);
    }

    /**
     * Gets the optional coordinators.
     *
     * @return an unmodifiable view of the optional coordinators
     */
    public List<Coordinator> getOptionalCoords() {
        return Collections.unmodifiableList(optionalCoords);
    }

    /**
     * Gets the authorization level.
     *
     * @return the authorization level
     */
    public AuthorizationLevel getAuthLevel() {
        return authLevel;
    }

    /**
     * Gets the force in-memory container flag.
     *
     * @return whether the container should be in-memory (regardless of the persistence configuration)
     */
    public boolean isForceInMemory() {
        return forceInMemory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((obligatoryCoords == null) ? 0 : obligatoryCoords.hashCode());
        result = prime * result + ((optionalCoords == null) ? 0 : optionalCoords.hashCode());
        result = prime * result + size;
        result = prime * result + ((authLevel == null) ? 0 : authLevel.hashCode());
        result = prime * result + (forceInMemory ? 1 : 0);
        // from superclasses
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
        if (!(obj instanceof CreateContainerRequest)) {
            return false;
        }
        CreateContainerRequest other = (CreateContainerRequest) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (obligatoryCoords == null) {
            if (other.obligatoryCoords != null) {
                return false;
            }
        } else if (!obligatoryCoords.equals(other.obligatoryCoords)) {
            return false;
        }
        if (optionalCoords == null) {
            if (other.optionalCoords != null) {
                return false;
            }
        } else if (!optionalCoords.equals(other.optionalCoords)) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        if (authLevel == null) {
            if (other.authLevel != null) {
                return false;
            }
        } else if (!authLevel.equals(other.authLevel)) {
            return false;
        }
        if (forceInMemory != other.forceInMemory) {
            return false;
        }
        // from superclasses
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
        return "CreateContainerRequest [name=" + name + ", obligatoryCoords=" + obligatoryCoords + ", optionalCoords="
                + optionalCoords + ", size=" + size + ", isolation=" + getIsolation() + ", transaction="
                + getTransaction() + ", authLevel=" + authLevel + ", forceInMemory=" + forceInMemory + ", context="
                + getContext() + "]";
    }

    // builder stuff below
    /**
     * Constructs a new builder.
     *
     * @return the builder
     */
    public static Builder withBuilder() {
        return new Builder();
    }

    /**
     * A class that helps to build a <code>CreateContainerRequest</code>.
     *
     * @author Tobias Doenz
     */
    public static final class Builder extends
            TransactionalRequest.Builder<CreateContainerRequest.Builder, CreateContainerRequest> {

        private String name = MzsConstants.Container.UNNAMED;
        private int size = MzsConstants.Container.DEFAULT_SIZE;
        private List<? extends Coordinator> obligatoryCoords;
        private List<? extends Coordinator> optionalCoords;
        private AuthorizationLevel authLevel = MzsConstants.DEFAULT_AUTHORIZATION;
        private boolean forceInMemory = MzsConstants.Container.DEFAULT_FORCE_IN_MEMORY;

        /**
         * Protected constructor, use the static factory method {@link CreateContainerRequest#withBuilder()}.
         */
        protected Builder() {
        }

        @Override
        public CreateContainerRequest build() {
            if ((obligatoryCoords == null || obligatoryCoords.isEmpty())
                    && (optionalCoords == null || optionalCoords.isEmpty())) {
                this.obligatoryCoords = Collections.singletonList(new AnyCoordinator());
            }
            return new CreateContainerRequest(name, size, obligatoryCoords, optionalCoords, getTransaction(),
                    getIsolation(), authLevel, forceInMemory, getContext());
        }

        /**
         * Sets the container name. The default value is <code>null</code> (unnamed container), if not explicitly set.
         *
         * @param name
         *            the name for the container
         * @return the builder
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the maximal number of entries in the container. The default value is
         * {@link org.mozartspaces.core.MzsConstants.Container#UNBOUNDED UNBOUNDED}, if not explicitly set. This
         * parameter must not be negative.
         *
         * @param size
         *            the maximal number of entries in the container
         * @return the builder
         */
        public Builder size(final int size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the authorization level of the container. The default value is
         * {@link org.mozartspaces.core.authorization.AuthorizationLevel#NONE}, if not explicitly set.
         *
         * @param authLevel
         *            the authorization level of the container
         * @return the builder
         */
        public Builder authLevel(final AuthorizationLevel authLevel) {
            this.authLevel = authLevel;
            return this;
        }

        /**
         * Sets the obligatory coordinators. Will be set to a single-element list with a {@link AnyCoordinator}, if the
         * obligatory AND optional coordinators are <code>null</code> or empty.
         *
         * @param obligatoryCoords
         *            the obligatory coordinator list
         * @return the builder
         */
        public Builder obligatoryCoords(final List<? extends Coordinator> obligatoryCoords) {
            this.obligatoryCoords = obligatoryCoords;
            return this;
        }

        /**
         * Sets the obligatory coordinators. Will be set to a single-element list with a {@link AnyCoordinator}, if the
         * obligatory AND optional coordinators are <code>null</code> or empty.
         *
         * @param obligatoryCoords
         *            the obligatory coordinator array or single coordinator
         * @return the builder
         */
        public Builder obligatoryCoords(final Coordinator... obligatoryCoords) {
            this.obligatoryCoords = Arrays.asList(obligatoryCoords);
            return this;
        }

        /**
         * Sets the optional coordinators. The default value is <code>null</code>, if not explicitly set.
         *
         * @param optionalCoords
         *            the optional coordinator list
         * @return the builder
         */
        public Builder optionalCoords(final List<? extends Coordinator> optionalCoords) {
            this.optionalCoords = optionalCoords;
            return this;
        }

        /**
         * Sets the optional coordinators. The default value is <code>null</code>, if not explicitly set.
         *
         * @param optionalCoords
         *            the optional coordinator array or single coordinator
         * @return the builder
         */
        public Builder optionalCoords(final Coordinator... optionalCoords) {
            this.optionalCoords = Arrays.asList(optionalCoords);
            return this;
        }

        /**
         * Sets the force-in-memory flag (override default persistence configuration and create an in-memory container).
         * The default value is {@link org.mozartspaces.core.MzsConstants.Container#DEFAULT_FORCE_IN_MEMORY
         * Container#DEFAULT_FORCE_IN_MEMORY}.
         *
         * @param forceInMemory
         *            the flag value
         * @return the builder
         */
        public Builder forceInMemory(final boolean forceInMemory) {
            this.forceInMemory = forceInMemory;
            return this;
        }
    }

}
