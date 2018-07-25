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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Manages responses to the embedded core. These responses include:
 * <ul>
 * <li>responses to the virtual answer container of this core, for requests that
 * haven been sent locally (Core API).
 * <li>callbacks for requests that have been sent locally (Core API) and were
 * processed in the embedded or a remote core.
 * <li>responses to (non-virtual) user containers in this space, either from
 * local or remote requests.
 * </ul>
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class EmbeddedResponseHandler implements ResponseHandler {

    private static final Logger log = LoggerFactory.get();

    private final CoreUtils coreUtils;
    private final UnansweredRequestStore vac;

    private volatile MzsCore core;

    /**
     * Constructs an <code>EmbeddedResponseHandler</code>.
     *
     * @param coreUtils
     *            the core utils
     * @param vac
     *            the virtual answer container
     */
    public EmbeddedResponseHandler(final CoreUtils coreUtils, final UnansweredRequestStore vac) {
        this.coreUtils = coreUtils;
        assert this.coreUtils != null;
        this.vac = vac;
        assert this.vac != null;
    }

    @Override
    public void processResponse(final ResponseMessage responseMessage) {

        assert responseMessage != null;

        RequestReference requestRef = responseMessage.getRequestReference();
        Response<?> response = responseMessage.getContent();
        AnswerContainerInfo answerContainerInfo = responseMessage.getAnswerContainerInfo();

        // TODO add special VAC ID to allow responses to VAC with alias Space URI (and thus other transport)
        if (answerContainerInfo == null) {
            vac.removeRequestAndSetResponse(requestRef, response);
        } else {
            writeAnswerToSpaceContainer(response, answerContainerInfo);
        }
    }

    /**
     * @param core
     *            the core to set
     */
    public void setCore(final MzsCore core) {
        this.core = core;
        assert this.core != null;
    }

    private void writeAnswerToSpaceContainer(final Response<?> response,
            final AnswerContainerInfo answerContainerInfo) {

        ContainerReference container = answerContainerInfo.getContainer();
        if (!coreUtils.isEmbeddedSpace(container.getSpace())) {
            log.warn("Answer container {} is not on this space", container);
            return;
        }

        Serializable entryValue;
        Object result = response.getResult();
        if (result != null) {
            entryValue = (Serializable) result;
        } else {
            entryValue = response.getError();
        }

        String coordinationKey = answerContainerInfo.getCoordinationKey();
        List<CoordinationData> coordData = new ArrayList<CoordinationData>();
        coordData.add(FifoCoordinator.newCoordinationData());
        if (coordinationKey != null) {
            coordData.add(KeyCoordinator.newCoordinationData(coordinationKey));
            log.debug("Writing answer to container {} with key {}", container, coordinationKey);
        } else {
            log.debug("Writing answer to container {}", container);
        }
        List<Entry> entries = Collections.singletonList(new Entry(entryValue, coordData));
        Request<?> writeAnswer = new WriteEntriesRequest(entries, container, RequestTimeout.INFINITE, null,
                MzsConstants.DEFAULT_ISOLATION, null);
        // TODO infinite timeout? how to handle errors?
        core.send(writeAnswer, container.getSpace());
    }
}
