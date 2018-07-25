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
package org.mozartspaces.runtime;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.Capi3;
import org.mozartspaces.capi3.javanative.DefaultCapi3Native;
import org.mozartspaces.capi3.javanative.operation.NativeContainerManager;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.MetaModelKeys;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.metamodel.MetaModelUtils.MethodTuple;
import org.mozartspaces.core.metamodel.Navigable;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.runtime.aspects.AspectManager;
import org.mozartspaces.runtime.blocking.TimeoutProcessor;
import org.mozartspaces.runtime.blocking.WaitAndEventManager;
import org.mozartspaces.runtime.blocking.deadlock.LockedTaskHandler;
import org.mozartspaces.runtime.tasks.Task;
import org.mozartspaces.runtime.util.EntryCopier;
import org.mozartspaces.runtime.util.RuntimeUtils;

/**
 * Runtime objects used in tasks (dependencies). This class is also the root of the meta model and used in the
 * respective task.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class RuntimeData implements Navigable, MetaDataProvider {

    private final ResponseDistributor responseDistributor;
    private final Capi3 capi3;
    private final TransactionManager txManager;
    private final AspectInvoker aspectInvoker;
    private final AspectManager aspectManager;

    private final EntryCopier entryCopier;
    private final RuntimeUtils runtimeUtils;
    private final LockedTaskHandler lockedTaskHandler;

    private volatile MzsCore core;
    private volatile TimeoutProcessor<Task> requestTimeoutProcessor;
    private volatile WaitAndEventManager waitEventManager;

    private final Map<String, Object> metaModel;

    /**
     * Constructs a <code>RuntimeData</code> object.
     *
     * @param responseDistributor
     *            the response distributor
     * @param capi3
     *            the CAPI-3 implementation
     * @param txManager
     *            the Transaction Manager
     * @param aspectInvoker
     *            the aspect invoker
     * @param aspectManager
     *            the aspect manager
     * @param entryCopier
     *            the entry copier
     * @param runtimeUtils
     *            the Runtime utils
     * @param lockedTaskHandler
     *            the Locked Task Handler
     */
    public RuntimeData(final ResponseDistributor responseDistributor, final Capi3 capi3,
            final TransactionManager txManager, final AspectInvoker aspectInvoker, final AspectManager aspectManager,
            final EntryCopier entryCopier, final RuntimeUtils runtimeUtils, final LockedTaskHandler lockedTaskHandler) {
        this.responseDistributor = responseDistributor;
        assert this.responseDistributor != null;
        this.capi3 = capi3;
        assert this.capi3 != null;
        this.txManager = txManager;
        assert this.txManager != null;
        this.aspectInvoker = aspectInvoker;
        assert this.aspectInvoker != null;
        this.aspectManager = aspectManager;
        assert this.aspectManager != null;
        this.entryCopier = entryCopier;
        // assert this.entryCopier != null;
        this.runtimeUtils = runtimeUtils;
        assert this.runtimeUtils != null;
        this.lockedTaskHandler = lockedTaskHandler;

        metaModel = new HashMap<String, Object>();
        if (capi3 instanceof DefaultCapi3Native) {
            // for testing, otherwise tests fail with a ClassCastException
            NativeContainerManager containerMng = ((DefaultCapi3Native) capi3).getContainerManager();
            metaModel.put(MetaModelKeys.CONTAINERS, containerMng);
        }

        // system properties
        Map<String, Object> systemProperties = new HashMap<String, Object>();
        Method runtimeMethod = null;
        try {
            runtimeMethod = Runtime.class.getMethod("availableProcessors", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        systemProperties.put(MetaModelKeys.System.AVAILABLE_PROCESSORS,
                new MethodTuple(runtimeMethod, Runtime.getRuntime()));
        try {
            runtimeMethod = Runtime.class.getMethod("freeMemory", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        systemProperties.put(MetaModelKeys.System.FREE_MEMORY, new MethodTuple(runtimeMethod, Runtime.getRuntime()));
        try {
            runtimeMethod = Runtime.class.getMethod("maxMemory", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        systemProperties.put(MetaModelKeys.System.MAX_MEMORY, new MethodTuple(runtimeMethod, Runtime.getRuntime()));
        try {
            runtimeMethod = Runtime.class.getMethod("totalMemory", (Class<?>[]) null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        systemProperties.put(MetaModelKeys.System.TOTAL_MEMORY, new MethodTuple(runtimeMethod, Runtime.getRuntime()));
        metaModel.put(MetaModelKeys.SYSTEM, systemProperties);

    }

    /**
     * Shuts down some of the runtime components.
     */
    public void shutdown() {
        txManager.shutdown();
        if (requestTimeoutProcessor != null) {
            requestTimeoutProcessor.shutdown();
        }
        if (waitEventManager != null) {
            waitEventManager.shutdown();
        }
    }

    /**
     * @return the Response Distributor
     */
    public ResponseDistributor getResponseDistributor() {
        return responseDistributor;
    }

    /**
     * @return the CAPI3 implementation
     */
    public Capi3 getCapi3() {
        return capi3;
    }

    /**
     * @return the Transaction Manager
     */
    public TransactionManager getTxManager() {
        return txManager;
    }

    /**
     * @return the aspect invoker
     */
    public AspectInvoker getAspectInvoker() {
        return aspectInvoker;
    }

    /**
     * @return the Aspect Manager
     */
    public AspectManager getAspectManager() {
        return aspectManager;
    }

    /**
     * @return the Entry Copier
     */
    public EntryCopier getEntryCopier() {
        return entryCopier;
    }

    /**
     * @return the Space Utils
     */
    public RuntimeUtils getRuntimeUtils() {
        return runtimeUtils;
    }

    /**
     * @return the Locked Task Handler
     */
    public LockedTaskHandler getLockedTaskHandler() {
        return lockedTaskHandler;
    }

    /**
     * @param core
     *            the core to set
     */
    public void setCore(final MzsCore core) {
        this.core = core;
        assert this.core != null;
        metaModel.put(MetaModelKeys.CONFIG, this.core.getConfig());
        if (core instanceof MetaDataProvider) {
            metaModel.put(MetaModelKeys.RUNTIME, this.core);
        }
    }

    /**
     * @return the core
     */
    public MzsCore getCore() {
        return core;
    }

    /**
     * @param requestTimeoutProcessor
     *            the requestTimeoutProcessor to set
     */
    public void setRequestTimeoutProcessor(final TimeoutProcessor<Task> requestTimeoutProcessor) {
        this.requestTimeoutProcessor = requestTimeoutProcessor;
        assert this.requestTimeoutProcessor != null;
    }

    /**
     * @return the requestTimeoutProcessor
     */
    public TimeoutProcessor<Task> getRequestTimeoutProcessor() {
        return requestTimeoutProcessor;
    }

    /**
     * @return the waitEventManager
     */
    public WaitAndEventManager getWaitEventManager() {
        return waitEventManager;
    }

    /**
     * @param waitEventManager
     *            the waitEventManager to set
     */
    public void setWaitEventManager(final WaitAndEventManager waitEventManager) {
        this.waitEventManager = waitEventManager;
        assert this.waitEventManager != null;
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
