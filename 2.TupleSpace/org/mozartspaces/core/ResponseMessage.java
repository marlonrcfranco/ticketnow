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

import net.jcip.annotations.Immutable;

/**
 * A <code>Message</code> that contains a <code>Response</code>.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class ResponseMessage extends AbstractMessage<Response<?>> {

    private static final long serialVersionUID = 1L;

    private static final Response<?> DUMMY_RESPONSE = new GenericResponse<String>("", null);

    /**
     * Constructs a <code>ResponseMessage</code>.
     *
     * @param requestRef
     *            the request reference
     * @param response
     *            the response
     * @param answerContainerInfo
     *            the answer container information, may be <code>null</code>
     */
    public ResponseMessage(final RequestReference requestRef, final Response<?> response,
            final AnswerContainerInfo answerContainerInfo) {
        super(requestRef, response, answerContainerInfo);
    }

    // for serialization
    private ResponseMessage() {
        super(RequestReference.DUMMY, DUMMY_RESPONSE, null);
    }

    @Override
    public URI getDestinationSpace() {
        AnswerContainerInfo answerContainerInfo = getAnswerContainerInfo();
        if (answerContainerInfo != null) {
            return answerContainerInfo.getContainer().getSpace();
        }
        // answer to VAC of sender space
        return getRequestReference().getSpace();
    }

}
