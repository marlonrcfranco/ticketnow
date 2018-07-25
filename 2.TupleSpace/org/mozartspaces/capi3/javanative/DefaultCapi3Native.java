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
package org.mozartspaces.capi3.javanative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.AuthTargetCoordinator;
import org.mozartspaces.capi3.AuthTargetCoordinator.AuthTargetSelector;
import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.ContainerOperationResult;
import org.mozartspaces.capi3.CoordinationTranslationFactory;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidTransactionException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LifoCoordinator.LifoSelector;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.OperationStatus;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.QueryCoordinator.QuerySelector;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.RandomCoordinator.RandomSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.capi3.TypeCoordinator;
import org.mozartspaces.capi3.TypeCoordinator.TypeSelector;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.capi3.VectorCoordinator.VectorSelector;
import org.mozartspaces.capi3.javanative.authorization.DefaultAccessManager;
import org.mozartspaces.capi3.javanative.authorization.NativeAccessManager;
import org.mozartspaces.capi3.javanative.coordination.DefaultAnyCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultAuthTargetCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultLabelCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultLindaCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueryCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueueCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultQueueCoordinator.QueueOrder;
import org.mozartspaces.capi3.javanative.coordination.DefaultRandomCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultTypeCoordinator;
import org.mozartspaces.capi3.javanative.coordination.DefaultVectorCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeCoordinator;
import org.mozartspaces.capi3.javanative.coordination.NativeSelector;
import org.mozartspaces.capi3.javanative.isolation.DefaultIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.DefaultTransaction;
import org.mozartspaces.capi3.javanative.isolation.NativeIsolationManager;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.operation.DefaultContainerManager;
import org.mozartspaces.capi3.javanative.operation.DefaultEntryOperationResult;
import org.mozartspaces.capi3.javanative.operation.DefaultOperationResult;
import org.mozartspaces.capi3.javanative.operation.NativeContainer;
import org.mozartspaces.capi3.javanative.operation.NativeContainerManager;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.util.AndroidHelperUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * CAPI-3 implementation with JDK collections (mainly hash maps) or alternatively persistent maps for data storage,
 * coordinator and transactions.
 *
 * @author Martin Barisits
 * @author Tobias Doenz
 * @author Stefan Crass
 */
public final class DefaultCapi3Native implements Capi3 {

    private static Logger log = LoggerFactory.get();

    private final AtomicLong txIdCounter = new AtomicLong();

    private final CoordinationTranslationFactory translationFactory;

    private final NativeContainerManager containerManager;

    private final PersistenceContext configuredPersistenceContext;
    private final PersistenceContext inMemoryPersistenceContext;

    /**
     * Constructs an instance of <code>DefaultCapi3Native</code> with the default module implementations.
     *
     * @param translationFactory
     *            the translation factory for the translation between API classes and implementation classes of
     *            coordinators and selectors
     * @param configuredPersistenceContext
     *            the configured persistence context
     * @param authorizationEnabled
     *            whether the authorization is enabled
     */
    public DefaultCapi3Native(final CoordinationTranslationFactory translationFactory,
            final PersistenceContext configuredPersistenceContext, final boolean authorizationEnabled) {

        this.translationFactory = translationFactory;
        if (this.translationFactory == null) {
            throw new NullPointerException("Coordination translation factory");
        }

        this.configuredPersistenceContext = configuredPersistenceContext;
        this.inMemoryPersistenceContext = new PersistenceContext();

        NativeIsolationManager isolationManager = new DefaultIsolationManager();
        NativeAccessManager accessManager = null;
        if (authorizationEnabled) {
            accessManager = new DefaultAccessManager(this);
        }
        this.containerManager = new DefaultContainerManager(isolationManager, accessManager,
                this.configuredPersistenceContext, this.inMemoryPersistenceContext);

        try {
            Transaction restoreTransaction = newTransaction();
            SubTransaction restoreSubTransaction = restoreTransaction.newSubTransaction();
            containerManager.restoreContainers((NativeSubTransaction) restoreSubTransaction);
            restoreSubTransaction.commit();
            restoreTransaction.commit();
        } catch (Exception e) {
            throw new MzsCoreRuntimeException("Error while restoring previous state.", e);
        }
        ((DefaultContainerManager) containerManager).initPolicyContainer();

        log.info("Started capi3-javanative");
    }

    /**
     * Constructs an instance of <code>DefaultCapi3Native</code> with the default module implementations. Just for
     * testing!
     */
    @SuppressWarnings("unchecked")
    public DefaultCapi3Native() {
        this(new CoordinationTranslationFactory(Collections.EMPTY_MAP, Collections.EMPTY_MAP),
                new PersistenceContext(), false);
    }

    private void checkCommonArguments(final IsolationLevel isolationLevel, final SubTransaction stx) {
        if (isolationLevel == null) {
            throw new NullPointerException("Isolation level");
        }
        if (stx == null) {
            throw new NullPointerException("Sub-transaction");
        }
        if (!(stx instanceof NativeSubTransaction)) {
            throw new IllegalArgumentException("The Sub-transaction must be a NativeSubTransaction");
        }
    }

    /**
     * Quick hack for the meta model to access the containers.
     *
     * // TODO refactor to get rid of this hack
     *
     * @return the container manager
     */
    public NativeContainerManager getContainerManager() {
        return containerManager;
    }

    @Override
    public ContainerOperationResult executeContainerDestroyOperation(final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {
        if (cRef == null) {
            throw new NullPointerException("Container reference");
        }
        checkCommonArguments(isolationLevel, stx);
        return containerManager.destroyContainer(cRef, isolationLevel, (NativeSubTransaction) stx, context);
    }

    @Override
    public ContainerOperationResult executeContainerLookupOperation(final String containerName,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {
        if (containerName == null) {
            throw new NullPointerException("Container name");
        }
        if (AndroidHelperUtils.isEmpty(containerName)) {
            throw new IllegalArgumentException("Unnamed container");
        }
        checkCommonArguments(isolationLevel, stx);
        return containerManager.lookupContainer(containerName, isolationLevel, (NativeSubTransaction) stx, context);
    }

    private void checkSelectingOperationArguments(final LocalContainerReference cref,
            final List<? extends Selector> selectors) {
        if (cref == null) {
            throw new NullPointerException("Container reference");
        }
        if (selectors == null) {
            throw new NullPointerException("Selector list");
        }
        if (selectors.isEmpty()) {
            throw new IllegalArgumentException("Selector list empty");
        }
    }

    @Override
    public EntryOperationResult executeReadOperation(final LocalContainerReference cRef,
            final List<? extends Selector> selectors, final IsolationLevel isolationLevel, final SubTransaction stx,
            final RequestContext context) {

        checkSelectingOperationArguments(cRef, selectors);
        checkCommonArguments(isolationLevel, stx);

        NativeContainer container;
        try {
            container = this.containerManager.getContainer(cRef); // TODO return null instead of throwing exception
        } catch (InvalidContainerException e) {
            return new DefaultEntryOperationResult(OperationStatus.NOTOK, new InvalidContainerException(), null);
        }

        return container.executeReadOperation(this.translateCommonSelectors(selectors), isolationLevel,
                (NativeSubTransaction) stx, context);
    }

    @Override
    public EntryOperationResult executeTakeOperation(final LocalContainerReference cRef,
            final List<? extends Selector> selectors, final IsolationLevel isolationLevel, final SubTransaction stx,
            final RequestContext context) {

        checkSelectingOperationArguments(cRef, selectors);
        checkCommonArguments(isolationLevel, stx);

        NativeContainer container;
        try {
            container = this.containerManager.getContainer(cRef);
        } catch (InvalidContainerException e) {
            return new DefaultEntryOperationResult(OperationStatus.NOTOK, new InvalidContainerException(), null);
        }

        return container.executeTakeOperation(this.translateCommonSelectors(selectors), isolationLevel,
                (NativeSubTransaction) stx, context);
    }

    @Override
    public OperationResult executeWriteOperation(final LocalContainerReference cRef, final Entry entry,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {

        if (cRef == null) {
            throw new NullPointerException("Container is null");
        }
        checkCommonArguments(isolationLevel, stx);

        NativeContainer container;
        try {
            container = this.containerManager.getContainer(cRef);
        } catch (InvalidContainerException e) {
            return new DefaultOperationResult(OperationStatus.NOTOK, new InvalidContainerException());
        }
        return container.executeWriteOperation(entry, isolationLevel, (NativeSubTransaction) stx, context);
    }

    @Override
    public Transaction newTransaction() {
        DefaultTransaction tx = new DefaultTransaction(txIdCounter.incrementAndGet());
        try {
            tx.addLog(configuredPersistenceContext.createPersistentTransaction(tx));
            tx.addLog(inMemoryPersistenceContext.createPersistentTransaction(tx));
        } catch (InvalidTransactionException e) {
            throw new IllegalStateException("New transactions must never be invalid.", e);
        }
        return tx;
    }

    @Override
    public void shutDown() {
        log.info("Shut capi3-javanative down");
        containerManager.close();
        configuredPersistenceContext.close();
        inMemoryPersistenceContext.close();
    }

    @Override
    public ContainerOperationResult executeContainerCreateOperation(final String containerName,
            final List<? extends Coordinator> obligatoryCoordinators,
            final List<? extends Coordinator> optionalCoordinators, final int size,
            final IsolationLevel isolationLevel, final SubTransaction stx, final AuthorizationLevel auth,
            final boolean forceInMemory, final RequestContext context) {
        if (obligatoryCoordinators == null) {
            throw new NullPointerException("Obligatory coordinator list");
        }
        if (optionalCoordinators == null) {
            throw new NullPointerException("Optional Coordinator list");
        }
        if (optionalCoordinators.isEmpty() && obligatoryCoordinators.isEmpty()) {
            throw new IllegalArgumentException("At least one Coordinator must be registered at the Container");
        }
        checkCommonArguments(isolationLevel, stx);

        return this.containerManager.createContainer(containerName, obligatoryCoordinators,
                this.translateCommonCoordinators(obligatoryCoordinators), optionalCoordinators,
                this.translateCommonCoordinators(optionalCoordinators), size, isolationLevel,
                (NativeSubTransaction) stx, auth, forceInMemory, context);
    }

    /**
     * Translates a list of selectors into their corresponding internal CAPI-3 versions.
     *
     * @param originalSelectors
     *            the original selectors
     * @return the translated internal selectors
     */
    public List<NativeSelector<?>> translateCommonSelectors(final List<? extends Selector> originalSelectors) {
        List<NativeSelector<?>> translatedSelectors = new ArrayList<NativeSelector<?>>();
        for (Selector selector : originalSelectors) {
            translatedSelectors.add(this.translateSelector(selector));
        }
        return translatedSelectors;
    }

    /**
     * Translates a list of coordinators into their corresponding internal CAPI-3 versions.
     *
     * @param origCoordinators
     *            the original coordinators
     * @return the translated internal coordinators
     */
    public List<NativeCoordinator> translateCommonCoordinators(final List<? extends Coordinator> origCoordinators) {
        List<NativeCoordinator> translatedCoordinators = new ArrayList<NativeCoordinator>();
        for (Coordinator coordinator : origCoordinators) {
            translatedCoordinators.add(this.translateCoordinator(coordinator));
        }
        return translatedCoordinators;
    }

    private NativeSelector<?> translateSelector(final Selector selector) {
        if (selector instanceof NativeSelector) {
            return (NativeSelector<?>) selector;
        }

        if (selector.getClass().equals(AnySelector.class)) {
            return DefaultAnyCoordinator.newSelector(selector.getName(), selector.getCount());
        } else if (selector.getClass().equals(FifoSelector.class)) {
            return DefaultQueueCoordinator.newSelector(selector.getName(), selector.getCount());
        } else if (selector.getClass().equals(KeySelector.class)) {
            return DefaultLabelCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((KeySelector) selector).getKey());
        } else if (selector.getClass().equals(LabelSelector.class)) {
            return DefaultLabelCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((LabelSelector) selector).getLabel());
        } else if (selector.getClass().equals(LifoSelector.class)) {
            return DefaultQueueCoordinator.newSelector(selector.getName(), selector.getCount());
        } else if (selector.getClass().equals(LindaSelector.class)) {
            return DefaultLindaCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((LindaSelector) selector).getTemplate());
        } else if (selector.getClass().equals(QuerySelector.class)) {
            return DefaultQueryCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((QuerySelector) selector).getQuery());
        } else if (selector.getClass().equals(RandomSelector.class)) {
            return DefaultRandomCoordinator.newSelector(selector.getName(), selector.getCount());
        } else if (selector.getClass().equals(VectorSelector.class)) {
            return DefaultVectorCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((VectorSelector) selector).getIndex());
        } else if (selector.getClass().equals(AuthTargetSelector.class)) {
            return DefaultAuthTargetCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((AuthTargetSelector) selector).getTarget());
        } else if (selector.getClass().equals(TypeSelector.class)) {
            return DefaultTypeCoordinator.newSelector(selector.getName(), selector.getCount(),
                    ((TypeSelector) selector).getType());
        } else {
            return translationFactory.createSelector(selector);
        }
    }

    private NativeCoordinator translateCoordinator(final Coordinator coordinator) {
        if (coordinator instanceof NativeCoordinator) {
            return (NativeCoordinator) coordinator;
        }

        if (coordinator.getClass().equals(AnyCoordinator.class)) {
            return new DefaultAnyCoordinator(coordinator.getName());
        } else if (coordinator.getClass().equals(FifoCoordinator.class)) {
            return new DefaultQueueCoordinator(coordinator.getName(), QueueOrder.FIFO);
        } else if (coordinator.getClass().equals(KeyCoordinator.class)) {
            return new DefaultLabelCoordinator(coordinator.getName(), true);
        } else if (coordinator.getClass().equals(LabelCoordinator.class)) {
            return new DefaultLabelCoordinator(coordinator.getName(), false);
        } else if (coordinator.getClass().equals(LifoCoordinator.class)) {
            return new DefaultQueueCoordinator(coordinator.getName(), QueueOrder.LIFO);
        } else if (coordinator.getClass().equals(LindaCoordinator.class)) {
            LindaCoordinator lindaCoordinator = (LindaCoordinator) coordinator;
            return new DefaultLindaCoordinator(coordinator.getName(), lindaCoordinator.isOnlyAnnotatedEntries());
        } else if (coordinator.getClass().equals(QueryCoordinator.class)) {
            return new DefaultQueryCoordinator(coordinator.getName());
        } else if (coordinator.getClass().equals(RandomCoordinator.class)) {
            return new DefaultRandomCoordinator(coordinator.getName());
        } else if (coordinator.getClass().equals(VectorCoordinator.class)) {
            return new DefaultVectorCoordinator(coordinator.getName());
        } else if (coordinator.getClass().equals(AuthTargetCoordinator.class)) {
            return new DefaultAuthTargetCoordinator(coordinator.getName());
        } else if (coordinator.getClass().equals(TypeCoordinator.class)) {
            TypeCoordinator typeCoordinator = (TypeCoordinator) coordinator;
            return new DefaultTypeCoordinator(coordinator.getName(), typeCoordinator.getAllowedTypes());
        } else {
            return translationFactory.createCoordinator(coordinator);
        }
    }

    @Override
    public ContainerOperationResult executeContainerLockOperation(final LocalContainerReference cRef,
            final IsolationLevel isolationLevel, final SubTransaction stx, final RequestContext context) {
        if (cRef == null) {
            throw new NullPointerException("Container reference");
        }
        checkCommonArguments(isolationLevel, stx);

        return this.containerManager.lockContainer(cRef, isolationLevel, (NativeSubTransaction) stx, context);
    }
}
