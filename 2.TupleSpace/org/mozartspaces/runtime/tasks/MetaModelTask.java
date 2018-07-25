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

import java.io.Serializable;

import net.jcip.annotations.NotThreadSafe;

import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.metamodel.MetaModelException;
import org.mozartspaces.core.metamodel.MetaModelUtils;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.runtime.RuntimeData;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * A <code>MetaModelTask</code> requests data from the meta model.
 *
 * @author Tobias Doenz
 */
@NotThreadSafe
public final class MetaModelTask extends AbstractTask<Serializable> {

    private static final Logger log = LoggerFactory.get();

    private final MetaModelRequest request;

    private final RuntimeData runtimeData;

    /**
     * Constructs a <code>ShutdownTask</code>.
     *
     * @param requestMessage
     *            the request message
     * @param runtimeData
     *            runtime objects and components
     */
    public MetaModelTask(final RequestMessage requestMessage, final RuntimeData runtimeData) {
        super(requestMessage, runtimeData);
        this.runtimeData = runtimeData;
        this.request = (MetaModelRequest) requestMessage.getContent();
    }

    @Override
    protected Serializable runSpecific() throws Throwable {

        String path = request.getPath();
        int depth = request.getDepth();
        log.debug("Navigate to path {}", path);
        Object metaModelNode = runtimeData.navigate(path);
        log.debug("Got meta model node {}", metaModelNode);
        if (metaModelNode == null) {
            throw new MetaModelException("Could not navigate to " + path);
        }

        if (depth != Integer.MAX_VALUE) {
            log.debug("Using depth {}", depth);
        }

        Object metaData = MetaModelUtils.getDataValue(depth, metaModelNode);
        log.debug("Got meta data: {}", metaData);

        if (metaData == null) {
            metaData = Nothing.INSTANCE;
        }
        return (Serializable) metaData;
    }

}
