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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.ClearSpaceRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.Nothing;

/**
 * Synchronous wrapper interface around the core requests. The methods of this interface create request objects, send
 * them to the {@link MzsCore} instance, and wait for the result.
 * <p>
 * For overloaded methods: if a request parameter is not in the method signature, then internally the default values are
 * used. These default values are defined in {@link MzsConstants}, and for the <code>RequestContext</code> it is
 * <code>null</code>.
 *
 * @author Tobias Doenz
 *
 * @see org.mozartspaces.core.Request
 */
@Immutable
public final class Capi {

    private final MzsCore core;

    /**
     * Constructs a <code>Capi</code> instance.
     *
     * @param core
     *            the core to which the requests are sent
     */
    public Capi(final MzsCore core) {
        this.core = core;
        assert this.core != null;
    }

    /**
     * @return the core to which the requests are sent
     */
    public MzsCore getCore() {
        return core;
    }

    // container methods
    /**
     * Creates an unbounded, unnamed container with the AnyCoordinator in the embedded space.
     *
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     */
    public ContainerReference createContainer() throws MzsCoreException {
        return createContainer(Container.UNNAMED, null, Container.DEFAULT_SIZE, null, null, null,
                MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Creates an unbounded, unnamed container in a space.
     *
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param obligatoryCoords
     *            the obligatory coordinator list, for <code>null</code> or an empty list the AnyCoordinator is used
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     */
    public ContainerReference createContainer(final URI space, final List<? extends Coordinator> obligatoryCoords,
            final TransactionReference transaction) throws MzsCoreException {
        return createContainer(Container.UNNAMED, space, Container.DEFAULT_SIZE, obligatoryCoords, null, transaction,
                MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Creates a container in a space.
     *
     * @param name
     *            the name for the container, use <code>null</code> for an unnamed container
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param size
     *            the maximal number of entries in the container. Must not be negative, use
     *            {@link org.mozartspaces.core.MzsConstants.Container#UNBOUNDED UNBOUNDED} for an unbounded container
     * @param obligatoryCoords
     *            the obligatory coordinator list, may be <code>null</code> or an empty list; the AnyCoordinator is
     *            used, if the <code>optionalCoords</code> are also <code>null</code>
     * @param optionalCoords
     *            the optional coordinator list, may be <code>null</code> or an empty list
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     */
    public ContainerReference createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction) throws MzsCoreException {
        return createContainer(name, space, size, obligatoryCoords, optionalCoords, transaction,
                MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Creates a container in a space.
     *
     * @param name
     *            the name for the container, use <code>null</code> for an unnamed container
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param size
     *            the maximal number of entries in the container. Must not be negative, use
     *            {@link org.mozartspaces.core.MzsConstants.Container#UNBOUNDED UNBOUNDED} for an unbounded container
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param obligatoryCoords
     *            the obligatory coordinators, for <code>null</code> the AnyCoordinator is used
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     */
    public ContainerReference createContainer(final String name, final URI space, final int size,
            final TransactionReference transaction, final Coordinator... obligatoryCoords) throws MzsCoreException {
        List<Coordinator> obligatoryCoordsList = (obligatoryCoords == null) ? null : Arrays.asList(obligatoryCoords);
        return createContainer(name, space, size, obligatoryCoordsList, null, transaction,
                MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Creates a container in a space.
     *
     * @param name
     *            the name for the container, use <code>null</code> for an unnamed container
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param size
     *            the maximal number of entries in the container. Must not be negative, use
     *            {@link org.mozartspaces.core.MzsConstants.Container#UNBOUNDED UNBOUNDED} for an unbounded container
     * @param obligatoryCoords
     *            the obligatory coordinator list, may be <code>null</code> or an empty list; the AnyCoordinator is
     *            used, if the <code>optionalCoords</code> are also <code>null</code>
     * @param optionalCoords
     *            the optional coordinator list, may be <code>null</code> or an empty list
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     * @see CreateContainerRequest
     */
    public ContainerReference createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {

        List<? extends Coordinator> theObligatoryCoords;
        if ((obligatoryCoords == null || obligatoryCoords.isEmpty())
                && (optionalCoords == null || optionalCoords.isEmpty())) {
            theObligatoryCoords = Collections.singletonList(new AnyCoordinator());
        } else {
            theObligatoryCoords = obligatoryCoords;
        }
        // TODO enable secure containers by setting authorization level via parameter
        Request<ContainerReference> request = new CreateContainerRequest(name, size, theObligatoryCoords,
                optionalCoords, transaction, isolation, MzsConstants.DEFAULT_AUTHORIZATION,
                MzsConstants.Container.DEFAULT_FORCE_IN_MEMORY, context);
        return sendRequestAndWaitForResult(request, space);
    }

    /**
     * Creates a container in a space.
     *
     *
     * @param name
     *            the name for the container, use <code>null</code> for an unnamed container
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param size
     *            the maximal number of entries in the container. Must not be negative, use
     *            {@link org.mozartspaces.core.MzsConstants.Container#UNBOUNDED UNBOUNDED} for an unbounded container
     * @param obligatoryCoords
     *            the obligatory coordinator list, may be <code>null</code> or an empty list; the AnyCoordinator is
     *            used, if the <code>optionalCoords</code> are also <code>null</code>
     * @param optionalCoords
     *            the optional coordinator list, may be <code>null</code> or an empty list
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param forceInMemory
     *            override the persistence configuration and create an in-memory-only container. Default is
     *            {@link org.mozartspaces.core.MzsConstants.Container#DEFAULT_FORCE_IN_MEMORY}.
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the created container
     * @throws MzsCoreException
     *             if the container could not be created
     * @see CreateContainerRequest
     */
    public ContainerReference createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final boolean forceInMemory,
            final RequestContext context) throws MzsCoreException {

        List<? extends Coordinator> theObligatoryCoords;
        if ((obligatoryCoords == null || obligatoryCoords.isEmpty())
                && (optionalCoords == null || optionalCoords.isEmpty())) {
            theObligatoryCoords = Collections.singletonList(new AnyCoordinator());
        } else {
            theObligatoryCoords = obligatoryCoords;
        }
        Request<ContainerReference> request = new CreateContainerRequest(name, size, theObligatoryCoords,
                optionalCoords, transaction, isolation, MzsConstants.DEFAULT_AUTHORIZATION, forceInMemory, context);
        return sendRequestAndWaitForResult(request, space);
    }

    /**
     * Looks up a named container in the embedded space. No explicit transaction is used (default value
     * <code>null</code>).
     *
     * @param name
     *            the name of the container, must not be <code>null</code>
     * @return the reference of the looked up container
     * @throws MzsCoreException
     *             if the container could not be looked up
     */
    public ContainerReference lookupContainer(final String name) throws MzsCoreException {
        return lookupContainer(name, null, RequestTimeout.ZERO, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Looks up a named container.
     *
     * @param name
     *            the name of the container, must not be <code>null</code>
     * @param space
     *            the URI of the space where the container should be looked up, use <code>null</code> for the embedded
     *            space
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds. The timeout value must be <code>>= 0</code>, or a constant
     *            defined in {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the reference of the looked up container
     * @throws MzsCoreException
     *             if the container could not be looked up
     */
    public ContainerReference lookupContainer(final String name, final URI space, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        return lookupContainer(name, space, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Looks up a named container.
     *
     * @param name
     *            the name of the container, must not be <code>null</code>
     * @param space
     *            the URI of the space where the container should be looked up, use <code>null</code> for the embedded
     *            space
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds. The timeout value must be <code>>= 0</code>, or a constant
     *            defined in {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the looked up container
     * @throws MzsCoreException
     *             if the container could not be looked up
     * @see LookupContainerRequest
     */
    public ContainerReference lookupContainer(final String name, final URI space, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {
        Request<ContainerReference> request = new LookupContainerRequest(name, timeoutInMilliseconds, transaction,
                isolation, context);
        return sendRequestAndWaitForResult(request, space);
    }

    /**
     * Exclusively locks a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @throws MzsCoreException
     *             if the container could not be locked
     */
    public void lockContainer(final ContainerReference container, final TransactionReference transaction)
            throws MzsCoreException {
        lockContainer(container, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Exclusively locks a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the container could not be locked
     * @see LockContainerRequest
     */
    public void lockContainer(final ContainerReference container, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) throws MzsCoreException {
        Request<Nothing> request = new LockContainerRequest(container, transaction, isolation, context);
        sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Destroys a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @throws MzsCoreException
     *             if the container could not be destroyed
     */
    public void destroyContainer(final ContainerReference container, final TransactionReference transaction)
            throws MzsCoreException {
        destroyContainer(container, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Destroys a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the container could not be destroyed
     * @see DestroyContainerRequest
     */
    public void destroyContainer(final ContainerReference container, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) throws MzsCoreException {
        Request<Nothing> request = new DestroyContainerRequest(container, transaction, isolation, context);
        sendRequestAndWaitForResult(request, container.getSpace());
    }

    // entry methods
    /**
     * Writes an entry into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param entry
     *            the entry, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final Entry entry, final ContainerReference container) throws MzsCoreException {
        List<Entry> entries = Collections.singletonList(entry);
        write(entries, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes entries into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param entries
     *            the entry list, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final List<Entry> entries, final ContainerReference container) throws MzsCoreException {
        write(entries, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes entries into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param entries
     *            the entries, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final ContainerReference container, final Entry... entries) throws MzsCoreException {
        List<Entry> entryList = Arrays.asList(entries);
        write(entryList, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes an entry into a container.
     *
     * @param entry
     *            the entry, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final Entry entry, final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        List<Entry> entries = Collections.singletonList(entry);
        write(entries, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes entries into a container.
     *
     * @param entries
     *            the entry list, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final List<Entry> entries, final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        write(entries, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes entries into a container.
     *
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param entries
     *            the entries, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the entries could not be written
     */
    public void write(final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction, final Entry... entries) throws MzsCoreException {
        List<Entry> entryList = Arrays.asList(entries);
        write(entryList, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Writes entries into a container.
     *
     * @param entries
     *            the entry list, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the entries could not be written
     * @see WriteEntriesRequest
     */
    public void write(final List<Entry> entries, final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {
        Request<Nothing> request = new WriteEntriesRequest(entries, container, timeoutInMilliseconds, transaction,
                isolation, context);
        sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Reads entries from a container. An <code>AnySelector</code>, the default timeout and an implicit transaction is
     * used.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @return the read entries in a list
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public <R extends Serializable> ArrayList<R> read(final ContainerReference container) throws MzsCoreException {
        return read(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Reads entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the read entries in a list
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public <R extends Serializable> ArrayList<R> read(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        return read(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Reads entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selector
     *            the entry selector, for <code>null</code> an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the read entries in a list
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public <R extends Serializable> ArrayList<R> read(final ContainerReference container, final Selector selector,
            final long timeoutInMilliseconds, final TransactionReference transaction) throws MzsCoreException {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return read(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Reads entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the read entries in a list
     * @throws MzsCoreException
     *             if the entries could not be read
     * @see ReadEntriesRequest
     */
    public <R extends Serializable> ArrayList<R> read(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new ReadEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Tests for entries in a container. An <code>AnySelector</code>, the default timeout and an implicit transaction is
     * used.
     *
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @return the number of tested entries
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public int test(final ContainerReference container) throws MzsCoreException {
        return test(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Tests for entries in a container.
     *
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the number of tested entries
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public int test(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction) throws MzsCoreException {
        return test(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Tests for entries in a container.
     *
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selector
     *            the entry selector, for <code>null</code> an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the number of tested entries
     * @throws MzsCoreException
     *             if the entries could not be read
     */
    public int test(final ContainerReference container, final Selector selector, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return test(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Tests for entries in a container.
     *
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the number of tested entries
     * @throws MzsCoreException
     *             if the entries could not be read
     * @see ReadEntriesRequest
     */
    public int test(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context) throws MzsCoreException {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<Integer> request = new TestEntriesRequest(container, selectorList, timeoutInMilliseconds, transaction,
                isolation, context);
        return sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Takes entries from a container. An <code>AnySelector</code>, the default timeout and an implicit transaction is
     * used.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken, must not be <code>null</code>
     * @return the taken entries in a list
     * @throws MzsCoreException
     *             if the entries could not be taken
     */
    public <R extends Serializable> ArrayList<R> take(final ContainerReference container) throws MzsCoreException {
        return take(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Takes entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the taken entries in a list
     * @throws MzsCoreException
     *             if the entries could not be taken
     */
    public <R extends Serializable> ArrayList<R> take(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        return take(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Takes entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken, must not be <code>null</code>
     * @param selector
     *            the entry selector, for <code>null</code> an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the taken entries in a list
     * @throws MzsCoreException
     *             if the entries could not be taken
     */
    public <R extends Serializable> ArrayList<R> take(final ContainerReference container, final Selector selector,
            final long timeoutInMilliseconds, final TransactionReference transaction) throws MzsCoreException {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return take(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Takes entries from a container.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the taken entries in a list
     * @throws MzsCoreException
     *             if the entries could not be taken
     * @see TakeEntriesRequest
     */
    public <R extends Serializable> ArrayList<R> take(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new TakeEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Deletes entries from a container. An <code>AnySelector</code>, the default timeout and an implicit transaction is
     * used.
     *
     * @param container
     *            the reference of the container where entries should be deleted, must not be <code>null</code>
     * @return the number of deleted entries
     * @throws MzsCoreException
     *             if the entries could not be deleted
     */
    public int delete(final ContainerReference container) throws MzsCoreException {
        return delete(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Deletes entries from a container.
     *
     * @param container
     *            the reference of the container where entries should be deleted, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the number of deleted entries
     * @throws MzsCoreException
     *             if the entries could not be deleted
     */
    public int delete(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction) throws MzsCoreException {
        return delete(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Deletes entries from a container.
     *
     * @param container
     *            the reference of the container where entries should be deleted, must not be <code>null</code>
     * @param selector
     *            the entry selector, for <code>null</code> an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the number of deleted entries
     * @throws MzsCoreException
     *             if the entries could not be deleted
     */
    public int delete(final ContainerReference container, final Selector selector, final long timeoutInMilliseconds,
            final TransactionReference transaction) throws MzsCoreException {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return delete(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Deletes entries from a container.
     *
     * @param container
     *            the reference of the container where entries should be deleted, must not be <code>null</code>
     * @param selectors
     *            the entry selector list, for <code>null</code> or an empty list an <code>AnySelector</code> is used
     * @param timeoutInMilliseconds
     *            the request timeout in milliseconds, must be <code>>= 0</code> , or a constant defined in
     *            {@link org.mozartspaces.core.MzsConstants.RequestTimeout MzsConstants.RequestTimeout}.
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the number of deleted entries
     * @throws MzsCoreException
     *             if the entries could not be deleted
     * @see DeleteEntriesRequest
     */
    public int delete(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context) throws MzsCoreException {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<Integer> request = new DeleteEntriesRequest(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequestAndWaitForResult(request, container.getSpace());
    }

    // transaction methods
    /**
     * Creates a transaction.
     *
     * @param timeoutInMilliseconds
     *            the transaction timeout in milliseconds, must be <code>> 0</code> or
     *            {@link org.mozartspaces.core.MzsConstants.TransactionTimeout#INFINITE INFINITE}.
     * @param space
     *            the URI of the space where the transaction should be created, use <code>null</code> for the embedded
     *            space
     * @return the reference of the created transaction
     * @throws MzsCoreException
     *             if the transaction could not be created
     */
    public TransactionReference createTransaction(final long timeoutInMilliseconds, final URI space)
            throws MzsCoreException {
        return createTransaction(timeoutInMilliseconds, space, null);
    }

    /**
     * Creates a transaction.
     *
     * @param timeoutInMilliseconds
     *            the transaction timeout in milliseconds, must be <code>> 0</code> or
     *            {@link org.mozartspaces.core.MzsConstants.TransactionTimeout#INFINITE INFINITE}.
     * @param space
     *            the URI of the space where the transaction should be created, use <code>null</code> for the embedded
     *            space
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the created transaction
     * @throws MzsCoreException
     *             if the transaction could not be created
     * @see CreateTransactionRequest
     */
    public TransactionReference createTransaction(final long timeoutInMilliseconds, final URI space,
            final RequestContext context) throws MzsCoreException {
        Request<TransactionReference> request = new CreateTransactionRequest(timeoutInMilliseconds, context);
        return sendRequestAndWaitForResult(request, space);
    }

    /**
     * Commits a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be committed, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the transaction could not be committed
     */
    public void commitTransaction(final TransactionReference transaction) throws MzsCoreException {
        commitTransaction(transaction, null);
    }

    /**
     * Commits a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be committed, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the transaction could not be committed
     * @see CommitTransactionRequest
     */
    public void commitTransaction(final TransactionReference transaction, final RequestContext context)
            throws MzsCoreException {
        Request<Nothing> request = new CommitTransactionRequest(transaction, context);
        sendRequestAndWaitForResult(request, transaction.getSpace());
    }

    /**
     * Rollbacks a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be rollbacked, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the transaction could not be rollbacked
     */
    public void rollbackTransaction(final TransactionReference transaction) throws MzsCoreException {
        rollbackTransaction(transaction, null);
    }

    /**
     * Rollbacks a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be rollbacked, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the transaction could not be rollbacked
     * @see RollbackTransactionRequest
     */
    public void rollbackTransaction(final TransactionReference transaction, final RequestContext context)
            throws MzsCoreException {
        Request<Nothing> request = new RollbackTransactionRequest(transaction, context);
        sendRequestAndWaitForResult(request, transaction.getSpace());
    }

    // aspect methods
    /**
     * Adds an aspect to a container.
     *
     * @param aspect
     *            the container aspect that should be added, must not be <code>null</code>
     * @param container
     *            the reference of the container where the aspect should be added, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be invoked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     */
    public AspectReference addContainerAspect(final ContainerAspect aspect, final ContainerReference container,
            final Set<? extends ContainerIPoint> iPoints, final TransactionReference transaction)
            throws MzsCoreException {
        return addContainerAspect(aspect, container, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Adds an aspect to a container.
     *
     * @param aspect
     *            the container aspect that should be added, must not be <code>null</code>
     * @param container
     *            the reference of the container where the aspect should be added, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be invoked, must not be <code>null</code>
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     */
    public AspectReference addContainerAspect(final ContainerAspect aspect, final ContainerReference container,
            final ContainerIPoint... iPoints) throws MzsCoreException {
        Set<ContainerIPoint> iPointSet = new HashSet<ContainerIPoint>(Arrays.asList(iPoints));
        return addContainerAspect(aspect, container, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Adds an aspect to a container.
     *
     * @param aspect
     *            the container aspect that should be added, must not be <code>null</code>
     * @param container
     *            the reference of the container where the aspect should be added, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be invoked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     * @see AddAspectRequest
     */
    public AspectReference addContainerAspect(final ContainerAspect aspect, final ContainerReference container,
            final Set<? extends ContainerIPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) throws MzsCoreException {
        Request<AspectReference> request = new AddAspectRequest(aspect, container, iPoints, transaction, isolation,
                context);
        return sendRequestAndWaitForResult(request, container.getSpace());
    }

    /**
     * Adds an aspect to a space.
     *
     * @param aspect
     *            the space aspect that should be added, must not be <code>null</code>
     * @param space
     *            the URI of the space where the aspect should be added, use <code>null</code> for the embedded space
     * @param iPoints
     *            the interception points where this aspect should be invoked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     */
    public AspectReference addSpaceAspect(final SpaceAspect aspect, final URI space,
            final Set<? extends SpaceIPoint> iPoints, final TransactionReference transaction) throws MzsCoreException {
        return addSpaceAspect(aspect, space, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Adds an aspect to a space.
     *
     * @param aspect
     *            the space aspect that should be added, must not be <code>null</code>
     * @param space
     *            the URI of the space where the aspect should be added, use <code>null</code> for the embedded space
     * @param iPoints
     *            the interception points where this aspect should be invoked, must not be <code>null</code>
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     */
    public AspectReference addSpaceAspect(final SpaceAspect aspect, final URI space, final SpaceIPoint... iPoints)
            throws MzsCoreException {
        Set<SpaceIPoint> iPointSet = new HashSet<SpaceIPoint>(Arrays.asList(iPoints));
        return addSpaceAspect(aspect, space, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Adds an aspect to a space.
     *
     * @param aspect
     *            the space aspect that should be added, must not be <code>null</code>
     * @param space
     *            the URI of the space where the aspect should be added, use <code>null</code> for the embedded space
     * @param iPoints
     *            the interception points where this aspect should be invoked
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @return the reference of the added aspect
     * @throws MzsCoreException
     *             if the aspect could not be added
     * @see AddAspectRequest
     */
    public AspectReference addSpaceAspect(final SpaceAspect aspect, final URI space,
            final Set<? extends SpaceIPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context) throws MzsCoreException {
        Request<AspectReference> request = new AddAspectRequest(aspect, null, iPoints, transaction, isolation, context);
        return sendRequestAndWaitForResult(request, space);
    }

    /**
     * Removes a container or space aspect. The aspect is removed from all interception points.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @throws MzsCoreException
     *             if the aspect could not be removed
     */
    public void removeAspect(final AspectReference aspect) throws MzsCoreException {
        removeAspect(aspect, null, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Removes a container or space aspect.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be removed, use <code>null</code> to remove it from
     *            all interception points
     * @throws MzsCoreException
     *             if the aspect could not be removed
     */
    public void removeAspect(final AspectReference aspect, final InterceptionPoint... iPoints) throws MzsCoreException {
        Set<InterceptionPoint> iPointSet = (iPoints == null) ? null : new HashSet<InterceptionPoint>(
                Arrays.asList(iPoints));
        removeAspect(aspect, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Removes a container or space aspect.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be removed, use <code>null</code> to remove it from
     *            all interception points
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @throws MzsCoreException
     *             if the aspect could not be removed
     */
    public void removeAspect(final AspectReference aspect, final Set<? extends InterceptionPoint> iPoints,
            final TransactionReference transaction) throws MzsCoreException {
        removeAspect(aspect, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null);
    }

    /**
     * Removes a container or space aspect.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be removed, use <code>null</code> to remove it from
     *            all interception points
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @param isolation
     *            the transaction isolation level for this request, you can use <code>null</code> for the default
     *            isolation level
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the aspect could not be removed
     * @see RemoveAspectRequest
     */
    public void removeAspect(final AspectReference aspect, final Set<? extends InterceptionPoint> iPoints,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context)
            throws MzsCoreException {
        Request<Nothing> request = new RemoveAspectRequest(aspect, iPoints, transaction, isolation, context);
        sendRequestAndWaitForResult(request, aspect.getSpace());
    }

    // other methods
    /**
     * Clears a space. This method is NOT implemented yet!
     *
     * @param space
     *            the URI of the space that should be cleared, use <code>null</code> for the embedded space
     * @throws MzsCoreException
     *             if the space could not be cleared
     */
    public void clearSpace(final URI space) throws MzsCoreException {
        clearSpace(space, null);
    }

    /**
     * Clears a space. This method is NOT implemented yet!
     *
     * @param space
     *            the URI of the space that should be cleared, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the space could not be cleared
     * @see ClearSpaceRequest
     */
    public void clearSpace(final URI space, final RequestContext context) throws MzsCoreException {
        Request<Nothing> request = new ClearSpaceRequest(context);
        sendRequestAndWaitForResult(request, space);
    }

    /**
     * Shuts down a space.
     *
     * @param space
     *            the URI of the space that should be shut down, use <code>null</code> for the embedded space
     * @throws MzsCoreException
     *             if the space could not be shut down
     */
    public void shutdown(final URI space) throws MzsCoreException {
        shutdown(space, null);
    }

    /**
     * Shuts down a space.
     *
     * @param space
     *            the URI of the space that should be shut down, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @throws MzsCoreException
     *             if the space could not be shut down
     * @see ShutdownRequest
     */
    public void shutdown(final URI space, final RequestContext context) throws MzsCoreException {
        Request<Nothing> request = new ShutdownRequest(context);
        sendRequestAndWaitForResult(request, space);
    }

    private List<? extends Selector> ensureSelectorsNotNull(final List<? extends Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return Collections.singletonList(AnyCoordinator.newSelector());
        }
        return selectors;
    }

    private <R extends Serializable> R sendRequestAndWaitForResult(final Request<R> request, final URI space)
            throws MzsCoreException {
        try {
            // TODO add configurable timeout?
            return core.send(request, space).getResult();
        } catch (InterruptedException ex) {
            throw new MzsCoreRuntimeException(ex);
        }
    }
}
