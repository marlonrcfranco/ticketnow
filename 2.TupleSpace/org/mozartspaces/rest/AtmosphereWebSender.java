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

import static org.mozartspaces.rest.resources.ResourceConstants.CONTAINER;
import static org.mozartspaces.rest.resources.ResourceConstants.CONTAINERS;
import static org.mozartspaces.rest.resources.ResourceConstants.CONTAINERS_LOOKUP;
import static org.mozartspaces.rest.resources.ResourceConstants.DELETE;
import static org.mozartspaces.rest.resources.ResourceConstants.READ;
import static org.mozartspaces.rest.resources.ResourceConstants.TAKE;
import static org.mozartspaces.rest.resources.ResourceConstants.TEST;
import static org.mozartspaces.rest.resources.ResourceConstants.TRANSACTION;
import static org.mozartspaces.rest.resources.ResourceConstants.TRANSACTIONS;
import static org.mozartspaces.rest.resources.ResourceConstants.WRITE;
import static org.mozartspaces.rest.resources.ResourceConstants.X_ISOLATION_LEVEL;
import static org.mozartspaces.rest.resources.ResourceConstants.X_REQUEST_ID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;

import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.Message;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.Response;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.remote.MessageDistributor;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.EntriesRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.MetaModelRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.TransactionalRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.core.util.Serializer;
import org.mozartspaces.rest.resources.RESTContainerResponse;
import org.mozartspaces.rest.resources.ResourceConstants;
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * This sender relies on the destination-space for a Request being able to suspend http-requests.
 *
 * RequestMessage: a message sent directly to this space with another space set in the destinationSpace-property.
 *
 * ResponseMessage: responses to requests sent over the the "/messages" resource. those must be answered through that
 * same resource on the remote-server since we don't have an open HTTP-Connection in that case.
 *
 * where possible the HTTP-Response-Codes of the server are matched to their Response-Family so this code is more robust
 * against changes in the server code. (for example a DELETE sending 200 OK instead of 204 No Content because it sends
 * back an entity some implementations.
 *
 * @author Christian Proinger
 */
class AtmosphereWebSender extends SimpleWebSender {

    private static final String DEFAULT_SERIALIZER = "xstream-json";

    private static final Logger log = LoggerFactory.get();

    private final AsyncHttpClient asyncClient;

    /**
     * if no serializer can be negotiated with the remote space this one is used. (currently no negotiation is made)
     */
    private final Serializer defaultSerializer;

    private final class MyAsyncHandler implements AsyncHandler<Void> {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private Status code;
        private final RequestReference ref;
        private boolean aborted = false;
        private boolean readNotOkBody = false;

        public MyAsyncHandler(final RequestReference ref) {
            this.ref = ref;
        }

        @Override
        public void onThrowable(final Throwable t) {
            log.error("error with async-request", t);
            readNotOkBody = true;
            messageDistributor.distributeMessage(new ResponseMessage(ref, new GenericResponse<Serializable>(null, t),
                    null));
        }

        @Override
        public com.ning.http.client.AsyncHandler.STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart)
                throws Exception {
            synchronized (this) {
                byte[] bodyPartBytes = bodyPart.getBodyPartBytes();
                // when using json this is invoked with {"string":""} -> omit ->
                // gets avoided in XStreamProvider now.
                //
                // boolean emptyString = Arrays.equals(CONTINUE_BYTES,
                // bodyPartBytes);
                // if(!emptyString) {
                // } else {
                // }
                // log.info("bpb: " + new String(bodyPartBytes));
                baos.write(bodyPartBytes);
            }
            return STATE.CONTINUE;
        }

        @Override
        public com.ning.http.client.AsyncHandler.STATE onStatusReceived(final HttpResponseStatus responseStatus)
                throws Exception {
            code = ClientResponse.Status.fromStatusCode(responseStatus.getStatusCode());
            // by now the server should respond with successful as long as the
            // request
            // came through correctly. 100 continue would ok to.
            if (code.getFamily() == Family.INFORMATIONAL || code.getFamily() == Family.SUCCESSFUL) {
                return STATE.CONTINUE;
            } else if (code == ClientResponse.Status.NOT_FOUND) {
                aborted = true;
                readNotOkBody = true;
                return STATE.CONTINUE;
            } else {
                log.info("aborted due to status " + code);
                aborted = true;
                return STATE.ABORT;
            }
        }

        @Override
        public com.ning.http.client.AsyncHandler.STATE onHeadersReceived(final HttpResponseHeaders headers)
                throws Exception {
            return STATE.CONTINUE;
        }

        @Override
        public Void onCompleted() throws Exception {
            log.debug("onComplete()");
            byte[] byteArray = baos.toByteArray();
            if (aborted && !readNotOkBody) {
                // response may be rubbish, so don't bother
                // trying to deserializing it.
                return null;
            }

            log.debug("response: " + new String(byteArray, Charset.defaultCharset()));
            // this doesn't work because the deserializer only knows
            // message. i couldn't find out why:
            // maybe this can help:
            // http://download.oracle.com/javase/6/docs/api/javax/xml/bind/JAXBContext.html
            // it would be far better if we could use the
            // jersey-serialization-mechanism
            // which can handle such objects.
            // but this doesn't seem to be an easy task:
            // http://jersey.576304.n2.nabble.com/Resource-class-with-JAXB-and-XmlType-propOrder-td3409251.html

            Response<?> r = getSerializer(ref.getSpace()).deserialize(byteArray);
            ResponseMessage mess = new ResponseMessage(ref, r, null);
            messageDistributor.distributeMessage(mess);
            return null;
        }
    }

    /**
     * command pattern helper class.
     *
     * @author cproinger
     *
     */
    private abstract class SafeRequest {
        private final RequestMessage message;

        public SafeRequest(final RequestMessage message) {
            this.message = message;
        }

        public final void execute() {
            try {
                GenericResponse<? extends Serializable> gres = request();
                ResponseMessage responseMessage = new ResponseMessage(message.getRequestReference(), gres, null);
                messageDistributor.distributeMessage(responseMessage);
            } catch (ClientHandlerException e) {
                // connection excpetions, ...
                log.error("", e);
                // TODO retry
            }
        }

        public abstract GenericResponse<? extends Serializable> request();
    }

    /**
     * request-command helper class for requests with answerContainers the server just sends 202 accepted in the case
     * where all goes well.
     *
     * @author cproinger
     *
     */
    private abstract class SafeAnswerContainerRequest {
        private final RequestMessage message;

        public SafeAnswerContainerRequest(final RequestMessage message) {
            this.message = message;
        }

        public final void execute() {
            try {
                request();
            } catch (ClientHandlerException e) {
                log.error("", e);
                // TODO retry
            }
        }

        public abstract void request();
    }

    public AtmosphereWebSender(final MessageDistributor md, final Map<String, Serializer> serializers) {
        super(md, serializers);
        this.defaultSerializer = serializers.get(DEFAULT_SERIALIZER);
        AsyncHttpClientConfig cfg = new AsyncHttpClientConfig.Builder().setConnectionTimeoutInMs(0)
                .setRequestTimeoutInMs(-1).build();
        // TODO use transaction-timeout as timeout-value
        asyncClient = new AsyncHttpClient(cfg);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mozartspaces.web.SimpleWebSender#sendMessage(org.mozartspaces.core.Message)
     */
    @Override
    public void sendMessage(final Message<?> message) throws SerializationException {
        if (message instanceof ResponseMessage) {
            super.sendMessage(message);
        } else if (message instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) message;

            try {
                sendRequest(requestMessage);
            } catch (IOException e) {
                log.error("exception while sending request", e);
            }

        } else {
            throw new IllegalArgumentException("don't know how to handle " + message.getClass().getName());
        }
    }

    /**
     *
     * @param message
     * @throws SerializationException
     *             if the serializer couldn't serialize the object.
     * @throws IOException
     */
    private void sendRequest(final RequestMessage message) throws SerializationException, IOException {

        log.debug("making request through jersey/async-Client: " + message);

        final Request<?> request = message.getContent();
        UriBuilder uriBuilder = UriBuilder.fromUri(message.getDestinationSpace());
        // final org.xvsm.protocol.AbstractRequest preq =
        // MARSHALLER_HELPER.marshalRequest(request);
        // JAXBElement<AbstractRequest> jaxbe = new
        // JAXBElement<AbstractRequest>(new QName("request"), preq.getClass(),
        // preq);
        // GenericEntity<AbstractRequest> ar = new
        // GenericEntity<AbstractRequest>(preq, )
        // final JAXBElement<? extends AbstractRequest> jaxbe =
        // MARSHALLER_HELPER.marshalToJaxbElement(request);

        StringBuilder pathBuilder = new StringBuilder();
        ArrayList<Object> pathParams = new ArrayList<Object>(2);
        FluentCaseInsensitiveStringsMap headers = new FluentCaseInsensitiveStringsMap();
        addCommon(message, request, pathBuilder, pathParams, headers);

        if (request instanceof CreateTransactionRequest) {
            final WebResource res = jerseyClient.resource(uriBuilder.path(TRANSACTIONS).build());
            new SafeRequest(message) {
                @Override
                public GenericResponse<? extends Serializable> request() {
                    ClientResponse cres = res.entity(request).post(ClientResponse.class);
                    if (cres.getClientResponseStatus() == ClientResponse.Status.CREATED) {
                        // CREATED, Location = remoteserver/transactions/<id>
                        String location = cres.getHeaders().getFirst("Location");

                        log.debug("location=" + location);
                        TransactionReference txref = CoreUtils.parseTransactionReference(location);
                        return new GenericResponse<TransactionReference>(txref, null);
                    } else {
                        // something went wrong. on the server.
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else if (request instanceof CommitTransactionRequest) {
            CommitTransactionRequest commitTxReq = (CommitTransactionRequest) request;

            final WebResource res = jerseyClient.resource(uriBuilder.path(TRANSACTION).build(
                    commitTxReq.getTransaction().getId()));
            new SafeRequest(message) {

                @Override
                public GenericResponse<? extends Serializable> request() {
                    ClientResponse cres = res.entity(request).put(ClientResponse.class);
                    if (cres.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
                        return new GenericResponse<Nothing>(Nothing.INSTANCE, null);
                    } else {
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else if (request instanceof RollbackTransactionRequest) {
            RollbackTransactionRequest rollbTxReq = (RollbackTransactionRequest) request;
            final WebResource res = jerseyClient.resource(uriBuilder.path(TRANSACTION).build(
                    rollbTxReq.getTransaction().getId()));
            new SafeRequest(message) {

                @Override
                public GenericResponse<? extends Serializable> request() {
                    ClientResponse cres = res.delete(ClientResponse.class);
                    if (cres.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
                        return new GenericResponse<Serializable>(Nothing.INSTANCE, null);
                    } else {
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else if (request instanceof CreateContainerRequest) {
            final WebResource res = jerseyClient.resource(uriBuilder.path(CONTAINERS).build());
            res.accept(getMediaType());

            log.debug("createContainerRequest: " + res.getURI());

            new SafeRequest(message) {

                @Override
                public GenericResponse<? extends Serializable> request() {
                    // created, Location= someserver/containers/1
                    ClientResponse cres = res.entity(request).post(ClientResponse.class);
                    if (cres.getClientResponseStatus() == ClientResponse.Status.CREATED) {
                        ContainerReference cref = CoreUtils.parseContainerReference(cres.getHeaders().getFirst(
                                "Location"));
                        return new GenericResponse<ContainerReference>(cref, null);
                    } else {
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else if (request instanceof LookupContainerRequest) {
            LookupContainerRequest lcr = (LookupContainerRequest) request;
            final WebResource res = jerseyClient.resource(uriBuilder.path(CONTAINERS_LOOKUP)
                    .queryParam("name", lcr.getName()).build());
            new SafeRequest(message) {

                @Override
                public GenericResponse<? extends Serializable> request() {
                    ClientResponse cres = res.get(ClientResponse.class);
                    if (cres.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
                        RESTContainerResponse response = cres.getEntity(RESTContainerResponse.class);
                        ContainerReference cref = CoreUtils.parseContainerReference(response.getLink().toASCIIString());
                        return new GenericResponse<ContainerReference>(cref, null);
                    } else {
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else if (request instanceof DestroyContainerRequest) {
            DestroyContainerRequest dcr = (DestroyContainerRequest) request;
            final WebResource res = jerseyClient.resource(uriBuilder.path(CONTAINER).build(dcr.getContainer().getId()));
            new SafeRequest(message) {

                @Override
                public GenericResponse<? extends Serializable> request() {
                    ClientResponse cres = res.delete(ClientResponse.class);
                    if (cres.getClientResponseStatus().getFamily() == Family.SUCCESSFUL) {
                        return new GenericResponse<Serializable>(Nothing.INSTANCE, null);
                    } else {
                        return exceptionResponse(message, cres);
                    }
                }
            }.execute();
        } else {
            AnswerContainerInfo answerC = message.getAnswerContainerInfo();
            if (request instanceof TakeEntriesRequest) {
                if (answerC == null) {
                    executeAsyncRequest(message, uriBuilder, request, pathBuilder.append(TAKE), pathParams, headers);
                } else {
                    // answer-container was set.
                    pathParams.add(ResourceConstants.TAKE_OP);
                    executeAnswerContainerRequest(message, request, uriBuilder, pathParams, answerC);
                }
            } else if (request instanceof ReadEntriesRequest) {
                // ReadEntriesRequest<?> rer = (ReadEntriesRequest<?>) request;
                if (answerC == null) {
                    executeAsyncRequest(message, uriBuilder, request, pathBuilder.append(READ), pathParams, headers);
                } else {
                    // answer-container was set.
                    pathParams.add(ResourceConstants.READ_OP);
                    executeAnswerContainerRequest(message, request, uriBuilder, pathParams, answerC);
                }
            } else if (request instanceof TestEntriesRequest) {
                // ReadEntriesRequest<?> rer = (ReadEntriesRequest<?>) request;
                // answer-container not yet implemented for this operation.
                // if (answerC == null) {
                executeAsyncRequest(message, uriBuilder, request, pathBuilder.append(TEST), pathParams, headers);
                // } else {
                // // answer-container was set.
                // pathParams.add(ResourceConstants.TEST_OP);
                // executeAnswerContainerRequest(message, request, uriBuilder,
                // pathParams, answerC);
                // }
            } else if (request instanceof DeleteEntriesRequest) {
                executeAsyncRequest(message, uriBuilder, request, pathBuilder.append(DELETE), pathParams, headers);
            } else if (request instanceof WriteEntriesRequest) {
                executeAsyncRequest(message, uriBuilder, request, pathBuilder.append(WRITE), pathParams, headers);
            } else if (request instanceof MetaModelRequest) {
                metaModelRequest(message, uriBuilder, (MetaModelRequest) request, pathParams, headers);
            } else {
                // handle everything i did not get finished trough the
                // message-resource.
                super.sendMessage(message);
                // throw new
                // UnsupportedOperationException("not yet implemented to make a rest-request with: "
                // + request.getClass().getName());
            }
        }
    }

    private void executeAnswerContainerRequest(final RequestMessage message, final Request<?> request,
            final UriBuilder uriBuilder, final ArrayList<Object> pathParams, final AnswerContainerInfo answerC) {
        final WebResource res = jerseyClient.resource(uriBuilder.path(ResourceConstants.CONTAINER_ANSWER)
                .queryParam("destination", answerC.getContainer().toString()).build(pathParams.toArray()));
        new SafeAnswerContainerRequest(message) {

            @Override
            public void request() {
                ClientResponse cres = res.entity(request).post(ClientResponse.class);
                if (cres.getClientResponseStatus() != ClientResponse.Status.ACCEPTED) {
                    throw new WebApplicationException(cres.getStatus());
                }
            }
        }.execute();
    }

    /**
     * transforms the meta-path into the corresponding REST-Path and makes a request with that.
     *
     * @param message
     * @param uriBuilder
     * @param request
     * @param pathParams
     * @param headers
     * @throws SerializationException
     */
    private void metaModelRequest(final RequestMessage message, final UriBuilder uriBuilder,
            final MetaModelRequest request, final ArrayList<Object> pathParams,
            final FluentCaseInsensitiveStringsMap headers) throws SerializationException {
        final String metaPath = request.getPath();
        log.debug("request-path: " + metaPath);
        new SafeRequest(message) {

            @Override
            public GenericResponse<? extends Serializable> request() {
                ClientResponse cres = jerseyClient.resource(uriBuilder.path(metaPath).build())
                        .get(ClientResponse.class);
                if (cres.getClientResponseStatus() == ClientResponse.Status.OK) {
                    return new GenericResponse<Serializable>(cres.getEntity(Serializable.class), null);
                } else {
                    return exceptionResponse(message, cres);
                }
            }
        }.execute();

    }

    private void executeAsyncRequest(final RequestMessage message, final UriBuilder uriBuilder,
            final Request<?> request,
            // final JAXBElement<? extends AbstractRequest> jaxbe,
            final StringBuilder pathBuilder, final ArrayList<Object> pathParams,
            final FluentCaseInsensitiveStringsMap headers) throws SerializationException, IOException {

        URI uri = uriBuilder.path(pathBuilder.toString()).build(pathParams.toArray());
        byte[] serialize = getSerializer(message.getDestinationSpace()).serialize(request);

        log.debug("serialized: " + new String(serialize, Charset.defaultCharset()));

        BoundRequestBuilder areq = asyncClient.preparePost(uri.toASCIIString()).setHeaders(headers).setBody(serialize);
        // .setPerRequestConfig(new PerRequestConfig(null, -1))

        areq.execute(new MyAsyncHandler(message.getRequestReference()));
    }

    /**
     * adds path-parts for transaction and container if necessary.
     *
     * @param request
     * @param pathBuilder
     * @param pathParams
     * @param headers
     */
    private void addCommon(final RequestMessage message, final Request<?> request, final StringBuilder pathBuilder,
            final ArrayList<Object> pathParams, final FluentCaseInsensitiveStringsMap headers) {

        headers.add("Content-Type", getContentType(message.getDestinationSpace()));
        headers.add("Accept", getMediaType());
        String id = message.getRequestReference().toString();
        // log.info("id: " + id);
        headers.add(X_REQUEST_ID, id);
        if (request instanceof TransactionalRequest<?>) {
            TransactionalRequest<?> txRequest = (TransactionalRequest<?>) request;
            headers.add(X_ISOLATION_LEVEL, txRequest.getIsolation().toString());
            if (txRequest.getTransaction() != null) {
                pathBuilder.append(TRANSACTION);
                pathParams.add(txRequest.getTransaction().getId());
            }
            if (txRequest instanceof EntriesRequest<?>) {
                EntriesRequest<?> eRequest = (EntriesRequest<?>) txRequest;
                if (eRequest.getContainer() != null) {
                    pathBuilder.append(CONTAINER);
                    pathParams.add(eRequest.getContainer().getId());
                }
            }
        }
    }

    /**
     * currently no negotiation about the content type takes place.
     *
     * @param destinationSpace
     * @return
     */
    protected Serializer getSerializer(final URI destinationSpace) {
        return defaultSerializer;
    }

    /**
     * currently no negotiation about the content type takes place.
     *
     * @param destinationSpace
     * @return
     */
    protected String getContentType(final URI destinationSpace) {
        return MediaType.APPLICATION_JSON;
    }

    private GenericResponse<Serializable> exceptionResponse(final RequestMessage message, final ClientResponse cres) {
        Status status = cres.getClientResponseStatus();
        log.error("status: " + status);
        if (Arrays.asList(ClientResponse.Status.NOT_FOUND, ClientResponse.Status.CONFLICT, ClientResponse.Status.OK)
                .contains(status)) {
            try {
                @SuppressWarnings("unchecked")
                GenericResponse<Serializable> response = cres.getEntity(GenericResponse.class);

                return response;
            } catch (Exception e) {
                log.info("error deserilizing exception", e);
            }
        }
        WebApplicationException ex = new WebApplicationException(cres.getStatus());
        return new GenericResponse<Serializable>(null, new MzsCoreRuntimeException(ex));
        // ResponseMessage responseMessage = new
        // ResponseMessage(message.getRequestReference(), exres, null);
    }

    @Override
    public void shutdown(final boolean wait) {
        try {
            super.shutdown(wait);
            asyncClient.close();
        } catch (Exception e) {
            // prevent runtime-exceptions from escaping.
            log.warn("error shutting down jersey-client", e);
        }
    }
}
