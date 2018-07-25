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
package org.mozartspaces.capi3.javanative.operation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.mozartspaces.capi3.AccessDeniedException;
import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.ContainerFullException;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CoordinatorNotRegisteredException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.DuplicateCoordinatorException;
import org.mozartspaces.capi3.DuplicateKeyException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidContainerNameException;
import org.mozartspaces.capi3.InvalidEntryException;
import org.mozartspaces.capi3.InvalidSubTransactionException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.ObligatoryCoordinatorMissingException;
import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.TransactionStatus;
import org.mozartspaces.capi3.javanative.LockedExceptionsHelper;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationResult;
import org.mozartspaces.capi3.javanative.authorization.AuthorizationType;
import org.mozartspaces.capi3.javanative.authorization.NativeAccessManager;
import org.mozartspaces.capi3.javanative.coordination.ImplicitNativeCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeSelector;
import org.mozartspaces.capi3.javanative.isolation.Availability;
import org.mozartspaces.capi3.javanative.isolation.Availability.AvailabilityType;
import org.mozartspaces.capi3.javanative.isolation.DefaultReadLogItem;
import org.mozartspaces.capi3.javanative.isolation.DefaultTakeLogItem;
import org.mozartspaces.capi3.javanative.isolation.DefaultWriteLogItem;
import org.mozartspaces.capi3.javanative.isolation.LockResult;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.capi3.javanative.persistence.key.LongPersistenceKey;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container.Counters;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;
import org.mozartspaces.util.AndroidHelperUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The DefaultContainer is a Implementation of the <code>NativeContainer</code> Interface.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 * @author Jan Zarnikov
 */
public final class DefaultContainer implements NativeContainer {

    private static final Logger log = LoggerFactory.get();

    // TODO make ID counter non-static (this requires also a container-specific isolation manager!)
    private static final AtomicLong ENTRY_ID_COUNTER = new AtomicLong();

    private final long containerId;
    private final LocalContainerReference containerReference;
    private final String containerName;

    private final int containerSize;
    private final AtomicInteger numberOfEntries;

    private final ConcurrentHashMap<String, NativeCoordinator> obligatoryCoordinators;
    private final ConcurrentHashMap<String, NativeCoordinator> optionalCoordinators;
    private final StoredMap<Long, NativeEntry> storedEntries;
    private final Set<NativeEntry> entrySet;

    private volatile boolean validContainer = true;
    private final NativeIsolationManager isolationManager;
    private final NativeAccessManager accessManager;

    private final AtomicLong writeOpCounter;
    private final AtomicLong successfulWriteOpCounter;
    private final AtomicLong readOpCounter;
    private final AtomicLong successfulReadOpCounter;
    private final AtomicLong takeOpCounter;
    private final AtomicLong successfulTakeOpCounter;

    private final PersistentContainerDescriptor persistentContainerDescriptor;
    private final PersistenceContext persistenceContext;

    private final Map<String, Object> metaModel;

    private static final Method GET_ENTRY_SET_METHOD;

    static {
        try {
            GET_ENTRY_SET_METHOD = DefaultContainer.class.getDeclaredMethod("getEntrySet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Constructor for the DefaultContainer.
     *
     * @param id
     *            the container ID
     * @param isolationManager
     *            the IsolationManager
     * @param auth
     *            the authorization level
     * @param accessManager
     *            the AccessManager
     * @param persistenceContext
     *            the persistence context
     * @param containerName
     *            the container Name
     * @param containerSize
     *            Container size
     * @param obligatoryCoordinatorArgs
     *            list of obligatory coordinators as specified in the request (for use in the meta model)
     * @param obligatoryCoordinators
     *            the obligatory coordinators
     * @param optionalCoordinatorArgs
     *            list of optional coordinators as specified in the request (for use in the meta model)
     * @param optionalCoordinators
     *            the optional coordinators
     * @throws InvalidContainerNameException
     *             if the ContainerName is invalid
     * @throws DuplicateCoordinatorException
     *             if two Coordinators with the same name are registered at the Container
     * @throws PersistenceException
     *             if the persistent storage for this container could not be initialized
     */
    public DefaultContainer(final long id, final NativeIsolationManager isolationManager,
            final AuthorizationLevel auth, final NativeAccessManager accessManager,
            final PersistenceContext persistenceContext, final String containerName, final int containerSize,
            final List<? extends Coordinator> obligatoryCoordinatorArgs,
            final List<NativeCoordinator> obligatoryCoordinators,
            final List<? extends Coordinator> optionalCoordinatorArgs,
            final List<NativeCoordinator> optionalCoordinators) throws InvalidContainerNameException,
            DuplicateCoordinatorException, PersistenceException {

        assert isolationManager != null : "The isolationManager must be injected.";
        // access manager is optional

        if (!validContainerName(containerName)) {
            throw new InvalidContainerNameException(containerName);
        }
        this.containerId = id;
        this.containerReference = new LocalContainerReference(Long.toString(this.containerId));
        this.containerName = containerName;

        this.containerSize = (containerSize > 0) ? containerSize : Integer.MAX_VALUE;
        this.numberOfEntries = new AtomicInteger(0);

        this.obligatoryCoordinators = new ConcurrentHashMap<String, NativeCoordinator>();
        this.optionalCoordinators = new ConcurrentHashMap<String, NativeCoordinator>();
        // this.entries = new ConcurrentHashMap<NativeEntry, NativeEntry>();
        this.entrySet = Collections.synchronizedSet(new HashSet<NativeEntry>());
        this.persistenceContext = persistenceContext;
        final String storedEntriesStoredMapName = persistenceContext.generateStoredMapName(getClass(), getIdAsString());
        this.storedEntries = persistenceContext.createStoredMap(storedEntriesStoredMapName,
                new LongPersistenceKey.LongPersistenceKeyFactory());

        long highestId = 0;
        // restore the entries from the persistent map
        // note that this only loads the ids and NOT the content of the entries (which is lazy-loaded)
        for (Long entryId : storedEntries.keySet()) {
            // restore the entry ID counter (which is static and thus used for all containers!!)
            if (entryId > highestId) {
                highestId = entryId;
                if (highestId > ENTRY_ID_COUNTER.get()) {
                    ENTRY_ID_COUNTER.set(highestId);
                }
            }
            NativeEntry lazyNativeEntry = new LazyNativeEntry(entryId, this);
            entrySet.add(lazyNativeEntry);
        }

        this.isolationManager = isolationManager;
        this.accessManager = accessManager;

        for (NativeCoordinator coord : optionalCoordinators) {
            coord.setIsolationManager(this.isolationManager);
            if (this.optionalCoordinators.putIfAbsent(coord.getName(), coord) != null) {
                throw new DuplicateCoordinatorException(coord.getName());
            }
        }
        for (NativeCoordinator coord : obligatoryCoordinators) {
            coord.setIsolationManager(this.isolationManager);
            this.obligatoryCoordinators.put(coord.getName(), coord);
            // TODO misleading, ALL coordinators are put to
            // optionalCoordinators!
            if (this.optionalCoordinators.putIfAbsent(coord.getName(), coord) != null) {
                throw new DuplicateCoordinatorException(coord.getName());
            }
        }
        List<PersistentCoordinator.CoordinatorRestoreTask> persistentObligatoryCoordinators =
                new LinkedList<PersistentCoordinator.CoordinatorRestoreTask>();
        List<Class<? extends NativeCoordinator>> obligatoryCoordinatorClasses =
                new LinkedList<Class<? extends NativeCoordinator>>();
        for (NativeCoordinator coordinator : obligatoryCoordinators) {
            if (coordinator instanceof PersistentCoordinator) {
                persistentObligatoryCoordinators.add(((PersistentCoordinator) coordinator).getRestoreTask());
            } else {
                obligatoryCoordinatorClasses.add(coordinator.getClass());
            }
        }

        List<PersistentCoordinator.CoordinatorRestoreTask> persistentOptionalCoordinators =
                new LinkedList<PersistentCoordinator.CoordinatorRestoreTask>();
        List<Class<? extends NativeCoordinator>> optionalCoordinatorClasses =
                new LinkedList<Class<? extends NativeCoordinator>>();
        for (NativeCoordinator coordinator : optionalCoordinators) {
            if (coordinator instanceof PersistentCoordinator) {
                persistentOptionalCoordinators.add(((PersistentCoordinator) coordinator).getRestoreTask());
            } else {
                optionalCoordinatorClasses.add(coordinator.getClass());
            }
        }
        persistentContainerDescriptor = new PersistentContainerDescriptor(containerId, containerName,
                obligatoryCoordinatorClasses, optionalCoordinatorClasses, obligatoryCoordinatorArgs,
                optionalCoordinatorArgs, persistentObligatoryCoordinators, persistentOptionalCoordinators,
                containerSize, auth);

        writeOpCounter = new AtomicLong();
        successfulWriteOpCounter = new AtomicLong();
        readOpCounter = new AtomicLong();
        successfulReadOpCounter = new AtomicLong();
        takeOpCounter = new AtomicLong();
        successfulTakeOpCounter = new AtomicLong();

        metaModel = new HashMap<String, Object>();
        metaModel.put(Container.NAME, containerName);
        metaModel.put(Container.MAXSIZE, containerSize);
        metaModel.put(Container.SIZE, new MethodTuple(MetaModelUtils.ATOMIC_INTEGER_GET_METHOD, numberOfEntries));
        metaModel.put(Container.AUTH_LEVEL, auth.toString());
        metaModel.put(Container.ENTRIES, new MethodTuple(GET_ENTRY_SET_METHOD, this));
        metaModel.put(Container.OBLIGATORY_COORDINATOR_ARGS, obligatoryCoordinatorArgs);
        metaModel.put(Container.OBLIGATORY_COORDINATORS, this.obligatoryCoordinators);
        metaModel.put(Container.OPTIONAL_COORDINATOR_ARGS, optionalCoordinatorArgs);
        metaModel.put(Container.OPTIONAL_COORDINATORS, this.optionalCoordinators);

        Map<String, Object> counters = new HashMap<String, Object>();
        counters.put(Counters.WRITE_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD, writeOpCounter));
        counters.put(Counters.SUCCESSFUL_WRITE_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                successfulWriteOpCounter));
        counters.put(Counters.READ_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD, readOpCounter));
        counters.put(Counters.SUCCESSFUL_READ_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                successfulReadOpCounter));
        counters.put(Counters.TAKE_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD, takeOpCounter));
        counters.put(Counters.SUCCESSFUL_TAKE_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                successfulTakeOpCounter));
        metaModel.put(Container.COUNTERS, counters);
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<NativeEntry> getEntrySet() {
        return new HashSet<NativeEntry>(entrySet);
    }

    @Override
    public LocalContainerReference getReference() {
        return this.containerReference;
    }

    /**
     * Checks if the containerName is valid.
     *
     * @param containerName
     *            the ContainerName to be checked
     * @return true if valid, false otherwise
     */
    // TODO remove method, check in API?
    static boolean validContainerName(final String containerName) {
        if (containerName == null) {
            return true;
        }
        return !AndroidHelperUtils.isEmpty(containerName);
    }

    private void performCommonChecks(final IsolationLevel isolationLevel, final NativeSubTransaction stx)
            throws Capi3Exception {

        if (isolationLevel == null) {
            throw new NullPointerException("Isolation level");
        }
        if (stx == null) {
            throw new NullPointerException("Sub-transaction");
        }

        /* Check if Container is valid */
        if (!this.isValid()) {
            throw new InvalidContainerException();
        }

        /* Check SubTransaction Status */
        if (!stx.getStatus().equals(TransactionStatus.RUNNING)) {
            throw new InvalidSubTransactionException(stx.getId());
        }

    }

    private EntryOperationResult performSelectingEntryOperation(final OperationType opType,
            final List<NativeSelector<?>> selectors, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final RequestContext context) {

        if (selectors == null) {
            throw new NullPointerException("Selector list");
        }
        try {
            performCommonChecks(isolationLevel, stx);
        } catch (Capi3Exception ex) {
            return new DefaultEntryOperationResult(OperationStatus.NOTOK, ex, null);
        }

        if (selectors.isEmpty()) {
            throw new IllegalArgumentException("Empty selector list");
        }

        Availability avi = this.isolationManager.checkContainerAvailability(this.getReference(), isolationLevel, stx,
                opType);
        if (!avi.getType().equals(AvailabilityType.AVAILABLE)) {
            log.debug("Container not available ({}), returning with LOCKED", avi);
            // TODO use result object that is not an exception
            return new DefaultEntryOperationResult(OperationStatus.LOCKED,
                    LockedExceptionsHelper.newContainerLockedException(avi), null);
        }

        NativeSelector<?> lastSelector;
        try {
            lastSelector = this.linkSelectors(selectors);
        } catch (CoordinatorNotRegisteredException ex) {
            return new DefaultEntryOperationResult(OperationStatus.NOTOK, ex, null);
        }

        AuthorizationResult authResult = null;
        if (this.accessManager != null) {
            authResult = this.accessManager.checkPermissions(this, opType, stx, context);
        }

        try {
            List<NativeEntry> resultingEntries = lastSelector.getAll(isolationLevel, authResult, stx, opType, context);
            ArrayList<Serializable> data = new ArrayList<Serializable>();
            for (NativeEntry entry : resultingEntries) {
                LazyNativeEntry lazyNativeEntry = new LazyNativeEntry(entry);
                LockResult lockResult = this.isolationManager.accquireEntryLock(opType, lazyNativeEntry,
                        getReference(), isolationLevel, stx, context, this);
                if (!lockResult.isValid()) {
                    return new DefaultEntryOperationResult(OperationStatus.LOCKED,
                            LockedExceptionsHelper.newEntryLockedException(lockResult), null);
                }
                if (this.accessManager != null
                        && authResult.checkEntryAuthorization(entry) != AuthorizationType.PERMITTED) {
                    log.debug("Access denied to selected entry.");
                    // TODO use own DENIED operation status, include cause in exception
                    return new DefaultEntryOperationResult(OperationStatus.NOTOK, new AccessDeniedException(), null);
                }
                if (opType == OperationType.TAKE) {
                    // remove entries
                    for (NativeCoordinator c : this.optionalCoordinators.values()) {
                        try {
                            c.prepareEntryRemoval(stx, entry, context);
                        } catch (CoordinatorLockedException e) {
                            return new DefaultEntryOperationResult(OperationStatus.LOCKED, e, null);
                        }
                    }
                }
                data.add(entry.getData());
            }
            return new DefaultEntryOperationResult(OperationStatus.OK, null, data);
        } catch (EntryLockedException e1) {
            return new DefaultEntryOperationResult(OperationStatus.LOCKED, e1, null);
        } catch (CountNotMetException e2) {
            return new DefaultEntryOperationResult(OperationStatus.DELAYABLE, e2, null);
        } catch (AccessDeniedException e3) {
            // TODO use own DENIED operation result, include cause in exception
            return new DefaultEntryOperationResult(OperationStatus.NOTOK, e3, null);
        } catch (InvalidEntryException e4) {
            return new DefaultEntryOperationResult(OperationStatus.LOCKED, e4, null);
        }
    }

    @Override
    public synchronized EntryOperationResult executeReadOperation(final List<NativeSelector<?>> selectors,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx, final RequestContext context) {
        readOpCounter.incrementAndGet();
        EntryOperationResult result = performSelectingEntryOperation(OperationType.READ, selectors, isolationLevel,
                stx, context);
        if (result.getStatus() != OperationStatus.OK) {
            // dummy log item to get container with DefaultTransaction.getAccessContainer (fix for bug #74)
            stx.addLog(new DefaultReadLogItem(null, containerReference, isolationManager, stx));
        } else {
            successfulReadOpCounter.incrementAndGet();
        }
        return result;
    }

    @Override
    public synchronized EntryOperationResult executeTakeOperation(final List<NativeSelector<?>> selectors,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx, final RequestContext context) {
        takeOpCounter.incrementAndGet();
        EntryOperationResult result = performSelectingEntryOperation(OperationType.TAKE, selectors, isolationLevel,
                stx, context);
        if (result.getStatus() != OperationStatus.OK) {
            // dummy log item to get container with DefaultTransaction.getAccessContainer (fix for bug #74)
            stx.addLog(new DefaultTakeLogItem(null, containerReference, isolationManager, stx, context, this));
        } else {
            successfulTakeOpCounter.incrementAndGet();
        }
        return result;
    }

    @Override
    public synchronized OperationResult executeWriteOperation(final Entry entry, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final RequestContext context) {

        writeOpCounter.incrementAndGet();

        if (entry == null) {
            throw new NullPointerException("Entry");
        }

        try {
            performCommonChecks(isolationLevel, stx);
        } catch (Capi3Exception ex) {
            return new DefaultOperationResult(OperationStatus.NOTOK, ex);
        }

        Availability avi = this.isolationManager.checkContainerAvailability(this.getReference(), isolationLevel, stx,
                OperationType.WRITE);
        if (!avi.getType().equals(AvailabilityType.AVAILABLE)) {
            /* Container locked */
            // dummy log item to get container with DefaultTransaction.getAccessContainer (fix for bug #74)
            stx.addLog(new DefaultWriteLogItem(null, containerReference, isolationManager, stx, context, this));
            // TODO use result object that is not an exception
            return new DefaultOperationResult(OperationStatus.LOCKED,
                    LockedExceptionsHelper.newContainerLockedException(avi));
        }

        // check if obligatory coordinators are satisfied
        List<CoordinationData> coordinationData = new ArrayList<CoordinationData>(entry.getCoordinationData());
        for (NativeCoordinator coord : this.obligatoryCoordinators.values()) {
            boolean found = false;
            for (CoordinationData coordData : coordinationData) {
                if (coord.getName().equals(coordData.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (coord instanceof ImplicitNativeCoordinator) {
                    coordinationData.add(((ImplicitNativeCoordinator) coord).createDefaultCoordinationData());
                } else {
                    return new DefaultOperationResult(OperationStatus.NOTOK, new ObligatoryCoordinatorMissingException(
                            coord.getName()));
                }
            }
        }

        NativeEntry nativeEntry = new DefaultEntry(this, entry.getValue(), ENTRY_ID_COUNTER.incrementAndGet());
        NativeEntry lazyNativeEntry = persistenceContext.makeEntryLazy(nativeEntry);
        try {
            this.isolationManager.accquireEntryLock(OperationType.WRITE, lazyNativeEntry, getReference(),
                    isolationLevel, stx, context, this);
        } catch (InvalidEntryException e) {
            /* Not possible to reach this state */
            throw new IllegalStateException(e);
        }

        if (numberOfEntries.incrementAndGet() > containerSize) {
            numberOfEntries.decrementAndGet();
            // TODO use result object that is not an exception
            return new DefaultOperationResult(OperationStatus.DELAYABLE, new ContainerFullException());
        }

        // write entry to coordinators
        for (CoordinationData coordData : coordinationData) {
            NativeCoordinator coord = this.optionalCoordinators.get(coordData.getName());
            if (coord == null) {
                numberOfEntries.decrementAndGet();
                return new DefaultOperationResult(OperationStatus.NOTOK, new CoordinatorNotRegisteredException(
                        coordData.getName()));
            } else {
                try {
                    coord.registerEntry(stx, coordData, lazyNativeEntry, context);
                    // TODO refactor, mix of (unused) return value and exceptions
                    // TODO unregister entry from coordinators where it has already been added?
                    // use ListIterator for reverse iterating?
                    // TODO remove entry lock acquired above
                } catch (DuplicateKeyException ex) {
                    // TODO refactor, coordinator-specific exception should not be used here
                    numberOfEntries.decrementAndGet();
                    return new DefaultOperationResult(OperationStatus.DELAYABLE, ex);
                } catch (CoordinatorLockedException ex) {
                    numberOfEntries.decrementAndGet();
                    return new DefaultOperationResult(OperationStatus.LOCKED, ex);
                } catch (Capi3Exception ex) {
                    numberOfEntries.decrementAndGet();
                    return new DefaultOperationResult(OperationStatus.NOTOK, ex);
                }
            }
        }

        if (this.accessManager != null) {
            AuthorizationResult authResult = this.accessManager.checkPermissions(this, OperationType.WRITE, stx,
                    context);
            if (authResult.checkEntryAuthorization(nativeEntry) != AuthorizationType.PERMITTED) {
                log.debug("Access denied to written entry.");
                numberOfEntries.decrementAndGet();
                // TODO use own DENIED operation status, include cause in exception
                return new DefaultOperationResult(OperationStatus.NOTOK, new AccessDeniedException());
            }
        }

        storedEntries.put(nativeEntry.getEntryId(), nativeEntry, stx.getParent());
        entrySet.add(lazyNativeEntry);

        successfulWriteOpCounter.incrementAndGet();
        return new DefaultOperationResult(OperationStatus.OK, null);
    }

    @Override
    public synchronized void purgeEntry(final NativeEntry entry, final RequestContext context,
            final NativeSubTransaction stx) {
        if (entry == null) {
            throw new NullPointerException("Entry");
        }
        boolean decrementCount = false;

        /**
         * It is necessary to removed the entry from the coordinators because otherwise a call to remove it from a
         * StoredMap inside of the unregisterEntry method of a coordinator could lead to an exception in
         * LazyNativeEntry#getWrapped when persistence is enabled (see bug #100).
         */
        for (NativeCoordinator coord : this.optionalCoordinators.values()) {
            if (coord.unregisterEntry(entry, context, stx)) {
                decrementCount = true;
            }
        }
        this.storedEntries.remove(entry.getEntryId(), stx.getParent());
        this.entrySet.remove(entry);

        if (decrementCount) {
            numberOfEntries.decrementAndGet();
        }
    }

    /**
     * This function correctly links the selectors to the respective coordinators.
     *
     * @param selectors
     *            the selectors to be linked to the coordinators
     * @return the last selector in the chain
     * @throws CoordinatorNotRegisteredException
     *             when a selector has no associated coordinator
     */
    @SuppressWarnings("unchecked")
    private NativeSelector<?> linkSelectors(final List<? extends Selector> selectors)
            throws CoordinatorNotRegisteredException {
        NativeSelector<NativeCoordinator> selector = null;
        NativeSelector<NativeCoordinator> prev = null;
        for (Selector s : selectors) {
            NativeCoordinator coord = this.optionalCoordinators.get(s.getName());
            if (coord == null) {
                throw new CoordinatorNotRegisteredException(s.getName());
            }
            selector = ((NativeSelector<NativeCoordinator>) s);
            selector.link(coord, this.isolationManager);
            selector.setPredecessor(prev);
            prev = selector;
        }
        log.debug("Selector list: {}", selectors);
        return selector;
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof NativeContainer) && (getId() == ((NativeContainer) obj).getId());
    }

    @Override
    public int hashCode() {
        return (int) (containerId ^ (containerId >>> 32));
    }

    @Override
    public void dispose() {
        this.validContainer = false;
        // delete all entry locks
        for (NativeEntry entry : entrySet) {
            this.isolationManager.purgeEntryLock(entry);
        }
        storedEntries.destroy();
        close();
        Set<NativeCoordinator> coordinators = new HashSet<NativeCoordinator>(obligatoryCoordinators.values());
        coordinators.addAll(optionalCoordinators.values());
        for (NativeCoordinator coordinator : coordinators) {
            if (coordinator instanceof PersistentCoordinator) {
                ((PersistentCoordinator) coordinator).destroy();
            }
        }
    }

    @Override
    public boolean isValid() {
        return this.validContainer;
    }

    @Override
    public String getName() {
        return this.containerName;
    }

    @Override
    public long getId() {
        return this.containerId;
    }

    @Override
    public String getIdAsString() {
        return Long.toString(this.containerId);
    }

    // meta model methods below
    @Override
    public Object navigate(final String path) {
        Object metaModelNode = MetaModelUtils.navigate(path, this, metaModel);
        if (metaModelNode instanceof NativeCoordinator) {
            @SuppressWarnings("resource")
            NativeCoordinator coord = (NativeCoordinator) metaModelNode;
            if (!(coord instanceof MetaDataProvider)) {
                // quite a hack
                log.debug("Returning generic meta data for coordinator {}", coord.getName());
                Map<String, Object> metaData = getGenericCoordinatorMetaData(coord);
                // path has format [obligatory|optional]Coords/<name>/<remainingPath>
                String[] matches = path.split(MetaModelKeys.PATH_DELIMITER, 3);
                // navigate further, if remaining path exists
                if (matches.length == 3) {
                    return MetaModelUtils.navigate(matches[2], metaData, metaData);
                } else {
                    return metaData;
                }
            }
        }
        return metaModelNode;
    }

    private static Map<String, Object> getGenericCoordinatorMetaData(final NativeCoordinator coord) {
        Map<String, Object> metaData = new HashMap<String, Object>();
        metaData.put(MetaModelKeys.Containers.Container.Coordinators.NAME, coord.getName());
        metaData.put(MetaModelKeys.Containers.Container.Coordinators.CLASS, coord.getClass().getName());
        return metaData;
    }

    @Override
    public Object getMetaData(final int depth) {
        return MetaModelUtils.getData(depth, metaModel);
    }

    @Override
    public void setMetaDataProperty(final String key, final Object value) {
        throw new UnsupportedOperationException("Properties cannot be set here");
    }

    @Override
    public NativeEntry getEntry(final long id) {
        return storedEntries.get(id);
    }

    @Override
    public PersistentContainerDescriptor getPersistentContainerDescriptor() {
        return persistentContainerDescriptor;
    }

    @Override
    public void restoreContent(final PersistenceContext persistenceContext, final NativeSubTransaction stx) {
        IsolationLevel isolationLevel = IsolationLevel.READ_COMMITTED;
        RequestContext context = new RequestContext();
        if (!entrySet.isEmpty()) {
            log.debug("Restoring {} entries in container {}", entrySet.size(), containerId);
        }
        for (NativeEntry nativeEntry : entrySet) {
            try {
                this.isolationManager.accquireEntryLock(OperationType.WRITE, nativeEntry, getReference(),
                        isolationLevel, stx, context, this);
            } catch (InvalidEntryException e) {
                /* Not possible to reach this state */
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public List<NativeEntry> selectEntries(final List<NativeSelector<?>> selectors, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final RequestContext context) throws Capi3Exception {
        if (selectors == null) {
            throw new NullPointerException("Selector list");
        }
        try {
            performCommonChecks(isolationLevel, stx);
        } catch (Capi3Exception ex) {
            log.warn("Invalid transaction while selecting entries for access control process");
            throw ex;
        }

        if (selectors.isEmpty()) {
            throw new IllegalArgumentException("Empty selector list");
        }

        Availability avi = this.isolationManager.checkContainerAvailability(this.getReference(), isolationLevel, stx,
                OperationType.READ);
        if (!avi.getType().equals(AvailabilityType.AVAILABLE)) {
            /* Container locked */
            log.warn("Locked container while selecting entries for access control process");
            throw LockedExceptionsHelper.newContainerLockedException(avi);
        }

        NativeSelector<?> lastSelector;
        try {
            lastSelector = this.linkSelectors(selectors);
        } catch (CoordinatorNotRegisteredException ex) {
            log.warn("Unregistered coordinator while selecting entries for access control process");
            throw ex;
        }

        try {
            List<NativeEntry> resultingEntries = lastSelector.getAll(isolationLevel, null, stx, OperationType.READ,
                    context);
            for (NativeEntry entry : resultingEntries) {
                LockResult lockResult = this.isolationManager.accquireEntryLock(OperationType.READ, entry,
                        getReference(), isolationLevel, stx, context, this);
                if (!lockResult.isValid()) {
                    log.warn("Locked entry while selecting entries for access control process");
                    throw LockedExceptionsHelper.newEntryLockedException(lockResult);
                }
            }
            return resultingEntries;
        } catch (EntryLockedException e1) {
            log.warn("Locked entry while selecting entries for access control process");
            throw e1;
        } catch (CountNotMetException e2) {
            log.warn("Insufficient entry count while selecting entries for access control process");
            throw e2;
        } catch (InvalidEntryException e3) {
            log.warn("Invalid entry while selecting entries for access control process");
            throw e3;
        }
    }

    @Override
    public void close() throws MzsCoreRuntimeException {
        Set<NativeCoordinator> coordinators = new HashSet<NativeCoordinator>(obligatoryCoordinators.values());
        coordinators.addAll(optionalCoordinators.values());
        for (NativeCoordinator coord : coordinators) {
            coord.close();
        }
    }

}
