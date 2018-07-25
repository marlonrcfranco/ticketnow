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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.mozartspaces.capi3.AuthTargetCoordinator;
import org.mozartspaces.capi3.ContainerNameNotAvailableException;
import org.mozartspaces.capi3.ContainerNotFoundException;
import org.mozartspaces.capi3.ContainerOperationResult;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.DuplicateCoordinatorException;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidContainerNameException;
import org.mozartspaces.capi3.InvalidSubTransactionException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.OperationType;
import org.mozartspaces.capi3.TransactionStatus;
import org.mozartspaces.capi3.javanative.LockedExceptionsHelper;
import org.mozartspaces.capi3.javanative.authorization.NativeAccessManager;
import org.mozartspaces.capi3.javanative.coordination.DefaultAuthTargetCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultLabelCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.isolation.Availability.AvailabilityType;
import org.mozartspaces.capi3.javanative.isolation.DefaultTransaction;
import org.mozartspaces.capi3.javanative.isolation.LockResult;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.isolation.NativeTransaction;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.capi3.javanative.persistence.PersistenceException;
import org.mozartspaces.capi3.javanative.persistence.PersistentCoordinator;
import org.mozartspaces.capi3.javanative.persistence.StoredMap;
import org.mozartspaces.capi3.javanative.persistence.key.LongPersistenceKey;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Container;
import org.mozartspaces.core.metamodel.MetaModelKeys.Containers.Counters;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The CAPI3 ContainerManager administrates all CAPI3 Containers and executes all direct ContainerOperations
 * (ContainerCreate, ContainerDelete, ContainerLookup).
 *
 * @author Martin Barisits
 * @author Stefan Crass
 * @author Jan Zarnikov
 */
public final class DefaultContainerManager implements NativeContainerManager {

    private static Logger log = LoggerFactory.get();

    private final AtomicLong containerIdCounter;
    private final ConcurrentHashMap<String, NativeContainer> containers;
    private final ConcurrentHashMap<String, NativeContainer> containerNames;

    private final NativeIsolationManager isolationManager;
    private final NativeAccessManager accessManager;

    private final AtomicLong createContainerOperationCounter;
    private final AtomicLong successfulCreateContainerOperationCounter;
    private final AtomicLong destroyContainerOperationCounter;
    private final AtomicLong successfulDestroyContainerOperationCounter;
    private final AtomicLong lockContainerOperationCounter;
    private final AtomicLong successfulLockContainerOperationCounter;
    private final AtomicLong lookupContainerOperationCounter;
    private final AtomicLong successfulLookupContainerOperationCounter;

    private final PersistenceContext configuredPersistenceContext;
    private final PersistenceContext inMemoryPersistenceContext;

    private final Map<String, Object> metaModel;

    private StoredMap<Long, PersistentContainerDescriptor> containerDescriptors;

    /**
     * Creates a DefaultContainerManager.
     *
     * @param isolationManager
     *            the isolation manager
     * @param accessManager
     *            the access manager
     * @param configuredPersistenceContext
     *            the configured persistence context
     * @param inMemoryPersistenceContext
     *            the in-memory persistence context (for in-memory containers)
     */
    public DefaultContainerManager(final NativeIsolationManager isolationManager,
            final NativeAccessManager accessManager, final PersistenceContext configuredPersistenceContext,
            final PersistenceContext inMemoryPersistenceContext) {
        assert isolationManager != null : "IsolationManager must be injected";
        // access manager is optional
        assert configuredPersistenceContext != null : "configuredPersistenceContext must be injected";
        assert inMemoryPersistenceContext != null : "inMemoryPersistenceContext must be injected";

        this.containerIdCounter = new AtomicLong();
        this.containers = new ConcurrentHashMap<String, NativeContainer>();
        this.containerNames = new ConcurrentHashMap<String, NativeContainer>();
        this.isolationManager = isolationManager;
        this.accessManager = accessManager;
        this.configuredPersistenceContext = configuredPersistenceContext;
        this.inMemoryPersistenceContext = inMemoryPersistenceContext;

        createContainerOperationCounter = new AtomicLong();
        successfulCreateContainerOperationCounter = new AtomicLong();
        destroyContainerOperationCounter = new AtomicLong();
        successfulDestroyContainerOperationCounter = new AtomicLong();
        lockContainerOperationCounter = new AtomicLong();
        successfulLockContainerOperationCounter = new AtomicLong();
        lookupContainerOperationCounter = new AtomicLong();
        successfulLookupContainerOperationCounter = new AtomicLong();

        metaModel = new HashMap<String, Object>();
        metaModel.put(Containers.CONTAINER, containers);
        Method namesSetMethod = null;
        try {
            namesSetMethod = getClass().getDeclaredMethod("getContainerNamesSet", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        metaModel.put(Containers.NAMES, new MethodTuple(namesSetMethod, this));
        metaModel.put(Containers.COUNT, new MethodTuple(MetaModelUtils.CONCURRENT_HASH_MAP_SIZE_METHOD, containers));

        Map<String, Object> counters = new HashMap<String, Object>();
        counters.put(Counters.CREATE_CONTAINER_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                createContainerOperationCounter));
        counters.put(Counters.SUCCESSFUL_CREATE_CONTAINER_OP_COUNT, new MethodTuple(
                MetaModelUtils.ATOMIC_LONG_GET_METHOD, successfulCreateContainerOperationCounter));
        counters.put(Counters.DESTROY_CONTAINER_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                destroyContainerOperationCounter));
        counters.put(Counters.SUCCESSFUL_DESTROY_CONTAINER_OP_COUNT, new MethodTuple(
                MetaModelUtils.ATOMIC_LONG_GET_METHOD, successfulDestroyContainerOperationCounter));
        counters.put(Counters.LOCK_CONTAINER_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                lockContainerOperationCounter));
        counters.put(Counters.SUCCESSFUL_LOCK_CONTAINER_OP_COUNT, new MethodTuple(
                MetaModelUtils.ATOMIC_LONG_GET_METHOD, successfulLockContainerOperationCounter));
        counters.put(Counters.LOOKUP_CONTAINER_OP_COUNT, new MethodTuple(MetaModelUtils.ATOMIC_LONG_GET_METHOD,
                lookupContainerOperationCounter));
        counters.put(Counters.SUCCESSFUL_LOOKUP_CONTAINER_OP_COUNT, new MethodTuple(
                MetaModelUtils.ATOMIC_LONG_GET_METHOD, successfulLookupContainerOperationCounter));
        metaModel.put(Container.COUNTERS, counters);
    }

    /**
     * Initializes the policy container.
     */
    public void initPolicyContainer() {
        if (accessManager == null) {
            return;
        }
        NativeContainer policyC = containerNames.get(MzsConstants.POLICY_CONTAINER_NAME);
        if (policyC == null) {
            // internally create the policy container
            policyC = createPolicyContainer();
        }
        log.debug("Setting container {} as policy container for access manager", policyC.getId());
        this.accessManager.setPolicyContainer(policyC);
    }

    private NativeContainer createPolicyContainer() {
        NativeContainer container = null;
        // TODO refine policy container coordination
        List<NativeCoordinator> obligatoryCoordinators = new ArrayList<NativeCoordinator>();
        List<Coordinator> obligatoryCoordinatorArgs = new ArrayList<Coordinator>();

        obligatoryCoordinators.add(new DefaultAuthTargetCoordinator(AuthTargetCoordinator.DEFAULT_NAME));
        obligatoryCoordinatorArgs.add(new AuthTargetCoordinator(AuthTargetCoordinator.DEFAULT_NAME));

        // access via unique id
        obligatoryCoordinators.add(new DefaultLabelCoordinator(KeyCoordinator.DEFAULT_NAME, true));
        obligatoryCoordinatorArgs.add(new KeyCoordinator(KeyCoordinator.DEFAULT_NAME));

        List<NativeCoordinator> optionalCoordinators = new ArrayList<NativeCoordinator>();
        List<Coordinator> optionalCoordinatorArgs = new ArrayList<Coordinator>();
        try {
            // TODO make configurable whether policy container is persistent
            PersistenceContext containerPersistenceContext = configuredPersistenceContext;
            container = new DefaultContainer(containerIdCounter.incrementAndGet(), isolationManager,
                    AuthorizationLevel.SECURE, accessManager, containerPersistenceContext,
                    MzsConstants.POLICY_CONTAINER_NAME, MzsConstants.Container.UNBOUNDED, obligatoryCoordinatorArgs,
                    obligatoryCoordinators, optionalCoordinatorArgs, optionalCoordinators);
            NativeTransaction tx = new DefaultTransaction(0);
            tx.addLog(containerPersistenceContext.createPersistentTransaction(tx));
            NativeSubTransaction stx = (NativeSubTransaction) tx.newSubTransaction();
            for (NativeCoordinator coordinator : obligatoryCoordinators) {
                coordinator.init(container, stx, null);
                if (coordinator instanceof PersistentCoordinator) {
                    ((PersistentCoordinator) coordinator).initPersistence(container, containerPersistenceContext);
                }
            }
            for (NativeCoordinator coordinator : optionalCoordinators) {
                coordinator.init(container, stx, null);
                if (coordinator instanceof PersistentCoordinator) {
                    ((PersistentCoordinator) coordinator).initPersistence(container, containerPersistenceContext);
                }
            }
            if (!this.isolationManager.accquireContainerLock(OperationType.CREATECONTAINER, container.getReference(),
                    IsolationLevel.READ_COMMITTED, stx, this).isValid()) {
                throw new IllegalStateException(
                        "It should not be possible for a newly created container to fail acquire the INSERT Lock");
            }
            if (!(this.containerNames.putIfAbsent(MzsConstants.POLICY_CONTAINER_NAME, container) == null)) {
                throw new RuntimeException("Policy Container must not already exist");
            }
            if (!(this.containers.putIfAbsent(container.getIdAsString(), container) == null)) {
                throw new RuntimeException("Duplicate ContainerId was generated, this should not be possible");
            }
            PersistentContainerDescriptor descriptor = container.getPersistentContainerDescriptor();
            containerDescriptors.put(descriptor.getId(), descriptor, stx.getParent());
            log.debug("Created policy container with ID {}", descriptor.getId());
            stx.commit();
            tx.commit();
        } catch (Exception ex) {
            throw new IllegalStateException("Creating policy container failed", ex);
        }
        return container;
    }

    // only for meta model
    @SuppressWarnings("unused")
    private Set<String> getContainerNamesSet() {
        return new HashSet<String>(containerNames.keySet());
    }

    @Override
    public void restoreContainers(final NativeSubTransaction stx) throws PersistenceException,
            InvalidContainerNameException, DuplicateCoordinatorException {
        final String containerDescriptorsStoredMapName = configuredPersistenceContext.generateStoredMapName(getClass(),
                "0");
        containerDescriptors = configuredPersistenceContext.createStoredMap(containerDescriptorsStoredMapName,
                new LongPersistenceKey.LongPersistenceKeyFactory());

        int persistentContainerCount = containerDescriptors.size();
        if (persistentContainerCount == 0) {
            log.debug("Restoring {} persistent container(s)", persistentContainerCount);
        } else {
            log.info("Restoring {} persistent container(s)", persistentContainerCount);
        }
        // store max ID; used to restore the ID counter
        long maxContainerId = 0;
        // for each container
        for (Long key : containerDescriptors.keySet()) {
            PersistentContainerDescriptor descriptor = containerDescriptors.get(key);
            log.info("Restoring persistent container {} (named '{}')", key, descriptor.getName());
            List<PersistentCoordinator> persistentCoordinators = new ArrayList<PersistentCoordinator>();
            List<NativeCoordinator> obligatoryCoordinators = new ArrayList<NativeCoordinator>();

            for (Class<? extends NativeCoordinator> coordinatorClass : descriptor.getObligatoryCoordinators()) {
                NativeCoordinator coordinator = getCoordinator(coordinatorClass);
                obligatoryCoordinators.add(coordinator);
            }
            for (PersistentCoordinator.CoordinatorRestoreTask restoreTask : descriptor
                    .getPersistentObligatoryCoordinators()) {
                try {
                    PersistentCoordinator persistentCoordinator = restoreTask.restoreCoordinator();
                    persistentCoordinator.preRestoreContent(configuredPersistenceContext);
                    obligatoryCoordinators.add(persistentCoordinator);
                    persistentCoordinators.add(persistentCoordinator);
                } catch (PersistenceException ex) {
                    throw new AssertionError();
                }
            }

            List<NativeCoordinator> optionalCoordinators = new ArrayList<NativeCoordinator>();
            for (Class<? extends NativeCoordinator> coordinatorClass : descriptor.getOptionalCoordinators()) {
                NativeCoordinator coordinator = getCoordinator(coordinatorClass);
                optionalCoordinators.add(coordinator);
            }
            for (PersistentCoordinator.CoordinatorRestoreTask restoreTask : descriptor
                    .getPersistentOptionalCoordinators()) {
                try {
                    PersistentCoordinator persistentCoordinator = restoreTask.restoreCoordinator();
                    persistentCoordinator.preRestoreContent(configuredPersistenceContext);
                    optionalCoordinators.add(persistentCoordinator);
                    persistentCoordinators.add(persistentCoordinator);
                } catch (PersistenceException ex) {
                    throw new AssertionError();
                }
            }
            String name = descriptor.getName();
            long id = descriptor.getId();
            if (id > maxContainerId) {
                maxContainerId = id;
            }
            AuthorizationLevel auth = descriptor.getAuth();
            NativeAccessManager containerAccessManager = checkAuthAndGetAccessManager(auth);
            DefaultContainer container = new DefaultContainer(id, isolationManager, auth, containerAccessManager,
                    configuredPersistenceContext, name, descriptor.getSize(),
                    descriptor.getObligatoryCoordinatorArgs(), obligatoryCoordinators,
                    descriptor.getOptionalCoordinatorArgs(), optionalCoordinators);

            try {
                if (!this.isolationManager.accquireContainerLock(OperationType.CREATECONTAINER,
                        container.getReference(), IsolationLevel.READ_COMMITTED, stx, this).isValid()) {
                    container.close();
                    throw new IllegalStateException(
                            "It should not be possible for a newly created container to fail acquire the INSERT Lock");
                }
            } catch (InvalidContainerException e) {
                container.close();
                throw new IllegalStateException(
                        "It should not be possible for a newly created container to fail acquire the INSERT Lock");
            }
            container.restoreContent(configuredPersistenceContext, stx);
            if (name != null) {
                containerNames.put(name, container);
            }
            containers.put(container.getIdAsString(), container);
            for (PersistentCoordinator persistentCoordinator : persistentCoordinators) {
                persistentCoordinator.postRestoreContent(configuredPersistenceContext, container, stx);
            }
        }
        containerIdCounter.set(maxContainerId);
    }

    private NativeCoordinator getCoordinator(final Class<? extends NativeCoordinator> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ContainerOperationResult createContainer(final String containerName,
            final List<? extends Coordinator> obligatoryCoordinatorArgs,
            final List<NativeCoordinator> obligatoryCoordinators,
            final List<? extends Coordinator> optionalCoordinatorArgs,
            final List<NativeCoordinator> optionalCoordinators, final int size, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final AuthorizationLevel auth, final boolean forceInMemory,
            final RequestContext context) {

        createContainerOperationCounter.incrementAndGet();

        if (obligatoryCoordinators == null) {
            throw new NullPointerException("Obligatory coordinator list");
        }
        if (optionalCoordinators == null) {
            throw new NullPointerException("Optional coordinator list");
        }
        if (optionalCoordinators.isEmpty() && obligatoryCoordinators.isEmpty()) {
            throw new IllegalArgumentException("At least one Coordinator must be registered at the Container");
        }

        if (isolationLevel == null) {
            throw new NullPointerException("Isolation level");
        }
        if (stx == null) {
            throw new NullPointerException("Sub-transaction");
        }

        /* Check SubTransaction Status */
        if (!stx.getStatus().equals(TransactionStatus.RUNNING)) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new InvalidSubTransactionException(
                    stx.getId()), null);
        }

        PersistenceContext containerPersistenceContext = forceInMemory ? inMemoryPersistenceContext
                : configuredPersistenceContext;
        NativeAccessManager containerAccessManager = checkAuthAndGetAccessManager(auth);

        NativeContainer container;
        try {
            container = new DefaultContainer(containerIdCounter.incrementAndGet(), isolationManager, auth,
                    containerAccessManager, containerPersistenceContext, containerName, size,
                    obligatoryCoordinatorArgs, obligatoryCoordinators, optionalCoordinatorArgs, optionalCoordinators);

            for (NativeCoordinator coordinator : obligatoryCoordinators) {
                coordinator.init(container, stx, context);
                if (coordinator instanceof PersistentCoordinator) {
                    ((PersistentCoordinator) coordinator).initPersistence(container, containerPersistenceContext);
                }
            }
            for (NativeCoordinator coordinator : optionalCoordinators) {
                coordinator.init(container, stx, context);
                if (coordinator instanceof PersistentCoordinator) {
                    ((PersistentCoordinator) coordinator).initPersistence(container, containerPersistenceContext);
                }
            }
        } catch (InvalidContainerNameException e) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, e, null);
        } catch (DuplicateCoordinatorException e) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, e, null);
        } catch (PersistenceException e) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, e, null);
        }
        try {
            if (!this.isolationManager.accquireContainerLock(OperationType.CREATECONTAINER, container.getReference(),
                    isolationLevel, stx, this).isValid()) {
                throw new IllegalStateException(
                        "It should not be possible for a newly created container to fail acquire the INSERT Lock");
            }
        } catch (InvalidContainerException e) {
            throw new IllegalStateException(
                    "It should not be possible for a newly created container to fail acquire the INSERT Lock");
        }
        if (containerName != null) {
            // named container
            if (!(this.containerNames.putIfAbsent(containerName, container) == null)) {
                return new DefaultContainerOperationResult(OperationStatus.NOTOK,
                        new ContainerNameNotAvailableException(containerName), null);
            }
        }
        if (!(this.containers.putIfAbsent(container.getIdAsString(), container) == null)) {
            throw new RuntimeException("Duplicate ContainerId was generated, this should not be possible");
        }

        successfulCreateContainerOperationCounter.incrementAndGet();

        if (!forceInMemory) {
            PersistentContainerDescriptor descriptor = container.getPersistentContainerDescriptor();
            containerDescriptors.put(descriptor.getId(), descriptor, stx.getParent());
        }

        return new DefaultContainerOperationResult(OperationStatus.OK, null, container.getReference());

    }

    private NativeAccessManager checkAuthAndGetAccessManager(final AuthorizationLevel auth) {
        switch (auth) {
        case NONE:
            return null;
        case SECURE:
            if (this.accessManager == null) {
                throw new IllegalArgumentException("Secure container cannot be created without AccessManager");
            }
            return this.accessManager;
        default:
            throw new IllegalArgumentException("Invalid authorization level");
        }
    }

    @Override
    public ContainerOperationResult destroyContainer(final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx, final RequestContext context) {

        destroyContainerOperationCounter.incrementAndGet();

        if (cRef == null) {
            throw new NullPointerException("The ContainerReference must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The IsolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        // check sub-transaction status
        if (!stx.getStatus().equals(TransactionStatus.RUNNING)) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new InvalidSubTransactionException(
                    stx.getId()), null);
        }

        LockResult lockResult;
        try {
            lockResult = this.isolationManager.accquireContainerLock(OperationType.DESTROYCONTAINER, cRef,
                    isolationLevel, stx, this);
        } catch (InvalidContainerException ex) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, ex, cRef);
        }
        if (!lockResult.isValid()) {
            return new DefaultContainerOperationResult(OperationStatus.LOCKED,
                    LockedExceptionsHelper.newContainerLockedException(lockResult), cRef);
        }
        NativeContainer container;
        try {
            container = this.getContainer(cRef);
        } catch (InvalidContainerException ex) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, ex, cRef);
        }
        if (!container.isValid()) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new InvalidContainerException(), cRef);
        }
        containerDescriptors.remove(container.getId(), stx.getParent());
        successfulDestroyContainerOperationCounter.incrementAndGet();
        return new DefaultContainerOperationResult(OperationStatus.OK, null, cRef);
    }

    @Override
    public ContainerOperationResult lookupContainer(final String containerName, final IsolationLevel isolationLevel,
            final NativeSubTransaction stx, final RequestContext context) {

        lookupContainerOperationCounter.incrementAndGet();

        if (containerName == null) {
            throw new NullPointerException("The containerName must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The isolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        /* Check SubTransaction Status */
        if (!stx.getStatus().equals(TransactionStatus.RUNNING)) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new InvalidSubTransactionException(
                    stx.getId()), null);
        }

        NativeContainer container = this.containerNames.get(containerName);
        if (container == null) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new ContainerNotFoundException(
                    containerName), null);
        }
        if (this.isolationManager
                .checkContainerAvailability(container.getReference(), isolationLevel, stx,
                        OperationType.LOOKUPCONTAINER).getType().equals(AvailabilityType.NOTVISIBLE)) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new ContainerNotFoundException(
                    containerName), null);
        }
        // we allow the lookup for NOTAVAILABLE containers (delete or exclusive lock)

        successfulLookupContainerOperationCounter.incrementAndGet();
        return new DefaultContainerOperationResult(OperationStatus.OK, null, container.getReference());
    }

    @Override
    public void purgeContainer(final LocalContainerReference cref) {
        if (cref == null) {
            throw new NullPointerException("The containerReference must not be null");
        }
        log.debug("Purging container {} ", cref);
        NativeContainer container = containers.remove(cref.getId());
        if (container == null) {
            log.debug("Cannot find container {} to purge", cref);
        }
        if (container != null) {
            if (container.getName() != null) {
                containerNames.remove(container.getName());
            }
            container.dispose();
        }
    }

    @Override
    public NativeContainer getContainer(final LocalContainerReference cRef) throws InvalidContainerException {
        NativeContainer container = this.containers.get(cRef.getId());
        if (container == null) {
            throw new InvalidContainerException();
        }
        return container;
    }

    @Override
    public ContainerOperationResult lockContainer(final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final NativeSubTransaction stx, final RequestContext context) {

        lockContainerOperationCounter.incrementAndGet();

        if (cRef == null) {
            throw new NullPointerException("The cRef must not be null");
        }
        if (isolationLevel == null) {
            throw new NullPointerException("The isolationLevel must not be null");
        }
        if (stx == null) {
            throw new NullPointerException("The SubTransaction must not be null");
        }

        /* Check SubTransaction Status */
        if (!stx.getStatus().equals(TransactionStatus.RUNNING)) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, new InvalidSubTransactionException(
                    stx.getId()), cRef);
        }

        LockResult lockResult;
        try {
            lockResult = this.isolationManager.accquireContainerLock(OperationType.LOCKCONTAINER, cRef, isolationLevel,
                    stx, this);
        } catch (InvalidContainerException ex) {
            return new DefaultContainerOperationResult(OperationStatus.NOTOK, ex, cRef);
        }
        if (!lockResult.isValid()) {
            return new DefaultContainerOperationResult(OperationStatus.LOCKED,
                    LockedExceptionsHelper.newContainerLockedException(lockResult), cRef);
        }

        successfulLockContainerOperationCounter.incrementAndGet();
        return new DefaultContainerOperationResult(OperationStatus.OK, null, cRef);
    }

    @Override
    public void close() throws MzsCoreRuntimeException {
        for (NativeContainer container : containers.values()) {
            container.close();
        }
    }

    // meta model methods below
    @Override
    public Object navigate(final String path) {
        return MetaModelUtils.navigate(path, this, metaModel);
    }

    @Override
    public Object getMetaData(final int depth) {
        return MetaModelUtils.getData(depth, metaModel);
    }

    @Override
    public void setMetaDataProperty(final String key, final Object value) {
        throw new UnsupportedOperationException("Properties cannot be set here");
    }

}
