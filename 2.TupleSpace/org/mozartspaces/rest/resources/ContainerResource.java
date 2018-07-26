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
package org.mozartspaces.rest.resources;

import java.io.Serializable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.atmosphere.annotation.Suspend;
import org.atmosphere.annotation.Suspend.SCOPE;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.jersey.Broadcastable;
import org.mozartspaces.capi3.ContainerLockedException;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestCallbackHandler;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest.Builder;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.core.ResourceContext;

/**
 * The ContainerResource handles all requests that affect a container, for long-polling requests the atmosphere
 * suspend-annotation is used. To be able to await proper suspension of these requests by the container a
 * AtmosphereResourceEventListener is used. It synchronizes over a latch with the callbackhandlers for these requests.
 *
 * @author Christian Proinger
 *
 */
@Path(ResourceConstants.CONTAINER2)
public final class ContainerResource extends MzsResource {

    private static final Logger log = LoggerFactory.get();

    /**
     * common code for long-polling requests is reused with this inner class.
     *
     * @author Christian Proinger
     *
     */
    private final class BroadcastEntryRequest implements RequestCallbackHandler<Request<Serializable>, Serializable> {
        private final Broadcaster bc;
        private final Broadcastable broadcastable;
        private final CountDownLatch latch;

        private BroadcastEntryRequest(final Broadcaster bc, final Broadcastable broadcastable,
                final CountDownLatch latch) {
            this.bc = bc;
            this.broadcastable = broadcastable;
            this.latch = latch;
        }

        private void awaitLatch() {
            try {
                // log.info("awaiting latch");
                latch.await();
                // log.info("latch awaited");
            } catch (InterruptedException e1) {
                log.error("waiting for latch failed", e1);
            }
        }

        @Override
        public void requestFailed(final Request<Serializable> request, final Throwable error) {
            // JAXBElement<?> response = MARSHALLER_HELPER.marshalToJaxbElement(error);
            // bc.broadcast(response);
            awaitLatch();
            bc.broadcast(new GenericResponse<Serializable>(null, error));
        }

        @Override
        public void requestProcessed(final Request<Serializable> request, final Serializable result) {

            awaitLatch();
            try {
                // log.info(bc.getID());
                bc.broadcast(new GenericResponse<Serializable>(result, null));
                // bc.destroy();
            } catch (IllegalStateException e) {
                log.error("", e);
            }
        }
    }

    /**
     * This class is used for synchronization of long-polling atmosphere-handled suspended requests. It counts down
     * CountDownLatches which are created at invocation time of long-polling-request-methods. The Callback-Handlers
     * await this count-down before they invoke the broadcast-method of the broadcaster to make sure it is in the proper
     * state
     *
     * (if the Broadcastable is not suspended at the time of the broadcast the message gets lost in this version of
     * atmosphere -> this may be fixed in later versions).
     *
     * @author Christian Proinger
     *
     */
    public static final class Listener implements AtmosphereResourceEventListener {

        @Override
        public void onBroadcast(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            // log.info("on broadcast: " + event);
        }

        @Override
        public void onDisconnect(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            log.info("on disconnect: " + event);
        }

        @Override
        public void onResume(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            // log.info("on resumed: " + event);
        }

        @Override
        public void onSuspend(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            // log.info("on suspend: " + event);
            Broadcaster bc = event.getResource().getBroadcaster();
            CountDownLatch l = latches.remove(bc);
            // log.info("suspend broadcaster " + bc.getID());
            if (l == null) {
                log.error("the latch is null for id: " + bc.getID());
                log.info("... ignore");
                throw new IllegalStateException(
                        "some broadcaster got suspended that wasn't used in the resource-method");
            } else {
                l.countDown();
            }
        }

        @Override
        public void onThrowable(final AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) {
            log.info("on throwable: " + event);
        }

    }

    /**
     * this makes the execution-stuff ~7% faster than normal threads.
     */
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * this is used to synchronize Callbackhandlers with the container so the Broadcasters broadcast-method is only
     * invoked when the container properly suspended the Broadcaster.
     */
    private static Map<Broadcaster, CountDownLatch> latches = Collections
            .synchronizedMap(new IdentityHashMap<Broadcaster, CountDownLatch>());

    /**
     * the container reference of this container resource.
     */
    private ContainerReference containerReference;

    /**
     * the id of this resource.
     */
    private final String id;

    private TransactionReference transactionReference;

    /**
     * constructs a ContainerResource. Notice that no fields are injected at this moment.
     *
     * @param id
     */
    public ContainerResource(@PathParam("cid") final String id) {
            log.debug("construced with id=" + id);
        this.id = id;
        // can't be called from here because no core is injected yet.
        // checkExists(id);
    }

    /**
     * this checks if the Container this resource links to exists.
     */
    @PostConstruct
    void checkExists() {
        ContainerReference cref = new ContainerReference(id, core.getConfig().getSpaceUri());
        containerReference = cref;

        try {
            // nur schauen ob mann dort hin-navigieren kann.
            core.send(MetaModelRequest.withPath(ResourceConstants.CONTAINERS + "/" + id).depth(0).build(), null)
                    .getResult();
        } catch (MzsCoreException e) {
            // konnte container nicht finden -> 404.
            // e.setStackTrace(new StackTraceElement[0]);
            throw new MzsApplicationException(Responses.notFound(), new InvalidContainerException());
            // throw new NotFoundException(uriInfo.getAbsolutePath());
        } catch (InterruptedException e) {
            log.error("", e);
            throw new WebApplicationException(500);
        }
    }

    /**
     * a long-polling delete-request. this request is suspended as long as the request cannot be satisfied. the first
     * broadcast (the result or a timeout-exception) resumes the request.
     *
     * an AtmosphereResourceEventListener is used to synchronize the broadcast with the container so the broadcast is
     * performed after the Broadcastable this method returns is suspended by the container.
     *
     * @param ader
     *            the DeleteEntriesRequst.
     * @param bc
     *            the broadcaster for this request.
     * @return a broadcastable to be handled by atmosphere.
     */
    @Path("/delete")
    @POST
    @Suspend(scope = SCOPE.REQUEST, resumeOnBroadcast = true, outputComments = false, listeners = Listener.class)
    public Broadcastable deleteEntries(final DeleteEntriesRequest ader,
            @HeaderParam(ResourceConstants.X_REQUEST_ID) final Broadcaster bc) {
        DeleteEntriesRequest ter = new DeleteEntriesRequest(containerReference, ader.getSelectors(), ader.getTimeout(),
                transactionReference, ader.getIsolation(), ader.getContext());
        Broadcastable broadcastable = new Broadcastable(bc);
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(bc, latch);
        executeOnCore(ter, new BroadcastEntryRequest(bc, broadcastable, latch));
        return broadcastable;
    }

    /**
     * destroys a container.
     *
     * @param iso
     *            the isolation-level from the header.
     * @return 204 No Content if the container could be destroyed.
     */
    @DELETE
    public Response destroyContainer(@HeaderParam("isolationLevel") final String iso) {
        try {
            Builder destroyCBuilder = DestroyContainerRequest.withContainer(containerReference);
            if (iso != null && iso.length() > 0) {
                destroyCBuilder.isolation(IsolationLevel.valueOf(iso));
            }
            core.send(destroyCBuilder.build(), null).getResult();
            return Response.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * executes a request in a way the atmosphere-framework doesn't run into a race-condition problem.
     *
     * @param rq
     * @param ch
     */
    private void executeOnCore(final Request<?> rq,
            final RequestCallbackHandler<? extends Request<?>, ? extends Serializable> ch) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Thread.yield();
                // try {
                // Thread.sleep(2,1);//i tried Thread.yield(); but that didn't work.
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
                core.send(rq, null, ch);
            }
        };
        executor.execute(r);
    }

    /**
     * subresource aspects. uris that look like
     * [transactions/transaction/<txId>]/containers/container/<cid>/aspects/aspect
     *
     * @param context
     *            is used to lookup the AspectResource.
     * @return an AspectResource
     */
    @Path(ResourceConstants.ASPECTS_ASPECT)
    public AspectsResource getAspectsResource(@Context final ResourceContext context) {
        AspectsResource ares = context.getResource(AspectsResource.class);
        ares.setContainerReference(containerReference);
        return ares;
    }

    /**
     * this is needed because @Path("{uri:.*}") doesn't match /containers/<id>.
     *
     * @return
     */
    @GET
    public Serializable getContainerInfo() {
        return getContainerInfo(null);
    }

    /**
     * this method matches any uri that starts with "/containers/<id>/.+" the the path-segments after that are injected
     * in the uri-param.
     */
    @GET
    @Path("{uri:.*}")
    public Serializable getContainerInfo(@PathParam("uri") final String uri) {
        // MultivaluedMap<String, String> segments = uriInfo.getPathParameters();
        // log.info("meta-request: " + uriInfo.getRequestUri());

        String path = "/containers/container/" + containerReference.getId();
        if (uri != null && uri.length() > 0) {
            path = path + "/" + uri;
        }
        MetaModelRequest req = MetaModelRequest.withPath(path).depth(1).build();
        try {
            return core.send(req, null).getResult();
        } catch (MzsCoreException e) {
            log.error("", e);
            throw new MzsApplicationException(Response.serverError(), e);
        } catch (InterruptedException e) {
            throw new WebApplicationException();
        }
    }

    /**
     * locks this container.
     *
     * @param iso
     *            the isolation-level from the header
     * @return 201 Created with a Location-Link the lock was created, otherwise (already locked) 200 OK with the
     *         application-Exception (ContainerLockedException).
     */
    @PUT
    @Path("/lock")
    public Response lockContainer(@HeaderParam("isolationLevel") final String iso) {
        org.mozartspaces.core.requests.LockContainerRequest.Builder builder = LockContainerRequest
                .withContainer(containerReference);
        if (iso != null && iso.length() > 0) {
            builder.isolation(IsolationLevel.valueOf(iso));
        }
        try {
            core.send(builder.build(), null).getResult();
            return Response.created(uriInfo.getRequestUri()).build();
        } catch (ContainerLockedException e) {
            throw new MzsApplicationException(Response.ok(), e);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * a long-polling delete-request. this request is suspended as long as the request cannot be satisfied. the first
     * broadcast (the result or a timeout-exception) resumes the request.
     *
     * an AtmosphereResourceEventListener is used to synchronize the broadcast with the container so the broadcast is
     * performed after the Broadcastable this method returns is suspended by the container.
     *
     * @param areq
     *            the ReadEntriesRequest.
     * @param bc
     *            the broadcaster for this request.
     * @return a broadcastable to be handled by atmosphere.
     */
    @Path("/read")
    @POST
    @Suspend(scope = SCOPE.REQUEST, resumeOnBroadcast = true, outputComments = true, listeners = Listener.class)
    public Broadcastable readEntries(final ReadEntriesRequest<Serializable> areq,
            @HeaderParam(ResourceConstants.X_REQUEST_ID) final Broadcaster bc) {
        ReadEntriesRequest<Serializable> rer = new ReadEntriesRequest<Serializable>(containerReference,
                areq.getSelectors(), areq.getTimeout(), transactionReference, areq.getIsolation(), areq.getContext());
        // log.info("rp" + rp);
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(bc, latch);
        Broadcastable broadcastable = new Broadcastable(null, bc);
        executeOnCore(rer, new BroadcastEntryRequest(bc, broadcastable, latch));
        return broadcastable;
    }

    /**
     * a read-entries request with an answer-container. the destination-container is taken from the query-parameter
     * "destination", the uri is in encoded form. so %2Fs have to be replaced with "/".
     *
     * currently only FIFO-Coordination is supported => TODO encode strategy in header or uri maybe.
     *
     * @param rer
     *            the ReadEntriesRequest
     * @param dest
     *            the destination-query-parameter
     * @return 202 Accepted.
     */
    @Path("/read/answer")
    @POST
    public Response readEntriesWithAnswerContainer(
    // @PathParam("containerId") String id,
            final ReadEntriesRequest<Serializable> rer,
            // @Context ResourceContext rc,
            @QueryParam("destination") final String dest) {
        // this can be any url like http://localhost/xcvcv.......
        ContainerReference cref = CoreUtils.parseContainerReference(dest.replace("%2F", "/"));

        core.send(rer, null, cref);
        return Response.status(Status.ACCEPTED).build();
    }

    /**
     * @param transactionReference
     *            the transaction which should be used for operations performed on this container.
     */
    public void setTransactionReference(final TransactionReference transactionReference) {
        this.transactionReference = transactionReference;
    }

    /**
     * a long-polling take-request. this request is suspended as long as the request cannot be satisfied. the first
     * broadcast (the result or a timeout-exception) resumes the request.
     *
     * an AtmosphereResourceEventListener is used to synchronize the broadcast with the container so the broadcast is
     * performed after the Broadcastable this method returns is suspended by the container.
     *
     * @param ater
     *            the DeleteEntriesRequst.
     * @param bc
     *            the broadcaster for this request.
     * @return a broadcastable to be handled by atmosphere.
     */
    @Path("/take")
    @POST
    @Suspend(scope = SCOPE.REQUEST, resumeOnBroadcast = true, outputComments = false, listeners = Listener.class)
    public Broadcastable takeEntries(final TakeEntriesRequest<Serializable> ater,
            @HeaderParam(ResourceConstants.X_REQUEST_ID) final Broadcaster bc) {
        TakeEntriesRequest<?> ter = new TakeEntriesRequest<Serializable>(containerReference, ater.getSelectors(),
                ater.getTimeout(), transactionReference, ater.getIsolation(), ater.getContext());
        Broadcastable broadcastable = new Broadcastable(bc);
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(bc, latch);
        executeOnCore(ter, new BroadcastEntryRequest(bc, broadcastable, latch));
        return broadcastable;
    }

    /**
     * a take-entries request with an answer-container. the destination-container is taken from the query-parameter
     * "destination", the uri is in encoded form. so %2Fs have to be replaced with "/".
     *
     * currently only FIFO-Coordination is supported => TODO encode strategy in header or uri maybe.
     *
     * @param ter
     *            the TakeEntriesRequest
     * @param dest
     *            the destination-query-parameter
     * @return 202 Accepted.
     */
    @Path("/take/answer")
    @POST
    public Response takeEntriesWithAnswerContainer(
    // @PathParam("containerId") String id,
            final TakeEntriesRequest<Serializable> ter,
            // @Context ResourceContext rc,
            @QueryParam("destination") final String dest) {
        // this can be any url like http://localhost/xcvcv.......
        ContainerReference cref = CoreUtils.parseContainerReference(dest.replace("%2F", "/"));

        // this uses a FifoCoordinator to write the entries into the answer container.
        // TODO support other strategies: both (key, and AnswerCoordinationKeyGenerationMethod)
        core.send(ter, null, cref);
        return Response.status(Status.ACCEPTED).build();
    }

    /**
     * a long-polling write-request. this request is suspended as long as the request cannot be satisfied. the first
     * broadcast (the result or a timeout-exception) resumes the request.
     *
     * an AtmosphereResourceEventListener is used to synchronize the broadcast with the container so the broadcast is
     * performed after the Broadcastable this method returns is suspended by the container.
     *
     * @param awer
     *            the WriteEntriesRequst.
     * @param bc
     *            the broadcaster for this request.
     * @return a broadcastable to be handled by atmosphere.
     */
    @Path("/write")
    @POST
    @Suspend(scope = SCOPE.REQUEST, resumeOnBroadcast = true, outputComments = false, listeners = Listener.class)
    public Broadcastable writeEntries(final WriteEntriesRequest awer,
    // JAXBElement<org.xvsm.protocol.WriteEntriesRequest> jaxbe,
    // if this parameter is not provided then a NullPointerException occurs in the atmosphere framework.
    // this is a bug in the framework: i created an issue for that: https://github.com/Atmosphere/atmosphere/issues/31
            @HeaderParam(ResourceConstants.X_REQUEST_ID) final Broadcaster bc) {
        // the Broadcaster has SCOPE = APPLICATION at this point, but it is set to REQUEST later.

        WriteEntriesRequest wer = new WriteEntriesRequest(awer.getEntries(), containerReference, awer.getTimeout(),
                transactionReference, awer.getIsolation(), awer.getContext());
        Broadcastable broadcastable = new Broadcastable(null, bc);
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(bc, latch);
        BroadcastEntryRequest callbackHandler = new BroadcastEntryRequest(bc, broadcastable, latch);
        executeOnCore(wer, callbackHandler);
        return broadcastable;
    }

    /**
     * a long-polling delete-request. this request is suspended as long as the request cannot be satisfied. the first
     * broadcast (the result or a timeout-exception) resumes the request.
     *
     * an AtmosphereResourceEventListener is used to synchronize the broadcast with the container so the broadcast is
     * performed after the Broadcastable this method returns is suspended by the container.
     *
     * @param ter
     *            the TestEntriesRequest.
     * @param bc
     *            the broadcaster for this request.
     * @return a broadcastable to be handled by atmosphere.
     */
    @Path("/test")
    @POST
    @Suspend(scope = SCOPE.REQUEST, resumeOnBroadcast = true, outputComments = true, listeners = Listener.class)
    public Broadcastable testEntries(final TestEntriesRequest ter,
            @HeaderParam(ResourceConstants.X_REQUEST_ID) final Broadcaster bc) {
        TestEntriesRequest rer = new TestEntriesRequest(containerReference, ter.getSelectors(), ter.getTimeout(),
                transactionReference, ter.getIsolation(), ter.getContext());
        // log.info("rp" + rp);
        CountDownLatch latch = new CountDownLatch(1);
        latches.put(bc, latch);
        Broadcastable broadcastable = new Broadcastable(null, bc);
        executeOnCore(rer, new BroadcastEntryRequest(bc, broadcastable, latch));
        return broadcastable;
    }
}
