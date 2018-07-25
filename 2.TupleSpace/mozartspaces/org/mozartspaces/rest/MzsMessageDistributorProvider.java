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
package org.mozartspaces.rest;

import java.lang.reflect.Type;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.mozartspaces.core.remote.MessageDistributor;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

/**
 * Provides the Singelton MessageDistributor instances to JAXRS-Resources which is bound in the Servlet Context. This
 * way multiple Servers can be started that use different MessageDistributor.
 *
 * @author Christian Proinger
 */
@Provider
public final class MzsMessageDistributorProvider extends AbstractHttpContextInjectable<MessageDistributor> implements
        InjectableProvider<Context, Type> {

    // private static MzsCore core = DefaultMzsCore.newInstance();

    @Context
    ServletContext context;

    @Override
    public Injectable<MessageDistributor> getInjectable(final ComponentContext ic, final Context a, final Type c) {
        if (c.equals(MessageDistributor.class)) {
            return this;
        }

        return null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }

    @Override
    public MessageDistributor getValue(final HttpContext c) {
        return (MessageDistributor) context.getAttribute(MzsBaseWebServer.MZS_MESSAGE_DISTRIBUTOR);
    }

    @Override
    public MessageDistributor getValue() {
        return (MessageDistributor) context.getAttribute(MzsBaseWebServer.MZS_MESSAGE_DISTRIBUTOR);
    }
}
