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
package org.mozartspaces.core;

import java.net.URI;
import java.util.List;

import org.mozartspaces.capi3.ContainerNameNotAvailableException;
import org.mozartspaces.capi3.ContainerNotFoundException;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class with helper functions for using the MozartSpaces Core API.
 *
 * @author Tobias Doenz
 */
public final class CapiUtil {

    private static final Logger log = LoggerFactory.get();

    /**
     * Looks up or creates an unbounded named container.
     *
     * @param containerName
     *            the name of the container, must not be <code>null</code>
     * @param space
     *            the space to use, use <code>null</code> for the embedded space
     * @param obligatoryCoords
     *            the obligatory coordinator list used for container creation, may be <code>null</code> or an empty
     *            list; the AnyCoordinator is used, if the <code>optionalCoords</code> are also <code>null</code>
     * @param transaction
     *            the transaction to use, use <code>null</code> for an implicit transaction
     * @param capi
     *            the interface to access the space (Core API)
     * @return the reference of the container
     * @throws MzsCoreException
     *             if getting or creating the container failed
     */
    public static ContainerReference lookupOrCreateContainer(final String containerName, final URI space,
            final List<? extends Coordinator> obligatoryCoords, final TransactionReference transaction, final Capi capi)
            throws MzsCoreException {

        ContainerReference cref;
        try {
            cref = capi.lookupContainer(containerName, space, RequestTimeout.DEFAULT, transaction);
            log.debug("Looked up container {}", cref);
        } catch (ContainerNotFoundException ex) {
            try {
                cref = capi.createContainer(containerName, space, Container.UNBOUNDED, obligatoryCoords, null,
                        transaction);
                log.debug("Created container {}", cref);
            } catch (ContainerNameNotAvailableException ex2) {
                log.debug("Concurrent container create, retrying lookup");
                cref = capi.lookupContainer(containerName, space, RequestTimeout.DEFAULT, transaction);
                log.debug("Looked up container {}", cref);
            }
        }
        return cref;
    }

    /**
     * Looks up or creates a container.
     *
     * @param core
     *            the core to use
     * @param space
     *            the space of the container
     * @param lookup
     *            the lookup container request
     * @param create
     *            the create container request
     * @return the container reference
     * @throws MzsCoreException
     *             if looking up or creating the container fails
     */
    public static ContainerReference lookupOrCreateContainer(final MzsCore core, final URI space,
            final LookupContainerRequest lookup, final CreateContainerRequest create) throws MzsCoreException {
        ContainerReference cref;
        try {
            try {
                cref = core.send(lookup, space).getResult();
                log.debug("Looked up container {}", cref);
            } catch (ContainerNotFoundException ex) {
                try {
                    cref = core.send(create, space).getResult();
                    log.debug("Created container {}", cref);
                } catch (ContainerNameNotAvailableException ex2) {
                    log.debug("Concurrent container create, retrying lookup");
                    cref = core.send(lookup, space).getResult();
                    log.debug("Looked up container {}", cref);
                }
            }
        } catch (InterruptedException ex) {
            throw new MzsCoreException("Getting request result interrupted", ex);
        }
        return cref;
    }

    /**
     * Gets the single entry in a list.
     *
     * @param <T>
     *            the entry type
     * @param list
     *            the list with one entry
     * @return the entry
     * @throws NullPointerException
     *             if the list is <code>null</code>
     * @throws IllegalArgumentException
     *             if the list is empty or contains more than one element
     */
    public static <T> T getSingleEntry(final List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        if (list.size() > 1) {
            throw new IllegalArgumentException("List contains more than one element");
        }
        return list.get(0);
    }

    /**
     * Gets the single entry in a list and returns it or throws it, if it is an exception.
     *
     * @param <T>
     *            the entry type
     * @param <E>
     *            the exception type that is thrown
     * @param list
     *            the list
     * @return the entry
     * @throws E
     *             if the entry is an <code>Exception</code>
     */
    @SuppressWarnings("unchecked")
    public static <T, E extends Exception> T getResultEntryOrThrowException(final List<?> list) throws E {
        Object entry = getSingleEntry(list);
        if (entry instanceof Exception) {
            throw (E) entry;
        }
        return (T) entry;
    }

    private CapiUtil() {
    }
}
