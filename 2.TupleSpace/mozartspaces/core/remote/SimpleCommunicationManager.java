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
package org.mozartspaces.core.remote;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.jcip.annotations.ThreadSafe;

import org.mozartspaces.core.Message;
import org.mozartspaces.core.RemotingException;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * The <code>SimpleCommunicationManager</code> manages multiple Sender and Receiver
 * components. It manages the shutdown of these components and when a message is
 * sent, it selects the Sender for a {@link Message} based on the scheme of the
 * message's destination space URI.
 *
 * @author Tobias Doenz
 */
@ThreadSafe
public final class SimpleCommunicationManager implements Sender {

    private static final Logger log = LoggerFactory.get();

    private final List<Receiver> receivers;
    private final Map<String, Sender> senders;
    private final String defaultScheme;

    /**
     * Counter for the number of messages that where submitted to a sender.
     */
    private final AtomicLong sentMessagesCounter;

    /**
     * Constructs a <code>SimpleCommunicationManager</code>.
     *
     * @param receivers
     *            a collection of the Receiver components
     * @param senders
     *            the mapping of URI schemes (keys) to Sender components
     *            (values), used when a {@link #sendMessage(Message) message is
     *            sent}
     * @param defaultScheme
     *            the default scheme
     */
    public SimpleCommunicationManager(final Collection<Receiver> receivers, final Map<String, Sender> senders,
            final String defaultScheme) {
        this.receivers = Collections.synchronizedList(new ArrayList<Receiver>(receivers));
        assert this.receivers != null;
        this.senders = new ConcurrentHashMap<String, Sender>(senders);
        assert this.senders != null;
        this.defaultScheme = defaultScheme;
        assert this.defaultScheme != null;

        sentMessagesCounter = new AtomicLong();
    }

    @Override
    public void sendMessage(final Message<?> message) throws SerializationException {
        assert message != null;
        URI destination = message.getDestinationSpace();
        String scheme = destination.getScheme();
        if (scheme == null) {
            scheme = defaultScheme;
        }
        log.debug("Selecting sender for scheme of {}", destination);
        Sender sender = senders.get(scheme);
        if (sender == null) {
            throw new RemotingException("No sender for scheme " + scheme);
        }
        sender.sendMessage(message);
        sentMessagesCounter.incrementAndGet();
    }

    @Override
    public long getNumberOfSentMessages() {
        return sentMessagesCounter.get();
    }

    @Override
    public void shutdown(final boolean wait) {
        for (Receiver receiver : receivers) {
            receiver.shutdown(wait);
        }
        for (Sender sender : senders.values()) {
            sender.shutdown(wait);
        }
    }

}
