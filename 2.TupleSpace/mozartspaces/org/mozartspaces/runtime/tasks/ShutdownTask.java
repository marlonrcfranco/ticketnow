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
package org.mozartspaces.runtime.tasks;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.config.Configuration;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.runtime.aspects.AspectInvoker;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>ShutdownTask</code> shuts down the space.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class ShutdownTask extends AbstractTask<Nothing> {

    private static final Logger log = LoggerFactory.get();

    private final ShutdownRequest request;
    private final AspectInvoker aspectInvoker;
    private final MzsCore core;

    private final RuntimeData runtimeData;

    /**
     * Constructs a <code>ShutdownTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public ShutdownTask(final RequestMessage requestMessage, final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.runtimeData = runtimeData;
        this.request = (ShutdownRequest) requestMessage.getContent();
        this.aspectInvoker = runtimeData.getAspectInvoker();
        this.core = runtimeData.getCore();
    }

    @Override
    protected Nothing runSpecific() throws Throwable {

        // invoke pre-aspects
        AspectResult result = aspectInvoker.preShutdown(request);
        AspectStatus status = result.getStatus();
        switch (status) {
        case OK:
        case SKIP:
            break;
        default:
            // TODO? throw exception for LOCKED, DELAYABLE (no Capi3AspectPort)
            handleSpecialResult(status.toOperationStatus(), result.getCause(), RequestTimeout.INFINITE);
            return null;
        }

        // invoke operation
        if (status != AspectStatus.SKIP) {
            core.shutdown(false);

            // HACK to test reboot functionality
            if (request.getContext() != null) {
                final Configuration config = (Configuration) request.getContext().getSystemProperty(
                        ShutdownRequest.CONTEXT_KEY_REBOOTCONFIG);
                if (config != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            MzsCore newCore = DefaultMzsCore.newInstance(config);
                            log.info("Started new core");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            runtimeData.setCore(newCore);
                            log.info("Set new core");
                        }
                    } .start();
                }
            }
        } else {
            log.info("Skipping operation due to pre-aspects status");
        }

        return Nothing.INSTANCE;

    }

}
