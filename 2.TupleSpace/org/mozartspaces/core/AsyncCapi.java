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
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.Nothing;

/**
 * Asynchronous wrapper interface around the core requests. The methods of this interface create request objects, send
 * them to the {@link MzsCore} instance, and return a {@link RequestFuture} which can be used to the result.
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
public final class AsyncCapi {

    private final MzsCore core;

    /**
     * Constructs an <code>AsyncCapi</code> instance.
     *
     * @param core
     *            the core to which the requests are sent
     */
    public AsyncCapi(final MzsCore core) {
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
     * Creates an unbounded, unnamed container with the AnyCoordinator on the embedded space.
     *
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<ContainerReference> createContainer() {
        return createContainer(Container.UNNAMED, null, Container.DEFAULT_SIZE, null, null, null,
                MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Creates an unbounded, unnamed container on a space.
     *
     * @param space
     *            the URI of the space where the container should be created, use <code>null</code> for the embedded
     *            space
     * @param obligatoryCoords
     *            the obligatory coordinator list, for <code>null</code> or an empty list the AnyCoordinator is used
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<ContainerReference> createContainer(final URI space,
            final List<? extends Coordinator> obligatoryCoords, final TransactionReference transaction) {
        return createContainer(Container.UNNAMED, space, Container.DEFAULT_SIZE, obligatoryCoords, null, transaction,
                MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Creates a container on a space.
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
     *            the obligatory coordinator list, may be <code>null</code> or an empty list, the AnyCoordinator is used
     *            if the <code>optionalCoords</code> are also <code>null</code>
     * @param optionalCoords
     *            the optional coordinator list, may be <code>null</code> or an empty list
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<ContainerReference> createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction) {
        return createContainer(name, space, size, obligatoryCoords, optionalCoords, transaction,
                MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Creates a container on a space.
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
    public RequestFuture<ContainerReference> createContainer(final String name, final URI space, final int size,
            final TransactionReference transaction, final Coordinator... obligatoryCoords) throws MzsCoreException {
        List<Coordinator> obligatoryCoordsList = (obligatoryCoords == null) ? null : Arrays.asList(obligatoryCoords);
        return createContainer(name, space, size, obligatoryCoordsList, null, transaction,
                MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Creates a container on a space.
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see CreateContainerRequest
     */
    public RequestFuture<ContainerReference> createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        return createContainer(name, space, size, obligatoryCoords, optionalCoords, transaction, isolation,
                MzsConstants.Container.DEFAULT_FORCE_IN_MEMORY, context, callbackHandler);
    }

    /**
     * Creates a container on a space.
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see CreateContainerRequest
     */
    public RequestFuture<ContainerReference> createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final boolean forceInMemory,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {

        List<? extends Coordinator> theObligatoryCoords = ensureCoordinatorsNotEmpty(obligatoryCoords, optionalCoords);
        // TODO enable secure containers by setting authorization level via parameter
        Request<ContainerReference> request = new CreateContainerRequest(name, size, theObligatoryCoords,
                optionalCoords, transaction, isolation, MzsConstants.DEFAULT_AUTHORIZATION, forceInMemory, context);
        return sendRequest(request, space, callbackHandler);
    }

    /**
     * Creates a container on a space.
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see CreateContainerRequest
     */
    public void createContainer(final String name, final URI space, final int size,
            final List<? extends Coordinator> obligatoryCoords, final List<? extends Coordinator> optionalCoords,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {

        List<? extends Coordinator> theObligatoryCoords = ensureCoordinatorsNotEmpty(obligatoryCoords, optionalCoords);
        // TODO enable secure containers by setting authorization level via parameter
        Request<ContainerReference> request = new CreateContainerRequest(name, size, theObligatoryCoords,
                optionalCoords, transaction, isolation, MzsConstants.DEFAULT_AUTHORIZATION,
                MzsConstants.Container.DEFAULT_FORCE_IN_MEMORY, context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    /**
     * Looks up a named container on the embedded space. No explicit transaction is used (default value
     * <code>null</code>).
     *
     * @param name
     *            the name of the container, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<ContainerReference> lookupContainer(final String name) {
        return lookupContainer(name, null, RequestTimeout.ZERO, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<ContainerReference> lookupContainer(final String name, final URI space,
            final long timeoutInMilliseconds, final TransactionReference transaction) {
        return lookupContainer(name, space, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see LookupContainerRequest
     */
    public RequestFuture<ContainerReference> lookupContainer(final String name, final URI space,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<ContainerReference> request = new LookupContainerRequest(name, timeoutInMilliseconds, transaction,
                isolation, context);
        return sendRequest(request, space, callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see LookupContainerRequest
     */
    public void lookupContainer(final String name, final URI space, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<ContainerReference> request = new LookupContainerRequest(name, timeoutInMilliseconds, transaction,
                isolation, context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    /**
     * Exclusively locks a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> lockContainer(final ContainerReference container,
            final TransactionReference transaction) {
        return lockContainer(container, transaction, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see LockContainerRequest
     */
    public RequestFuture<Nothing> lockContainer(final ContainerReference container,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new LockContainerRequest(container, transaction, isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see LockContainerRequest
     */
    public void lockContainer(final ContainerReference container, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context, final ContainerReference answerContainer,
            final String coordinationKey) {
        Request<Nothing> request = new LockContainerRequest(container, transaction, isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
    }

    /**
     * Destroys a container.
     *
     * @param container
     *            the reference of the container that should be locked, must not be <code>null</code>
     * @param transaction
     *            the transaction reference, use <code>null</code> for an implicit transaction
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> destroyContainer(final ContainerReference container,
            final TransactionReference transaction) {
        return destroyContainer(container, transaction, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see DestroyContainerRequest
     */
    public RequestFuture<Nothing> destroyContainer(final ContainerReference container,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new DestroyContainerRequest(container, transaction, isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see DestroyContainerRequest
     */
    public void destroyContainer(final ContainerReference container, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context, final ContainerReference answerContainer,
            final String coordinationKey) {
        Request<Nothing> request = new DestroyContainerRequest(container, transaction, isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
    }

    // entry methods
    /**
     * Writes an entry into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param entry
     *            the entry, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final Entry entry, final ContainerReference container) {
        List<Entry> entries = Collections.singletonList(entry);
        return write(entries, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Writes entries into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param entries
     *            the entry list, must not be <code>null</code>
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final List<Entry> entries, final ContainerReference container) {
        return write(entries, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Writes entries into a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param container
     *            the reference of the container where entries should be written, must not be <code>null</code>
     * @param entries
     *            the entries, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final ContainerReference container, final Entry... entries) {
        List<Entry> entryList = Arrays.asList(entries);
        return write(entryList, container, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final Entry entry, final ContainerReference container,
            final long timeoutInMilliseconds, final TransactionReference transaction) {
        List<Entry> entries = Collections.singletonList(entry);
        return write(entries, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final List<Entry> entries, final ContainerReference container,
            final long timeoutInMilliseconds, final TransactionReference transaction) {
        return write(entries, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> write(final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction, final Entry... entries) {
        List<Entry> entryList = Arrays.asList(entries);
        return write(entryList, container, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see WriteEntriesRequest
     */
    public RequestFuture<Nothing> write(final List<Entry> entries, final ContainerReference container,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new WriteEntriesRequest(entries, container, timeoutInMilliseconds, transaction,
                isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see WriteEntriesRequest
     */
    public void write(final List<Entry> entries, final ContainerReference container, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<Nothing> request = new WriteEntriesRequest(entries, container, timeoutInMilliseconds, transaction,
                isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
    }

    /**
     * Reads entries from a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be read, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> read(final ContainerReference container) {
        return read(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> read(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction) {
        return read(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> read(final ContainerReference container,
            final Selector selector, final long timeoutInMilliseconds, final TransactionReference transaction) {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return read(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see ReadEntriesRequest
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> read(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new ReadEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see ReadEntriesRequest
     */
    public <R extends Serializable> void read(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new ReadEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
    }

    /**
     * Takes entries from a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param <R>
     *            the type of the entries
     * @param container
     *            the reference of the container where entries should be taken, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> take(final ContainerReference container) {
        return take(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> take(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction) {
        return take(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> take(final ContainerReference container,
            final Selector selector, final long timeoutInMilliseconds, final TransactionReference transaction) {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return take(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see TakeEntriesRequest
     */
    public <R extends Serializable> RequestFuture<ArrayList<R>> take(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new TakeEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see TakeEntriesRequest
     */
    public <R extends Serializable> void take(final ContainerReference container,
            final List<? extends Selector> selectors, final long timeoutInMilliseconds,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<ArrayList<R>> request = new TakeEntriesRequest<R>(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
    }

    /**
     * Deletes entries from a container. The AnyCoordinator, the default timeout and an implicit transaction is used.
     *
     * @param container
     *            the reference of the container where entries should be deleted, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Integer> delete(final ContainerReference container) {
        return delete(container, null, RequestTimeout.DEFAULT, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Integer> delete(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction) {
        return delete(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Integer> delete(final ContainerReference container, final Selector selector,
            final long timeoutInMilliseconds, final TransactionReference transaction) {
        List<Selector> selectors = (selector == null) ? null : Collections.singletonList(selector);
        return delete(container, selectors, timeoutInMilliseconds, transaction, MzsConstants.DEFAULT_ISOLATION, null,
                null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see DeleteEntriesRequest
     */
    public RequestFuture<Integer> delete(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<Integer> request = new DeleteEntriesRequest(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see DeleteEntriesRequest
     */
    public void delete(final ContainerReference container, final List<? extends Selector> selectors,
            final long timeoutInMilliseconds, final TransactionReference transaction, final IsolationLevel isolation,
            final RequestContext context, final ContainerReference answerContainer, final String coordinationKey) {
        List<? extends Selector> selectorList = ensureSelectorsNotNull(selectors);
        Request<Integer> request = new DeleteEntriesRequest(container, selectorList, timeoutInMilliseconds,
                transaction, isolation, context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<TransactionReference> createTransaction(final long timeoutInMilliseconds, final URI space) {
        return createTransaction(timeoutInMilliseconds, space, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see CreateTransactionRequest
     */
    public RequestFuture<TransactionReference> createTransaction(final long timeoutInMilliseconds, final URI space,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<TransactionReference> request = new CreateTransactionRequest(timeoutInMilliseconds, context);
        return sendRequest(request, space, callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see CreateTransactionRequest
     */
    public void createTransaction(final long timeoutInMilliseconds, final URI space, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<TransactionReference> request = new CreateTransactionRequest(timeoutInMilliseconds, context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    /**
     * Commits a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be committed, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> commitTransaction(final TransactionReference transaction) {
        return commitTransaction(transaction, null, null);
    }

    /**
     * Commits a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be committed, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see CommitTransactionRequest
     */
    public RequestFuture<Nothing> commitTransaction(final TransactionReference transaction,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new CommitTransactionRequest(transaction, context);
        return sendRequest(request, transaction.getSpace(), callbackHandler);
    }

    /**
     * Commits a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be committed, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see CommitTransactionRequest
     */
    public void commitTransaction(final TransactionReference transaction, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<Nothing> request = new CommitTransactionRequest(transaction, context);
        sendRequest(request, transaction.getSpace(), answerContainer, coordinationKey);
    }

    /**
     * Rollbacks a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be rollbacked, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> rollbackTransaction(final TransactionReference transaction) {
        return rollbackTransaction(transaction, null, null);
    }

    /**
     * Rollbacks a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be rollbacked, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see RollbackTransactionRequest
     */
    public RequestFuture<Nothing> rollbackTransaction(final TransactionReference transaction,
            final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new RollbackTransactionRequest(transaction, context);
        return sendRequest(request, transaction.getSpace(), callbackHandler);
    }

    /**
     * Rollbacks a transaction.
     *
     * @param transaction
     *            the reference of the transaction that should be rollbacked, must not be <code>null</code>
     * @param context
     *            the request context, may be <code>null</code>
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see RollbackTransactionRequest
     */
    public void rollbackTransaction(final TransactionReference transaction, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<Nothing> request = new RollbackTransactionRequest(transaction, context);
        sendRequest(request, transaction.getSpace(), answerContainer, coordinationKey);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<AspectReference> addContainerAspect(final ContainerAspect aspect,
            final ContainerReference container, final Set<? extends ContainerIPoint> iPoints,
            final TransactionReference transaction) {
        return addContainerAspect(aspect, container, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<AspectReference> addContainerAspect(final ContainerAspect aspect,
            final ContainerReference container, final ContainerIPoint... iPoints) {
        Set<ContainerIPoint> iPointSet = new HashSet<ContainerIPoint>(Arrays.asList(iPoints));
        return addContainerAspect(aspect, container, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see AddAspectRequest
     */
    public RequestFuture<AspectReference> addContainerAspect(final ContainerAspect aspect,
            final ContainerReference container, final Set<? extends ContainerIPoint> iPoints,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<AspectReference> request = new AddAspectRequest(aspect, container, iPoints, transaction, isolation,
                context);
        return sendRequest(request, container.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see AddAspectRequest
     */
    public void addContainerAspect(final ContainerAspect aspect, final ContainerReference container,
            final Set<? extends ContainerIPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context, final ContainerReference answerContainer,
            final String coordinationKey) {
        Request<AspectReference> request = new AddAspectRequest(aspect, container, iPoints, transaction, isolation,
                context);
        sendRequest(request, container.getSpace(), answerContainer, coordinationKey);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<AspectReference> addSpaceAspect(final SpaceAspect aspect, final URI space,
            final Set<? extends SpaceIPoint> iPoints, final TransactionReference transaction) {
        return addSpaceAspect(aspect, space, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<AspectReference> addSpaceAspect(final SpaceAspect aspect, final URI space,
            final SpaceIPoint... iPoints) {
        Set<SpaceIPoint> iPointSet = new HashSet<SpaceIPoint>(Arrays.asList(iPoints));
        return addSpaceAspect(aspect, space, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see AddAspectRequest
     */
    public RequestFuture<AspectReference> addSpaceAspect(final SpaceAspect aspect, final URI space,
            final Set<? extends SpaceIPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<AspectReference> request = new AddAspectRequest(aspect, null, iPoints, transaction, isolation, context);
        return sendRequest(request, space, callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see AddAspectRequest
     */
    public void addSpaceAspect(final SpaceAspect aspect, final URI space, final Set<? extends SpaceIPoint> iPoints,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<AspectReference> request = new AddAspectRequest(aspect, null, iPoints, transaction, isolation, context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    /**
     * Removes a container or space aspect. The aspect is removed from all interception points.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> removeAspect(final AspectReference aspect) {
        return removeAspect(aspect, null, null, MzsConstants.DEFAULT_ISOLATION, null, null);
    }

    /**
     * Removes a container or space aspect.
     *
     * @param aspect
     *            the reference of the aspect that should be removed, must not be <code>null</code>
     * @param iPoints
     *            the interception points where this aspect should be removed, use <code>null</code> to remove it from
     *            all interception points
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> removeAspect(final AspectReference aspect, final InterceptionPoint... iPoints) {
        Set<InterceptionPoint> iPointSet = (iPoints == null) ? null : new HashSet<InterceptionPoint>(
                Arrays.asList(iPoints));
        return removeAspect(aspect, iPointSet, null, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> removeAspect(final AspectReference aspect,
            final Set<? extends InterceptionPoint> iPoints, final TransactionReference transaction) {
        return removeAspect(aspect, iPoints, transaction, MzsConstants.DEFAULT_ISOLATION, null, null);
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
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see RemoveAspectRequest
     */
    public RequestFuture<Nothing> removeAspect(final AspectReference aspect,
            final Set<? extends InterceptionPoint> iPoints, final TransactionReference transaction,
            final IsolationLevel isolation, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new RemoveAspectRequest(aspect, iPoints, transaction, isolation, context);
        return sendRequest(request, aspect.getSpace(), callbackHandler);
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
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see RemoveAspectRequest
     */
    public void removeAspect(final AspectReference aspect, final Set<? extends InterceptionPoint> iPoints,
            final TransactionReference transaction, final IsolationLevel isolation, final RequestContext context,
            final ContainerReference answerContainer, final String coordinationKey) {
        Request<Nothing> request = new RemoveAspectRequest(aspect, iPoints, transaction, isolation, context);
        sendRequest(request, aspect.getSpace(), answerContainer, coordinationKey);
    }

    // other methods
    /**
     * Clears a space.
     *
     * @param space
     *            the URI of the space that should be cleared, use <code>null</code> for the embedded space
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> clearSpace(final URI space) {
        return clearSpace(space, null, null);
    }

    /**
     * Clears a space.
     *
     * @param space
     *            the URI of the space that should be cleared, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see ClearSpaceRequest
     */
    public RequestFuture<Nothing> clearSpace(final URI space, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new ClearSpaceRequest(context);
        return sendRequest(request, space, callbackHandler);
    }

    /**
     * Clears a space.
     *
     * @param space
     *            the URI of the space that should be cleared, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see ClearSpaceRequest
     */
    public void clearSpace(final URI space, final RequestContext context, final ContainerReference answerContainer,
            final String coordinationKey) {
        Request<Nothing> request = new ClearSpaceRequest(context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    /**
     * Shuts down a space.
     *
     * @param space
     *            the URI of the space that should be shut down, use <code>null</code> for the embedded space
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     */
    public RequestFuture<Nothing> shutdown(final URI space) {
        return shutdown(space, null, null);
    }

    /**
     * Shuts down a space.
     *
     * @param space
     *            the URI of the space that should be shut down, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @param callbackHandler
     *            the callback handler, gets invoked when an answer for this request is received, may be
     *            <code>null</code>
     * @return a <code>RequestFuture</code> representing the pending result of the sent request
     * @see ShutdownRequest
     */
    public RequestFuture<Nothing> shutdown(final URI space, final RequestContext context,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        Request<Nothing> request = new ShutdownRequest(context);
        return sendRequest(request, space, callbackHandler);
    }

    /**
     * Shuts down a space.
     *
     * @param space
     *            the URI of the space that should be shut down, use <code>null</code> for the embedded space
     * @param context
     *            the request context, may be <code>null</code>
     * @param answerContainer
     *            the container where the answer is written
     * @param coordinationKey
     *            the coordination key, used as key for writing the answer, may be <code>null</code>
     * @see ShutdownRequest
     */
    public void shutdown(final URI space, final RequestContext context, final ContainerReference answerContainer,
            final String coordinationKey) {
        Request<Nothing> request = new ShutdownRequest(context);
        sendRequest(request, space, answerContainer, coordinationKey);
    }

    private List<? extends Coordinator> ensureCoordinatorsNotEmpty(final List<? extends Coordinator> obligatoryCoords,
            final List<? extends Coordinator> optionalCoords) {
        List<? extends Coordinator> theObligatoryCoords;
        if ((obligatoryCoords == null || obligatoryCoords.isEmpty())
                && (optionalCoords == null || optionalCoords.isEmpty())) {
            theObligatoryCoords = Collections.singletonList(new AnyCoordinator());
        } else {
            theObligatoryCoords = obligatoryCoords;
        }
        return theObligatoryCoords;
    }

    private List<? extends Selector> ensureSelectorsNotNull(final List<? extends Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return Collections.singletonList(AnyCoordinator.newSelector());
        }
        return selectors;
    }

    private <R extends Serializable> RequestFuture<R> sendRequest(final Request<R> request, final URI space,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> callbackHandler) {
        return core.send(request, space, callbackHandler);
    }

    private <R extends Serializable> void sendRequest(final Request<R> request, final URI space,
            final ContainerReference answerContainer, final String coordinationKey) {
        core.send(request, space, answerContainer, coordinationKey);
    }
}
