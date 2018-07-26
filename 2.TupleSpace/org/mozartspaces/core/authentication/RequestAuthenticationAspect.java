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
package org.mozartspaces.core.authentication;

import java.util.Set;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.aspects.AbstractSpaceAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.authorization.NamedValue;
import org.mozartspaces.core.requests.AbstractRequest;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.security.IdentityConstants;
import org.mozartspaces.core.security.RequestContextUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * Internal aspect used for the authentication of requests. Every request is authenticated with the provided identity
 * provider.
 *
 * @author Tobias Doenz
 */
public final class RequestAuthenticationAspect extends AbstractSpaceAspect {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.get();

    private final IdentityProvider idp;

    /**
     * Creates an aspect that uses the provided identity provider.
     *
     * @param idp
     *            the identity provider used for authentication
     */
    public RequestAuthenticationAspect(final IdentityProvider idp) {
        this.idp = idp;
        assert this.idp != null;
    }

    private AspectResult authenticateRequest(final AbstractRequest<?> request) {
        RequestContext context = request.getContext();
        if (!RequestContextUtils.isRemoteRequest(context)) {
            log.debug("Request authentication not required (embedded request)");
            return AspectResult.OK;
        }
        log.debug("Authenticating request of type {}", request.getClass().getName());
        return checkToken(context);
    }

    private AspectResult checkToken(final RequestContext ctx) {
        Object tokenIdProperty = ctx.getRequestProperty(IdentityConstants.TOKEN_ID_PROPERTY_KEY);
        if (tokenIdProperty == null || !(tokenIdProperty instanceof String)) {
            return new AspectResult(AspectStatus.NOTOK, new AuthenticationException("Token ID not set in context"));
        }
        String tokenId = (String) tokenIdProperty;
        try {
            log.debug("Checking SSO token {}", tokenId);
            Set<NamedValue> validAttributes = idp.checkToken(tokenId);
            Object claimedAttributesProperty = ctx.getRequestProperty(RequestContextUtils.ATTRIBUTES_PROPERTY_KEY);
            if (claimedAttributesProperty == null || !(claimedAttributesProperty instanceof Set)) {
                return new AspectResult(AspectStatus.NOTOK, new AuthenticationException(
                        "Claimed attributes not set in context"));
            }
            @SuppressWarnings("unchecked")
            Set<NamedValue> claimedAttributes = (Set<NamedValue>) claimedAttributesProperty;
            if (claimedAttributes.equals(validAttributes)) {
                log.debug("Claimed attributes are valid");
            } else {
                log.debug("Claimed attributes {} do not match attributes from IDP: {}", claimedAttributes,
                        validAttributes);
                return new AspectResult(AspectStatus.NOTOK, new AuthenticationException("Claimed attributes "
                        + claimedAttributes + " do not match attributes from IDP"));
            }
        } catch (AuthenticationException ex) {
            return new AspectResult(AspectStatus.NOTOK, new AuthenticationException("SSO token is invalid", ex));
        }
        return AspectResult.OK;
    }

    @Override
    public AspectResult preRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preAddAspect(final AddAspectRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference container, final Capi3AspectPort capi3,
            final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preCreateTransaction(final CreateTransactionRequest request) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult prePrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {
        return authenticateRequest(request);
    }

    @Override
    public AspectResult preShutdown(final ShutdownRequest request) {
        return authenticateRequest(request);
    }
}
