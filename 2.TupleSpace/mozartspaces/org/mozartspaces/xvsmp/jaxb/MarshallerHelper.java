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
package org.mozartspaces.xvsmp.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnyData;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.FifoCoordinator.FifoData;
import org.mozartspaces.capi3.FifoCoordinator.FifoSelector;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeyData;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LabelCoordinator.LabelData;
import org.mozartspaces.capi3.LabelCoordinator.LabelSelector;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LifoCoordinator.LifoData;
import org.mozartspaces.capi3.LifoCoordinator.LifoSelector;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaData;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.QueryCoordinator.QueryData;
import org.mozartspaces.capi3.QueryCoordinator.QuerySelector;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.RandomCoordinator.RandomData;
import org.mozartspaces.capi3.RandomCoordinator.RandomSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.capi3.VectorCoordinator.VectorData;
import org.mozartspaces.capi3.VectorCoordinator.VectorSelector;
import org.mozartspaces.core.AbstractMessage;
import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.Response;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.requests.AbstractRequest;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.AspectRequest;
import org.mozartspaces.core.requests.ClearSpaceRequest;
import org.mozartspaces.core.requests.CommitTransactionRequest;
import org.mozartspaces.core.requests.CreateContainerRequest;
import org.mozartspaces.core.requests.CreateTransactionRequest;
import org.mozartspaces.core.requests.DeleteEntriesRequest;
import org.mozartspaces.core.requests.DestroyContainerRequest;
import org.mozartspaces.core.requests.EntriesRequest;
import org.mozartspaces.core.requests.LockContainerRequest;
import org.mozartspaces.core.requests.LookupContainerRequest;
import org.mozartspaces.core.requests.PrepareTransactionRequest;
import org.mozartspaces.core.requests.ReadEntriesRequest;
import org.mozartspaces.core.requests.RemoveAspectRequest;
import org.mozartspaces.core.requests.RollbackTransactionRequest;
import org.mozartspaces.core.requests.SelectingEntriesRequest;
import org.mozartspaces.core.requests.ShutdownRequest;
import org.mozartspaces.core.requests.TakeEntriesRequest;
import org.mozartspaces.core.requests.TestEntriesRequest;
import org.mozartspaces.core.requests.TransactionalRequest;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.util.LoggerFactory;
import org.mozartspaces.xvsmp.util.CoordinationDataFactory;
import org.mozartspaces.xvsmp.util.CoordinatorFactory;
import org.mozartspaces.xvsmp.util.ExceptionFactory;
import org.mozartspaces.xvsmp.util.MarshalFactories;
import org.mozartspaces.xvsmp.util.SelectorFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xvsm.protocol.AspectIPoint;
import org.xvsm.protocol.CustomCoordinationData;
import org.xvsm.protocol.CustomCoordinator;
import org.xvsm.protocol.CustomSelector;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomWriter;

/**
 * Marshals MozartSpaces messages to JAXB messages, that is, the classes that
 * are generated by XJC from the XVSMP schema.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class MarshallerHelper {

    private static final Logger log = LoggerFactory.get();

    private final ExceptionFactory exceptionFactory;
    private final CoordinatorFactory coordinatorFactory;
    private final CoordinationDataFactory coordinationDataFactory;
    private final SelectorFactory selectorFactory;

    // TODO lazy init? load with reflection?
    private final XStream xstream;
    private final ThreadLocal<DocumentBuilder> domDocumentBuilder;

    /**
     * Constructs a {@code MarshallerHelper}.
     *
     * @param factories
     *            provides references to the factories used for marshalling
     */
    public MarshallerHelper(final MarshalFactories factories) {
        exceptionFactory = factories.getExceptionFactory();
        coordinatorFactory = factories.getCoordinatorFactory();
        coordinationDataFactory = factories.getCoordinationDataFactory();
        selectorFactory = factories.getSelectorFactory();

        xstream = new XStream();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        domDocumentBuilder = new ThreadLocal<DocumentBuilder>() {
            @Override
            protected DocumentBuilder initialValue() {
                try {
                    return dbf.newDocumentBuilder();
                } catch (ParserConfigurationException ex) {
                    String message = "Could not create DocumentBuilder";
                    log.error(message, ex);
                    throw new MzsCoreRuntimeException(message, ex);
                }
            }
        };
    }

    /**
     * Marshals a MozartSpaces message to a JAXB message.
     *
     * @param message
     *            the MozartSpaces message to marshal
     * @return the message as JAXB message
     */
    public Object marshal(final Object message) {
        if (message instanceof RequestMessage) {
            return marshalRequestMessage((RequestMessage) message);
        }
        if (message instanceof ResponseMessage) {
            return marshalResponseMessage((ResponseMessage) message);
        }
        throw new IllegalArgumentException("Cannot marshal object of type " + message.getClass().getName());
    }

    private org.xvsm.protocol.RequestMessage marshalRequestMessage(final RequestMessage message) {
        org.xvsm.protocol.RequestMessage xmlMessage = new org.xvsm.protocol.RequestMessage();
        xmlMessage.setRequest(marshalRequest(message.getContent()));
        xmlMessage.setDestinationSpace(message.getDestinationSpace().toString());
        setCommonAbstractMessageFields(xmlMessage, message);
        return xmlMessage;
    }

    private org.xvsm.protocol.ResponseMessage marshalResponseMessage(final ResponseMessage message) {
        org.xvsm.protocol.ResponseMessage xmlMessage = new org.xvsm.protocol.ResponseMessage();
        xmlMessage.setResponse(marshalResponse(message.getContent()));
        setCommonAbstractMessageFields(xmlMessage, message);
        return xmlMessage;
    }

    private void setCommonAbstractMessageFields(final org.xvsm.protocol.AbstractMessage xmlMessage,
            final AbstractMessage<?> message) {
        xmlMessage.setRef(message.getRequestReference().getStringRepresentation());
        xmlMessage.setAnswerContainerInfo(marshalAnswerContainerInfo(message.getAnswerContainerInfo()));
    }

    private org.xvsm.protocol.AnswerContainerInfo marshalAnswerContainerInfo(
            final AnswerContainerInfo answerContainerInfo) {
        if (answerContainerInfo == null) {
            return null;
        }
        org.xvsm.protocol.AnswerContainerInfo aci = new org.xvsm.protocol.AnswerContainerInfo();
        aci.setContainer(answerContainerInfo.getContainer().toString());
        aci.setCoordinationKey(answerContainerInfo.getCoordinationKey());
        return aci;
    }

    private org.xvsm.protocol.AbstractRequest marshalRequest(final Request<?> request) {
        // entries requests
        if (request instanceof ReadEntriesRequest<?>) {
            return marshalReadEntriesRequest((ReadEntriesRequest<?>) request);
        }
        if (request instanceof TakeEntriesRequest<?>) {
            return marshalTakeEntriesRequest((TakeEntriesRequest<?>) request);
        }
        if (request instanceof WriteEntriesRequest) {
            return marshalWriteEntriesRequest((WriteEntriesRequest) request);
        }
        if (request instanceof TestEntriesRequest) {
            return marshalTestEntriesRequest((TestEntriesRequest) request);
        }
        if (request instanceof DeleteEntriesRequest) {
            return marshalDeleteEntriesRequest((DeleteEntriesRequest) request);
        }
        // transaction requests
        if (request instanceof CreateTransactionRequest) {
            return marshalCreateTransactionRequest((CreateTransactionRequest) request);
        }
        if (request instanceof CommitTransactionRequest) {
            return marshalCommitTransactionRequest((CommitTransactionRequest) request);
        }
        if (request instanceof RollbackTransactionRequest) {
            return marshalRollbackTransactionRequest((RollbackTransactionRequest) request);
        }
        if (request instanceof PrepareTransactionRequest) {
            return marshalPrepareTransactionRequest((PrepareTransactionRequest) request);
        }
        // container requests
        if (request instanceof CreateContainerRequest) {
            return marshalCreateContainerRequest((CreateContainerRequest) request);
        }
        if (request instanceof LookupContainerRequest) {
            return marshalLookupContainerRequest((LookupContainerRequest) request);
        }
        if (request instanceof DestroyContainerRequest) {
            return marshalDestroyContainerRequest((DestroyContainerRequest) request);
        }
        if (request instanceof LockContainerRequest) {
            return marshalLockContainerRequest((LockContainerRequest) request);
        }
        // aspect requests
        if (request instanceof AddAspectRequest) {
            return marshalAddAspectRequest((AddAspectRequest) request);
        }
        if (request instanceof RemoveAspectRequest) {
            return marshalRemoveAspectRequest((RemoveAspectRequest) request);
        }
        // other requests
        if (request instanceof ClearSpaceRequest) {
            return marshalClearSpaceRequest((ClearSpaceRequest) request);
        }
        if (request instanceof ShutdownRequest) {
            return marshalShutdownRequest((ShutdownRequest) request);
        }
        throw new IllegalArgumentException("Cannot marshal request of type " + request.getClass().getName());
    }

    private org.xvsm.protocol.ReadEntriesRequest marshalReadEntriesRequest(final ReadEntriesRequest<?> request) {
        org.xvsm.protocol.ReadEntriesRequest xmlRequest = new org.xvsm.protocol.ReadEntriesRequest();
        setCommonSelectingEntriesRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.TakeEntriesRequest marshalTakeEntriesRequest(final TakeEntriesRequest<?> request) {
        org.xvsm.protocol.TakeEntriesRequest xmlRequest = new org.xvsm.protocol.TakeEntriesRequest();
        setCommonSelectingEntriesRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.WriteEntriesRequest marshalWriteEntriesRequest(final WriteEntriesRequest request) {
        org.xvsm.protocol.WriteEntriesRequest xmlRequest = new org.xvsm.protocol.WriteEntriesRequest();
        xmlRequest.setEntries(marshalEntryList(request.getEntries()));
        setCommonEntriesRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.TestEntriesRequest marshalTestEntriesRequest(final TestEntriesRequest request) {
        org.xvsm.protocol.TestEntriesRequest xmlRequest = new org.xvsm.protocol.TestEntriesRequest();
        setCommonSelectingEntriesRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.DeleteEntriesRequest marshalDeleteEntriesRequest(final DeleteEntriesRequest request) {
        org.xvsm.protocol.DeleteEntriesRequest xmlRequest = new org.xvsm.protocol.DeleteEntriesRequest();
        setCommonSelectingEntriesRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.CreateTransactionRequest marshalCreateTransactionRequest(
            final CreateTransactionRequest request) {
        org.xvsm.protocol.CreateTransactionRequest xmlRequest = new org.xvsm.protocol.CreateTransactionRequest();
        xmlRequest.setTimeout(request.getTimeout());
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.CommitTransactionRequest marshalCommitTransactionRequest(
            final CommitTransactionRequest request) {
        org.xvsm.protocol.CommitTransactionRequest xmlRequest = new org.xvsm.protocol.CommitTransactionRequest();
        xmlRequest.setTransaction(request.getTransaction().getStringRepresentation());
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.RollbackTransactionRequest marshalRollbackTransactionRequest(
            final RollbackTransactionRequest request) {
        org.xvsm.protocol.RollbackTransactionRequest xmlRequest = new org.xvsm.protocol.RollbackTransactionRequest();
        xmlRequest.setTransaction(request.getTransaction().getStringRepresentation());
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.PrepareTransactionRequest marshalPrepareTransactionRequest(
            final PrepareTransactionRequest request) {
        org.xvsm.protocol.PrepareTransactionRequest xmlRequest = new org.xvsm.protocol.PrepareTransactionRequest();
        xmlRequest.setTransaction(request.getTransaction().getStringRepresentation());
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.CreateContainerRequest marshalCreateContainerRequest(
            final CreateContainerRequest request) {
        org.xvsm.protocol.CreateContainerRequest xmlRequest = new org.xvsm.protocol.CreateContainerRequest();
        xmlRequest.setName(request.getName());
        if (request.getSize() != Container.DEFAULT_SIZE) {
            xmlRequest.setSize(request.getSize());
        }
        xmlRequest.setObligatoryCoords(marshalCoordinatorList(request.getObligatoryCoords()));
        xmlRequest.setOptionalCoords(marshalCoordinatorList(request.getOptionalCoords()));
        xmlRequest.setForceInMemory(request.isForceInMemory());
        setCommonTransactionalRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.LookupContainerRequest marshalLookupContainerRequest(
            final LookupContainerRequest request) {
        org.xvsm.protocol.LookupContainerRequest xmlRequest = new org.xvsm.protocol.LookupContainerRequest();
        xmlRequest.setName(request.getName());
        xmlRequest.setTimeout(marshalRequestTimeout(request.getTimeout()));
        setCommonTransactionalRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.DestroyContainerRequest marshalDestroyContainerRequest(
            final DestroyContainerRequest request) {
        org.xvsm.protocol.DestroyContainerRequest xmlRequest = new org.xvsm.protocol.DestroyContainerRequest();
        xmlRequest.setContainer(request.getContainer().getStringRepresentation());
        setCommonTransactionalRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.LockContainerRequest marshalLockContainerRequest(final LockContainerRequest request) {
        org.xvsm.protocol.LockContainerRequest xmlRequest = new org.xvsm.protocol.LockContainerRequest();
        xmlRequest.setContainer(request.getContainer().getStringRepresentation());
        setCommonTransactionalRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.AddAspectRequest marshalAddAspectRequest(
            final AddAspectRequest request) {
        org.xvsm.protocol.AddAspectRequest xmlRequest = new org.xvsm.protocol.AddAspectRequest();
        xmlRequest.setAspect(marshalAspect(request.getAspect()));
        if (request.getContainer() != null) {
            xmlRequest.setContainer(request.getContainer().getStringRepresentation());
        }
        setCommonAspectRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.RemoveAspectRequest marshalRemoveAspectRequest(final RemoveAspectRequest request) {
        org.xvsm.protocol.RemoveAspectRequest xmlRequest = new org.xvsm.protocol.RemoveAspectRequest();
        xmlRequest.setAspect(request.getAspect().getStringRepresentation());
        setCommonAspectRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.ClearSpaceRequest marshalClearSpaceRequest(final ClearSpaceRequest request) {
        org.xvsm.protocol.ClearSpaceRequest xmlRequest = new org.xvsm.protocol.ClearSpaceRequest();
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private org.xvsm.protocol.ShutdownRequest marshalShutdownRequest(final ShutdownRequest request) {
        org.xvsm.protocol.ShutdownRequest xmlRequest = new org.xvsm.protocol.ShutdownRequest();
        setCommonAbstractRequestFields(xmlRequest, request);
        return xmlRequest;
    }

    private void setCommonSelectingEntriesRequestFields(final org.xvsm.protocol.SelectingEntriesRequest xmlRequest,
            final SelectingEntriesRequest<?> request) {
        xmlRequest.setSelectors(marshalSelectorList(request.getSelectors()));
        setCommonEntriesRequestFields(xmlRequest, request);
    }

    private void setCommonEntriesRequestFields(final org.xvsm.protocol.EntriesRequest xmlRequest,
            final EntriesRequest<?> request) {
        xmlRequest.setContainer(request.getContainer().getStringRepresentation());
        xmlRequest.setTimeout(marshalRequestTimeout(request.getTimeout()));
        setCommonTransactionalRequestFields(xmlRequest, request);
    }

    private void setCommonTransactionalRequestFields(final org.xvsm.protocol.TransactionalRequest xmlRequest,
            final TransactionalRequest<?> request) {
        if (request.getTransaction() != null) {
            xmlRequest.setTransaction(request.getTransaction().getStringRepresentation());
        }
        xmlRequest.setIsolation(marshalIsolationLevel(request.getIsolation()));
        setCommonAbstractRequestFields(xmlRequest, request);
    }

    private void setCommonAspectRequestFields(final org.xvsm.protocol.AspectRequest xmlRequest,
            final AspectRequest<?> request) {
        xmlRequest.setIpoints(marshalAspectIPoints(request.getIPoints()));
        setCommonTransactionalRequestFields(xmlRequest, request);
    }

    private org.xvsm.protocol.AspectIPointList marshalAspectIPoints(final Set<? extends InterceptionPoint> iPoints) {
        if (iPoints == null) {
            return null;
        }
        org.xvsm.protocol.AspectIPointList xmlIPoints = new org.xvsm.protocol.AspectIPointList();
        for (InterceptionPoint point : iPoints) {
            if (point instanceof SpaceIPoint) {
                SpaceIPoint sip = (SpaceIPoint) point;
                xmlIPoints.getIpoints().add(marshalSpaceAspectIPoint(sip));
            } else {
                ContainerIPoint cip = (ContainerIPoint) point;
                xmlIPoints.getIpoints().add(marshalContainerAspectIPoint(cip));
            }
        }
        return xmlIPoints;
    }

    private AspectIPoint marshalSpaceAspectIPoint(final SpaceIPoint ipoint) {
        switch (ipoint) {
        case POST_ADD_ASPECT:
            return AspectIPoint.POST_ADD_ASPECT;
        case POST_COMMIT_TRANSACTION:
            return AspectIPoint.POST_COMMIT_TRANSACTION;
        case POST_CREATE_CONTAINER:
            return AspectIPoint.POST_CREATE_CONTAINER;
        case POST_CREATE_TRANSACTION:
            return AspectIPoint.POST_CREATE_TRANSACTION;
        case POST_DELETE:
            return AspectIPoint.POST_DELETE;
        case POST_DESTROY_CONTAINER:
            return AspectIPoint.POST_DESTROY_CONTAINER;
        case POST_LOCK_CONTAINER:
            return AspectIPoint.POST_LOCK_CONTAINER;
        case POST_LOOKUP_CONTAINER:
            return AspectIPoint.POST_LOOKUP_CONTAINER;
        case POST_PREPARE_TRANSACTION:
            return AspectIPoint.POST_PREPARE_TRANSACTION;
        case POST_READ:
            return AspectIPoint.POST_READ;
        case POST_REMOVE_ASPECT:
            return AspectIPoint.POST_REMOVE_ASPECT;
        case POST_ROLLBACK_TRANSACTION:
            return AspectIPoint.POST_ROLLBACK_TRANSACTION;
        case POST_TAKE:
            return AspectIPoint.POST_TAKE;
        case POST_TEST:
            return AspectIPoint.POST_TEST;
        case POST_WRITE:
            return AspectIPoint.POST_WRITE;
        case PRE_ADD_ASPECT:
            return AspectIPoint.PRE_ADD_ASPECT;
        case PRE_COMMIT_TRANSACTION:
            return AspectIPoint.PRE_COMMIT_TRANSACTION;
        case PRE_CREATE_CONTAINER:
            return AspectIPoint.PRE_CREATE_CONTAINER;
        case PRE_CREATE_TRANSACTION:
            return AspectIPoint.PRE_CREATE_TRANSACTION;
        case PRE_DELETE:
            return AspectIPoint.PRE_DELETE;
        case PRE_DESTROY_CONTAINER:
            return AspectIPoint.PRE_DESTROY_CONTAINER;
        case PRE_LOCK_CONTAINER:
            return AspectIPoint.PRE_LOCK_CONTAINER;
        case PRE_LOOKUP_CONTAINER:
            return AspectIPoint.PRE_LOOKUP_CONTAINER;
        case PRE_PREPARE_TRANSACTION:
            return AspectIPoint.PRE_PREPARE_TRANSACTION;
        case PRE_READ:
            return AspectIPoint.PRE_READ;
        case PRE_REMOVE_ASPECT:
            return AspectIPoint.PRE_REMOVE_ASPECT;
        case PRE_ROLLBACK_TRANSACTION:
            return AspectIPoint.PRE_ROLLBACK_TRANSACTION;
        case PRE_SHUTDOWN:
            return AspectIPoint.PRE_SHUTDOWN;
        case PRE_TAKE:
            return AspectIPoint.PRE_TAKE;
        case PRE_TEST:
            return AspectIPoint.PRE_TEST;
        case PRE_WRITE:
            return AspectIPoint.PRE_WRITE;
        default:
            throw new IllegalArgumentException(ipoint.toString());
        }
    }

    private AspectIPoint marshalContainerAspectIPoint(final ContainerIPoint ipoint) {
        switch (ipoint) {
        case POST_ADD_ASPECT:
            return AspectIPoint.POST_ADD_ASPECT;
        case POST_DELETE:
            return AspectIPoint.POST_DELETE;
        case POST_DESTROY_CONTAINER:
            return AspectIPoint.POST_DESTROY_CONTAINER;
        case POST_LOCK_CONTAINER:
            return AspectIPoint.POST_LOCK_CONTAINER;
        case POST_LOOKUP_CONTAINER:
            return AspectIPoint.POST_LOOKUP_CONTAINER;
        case POST_READ:
            return AspectIPoint.POST_READ;
        case POST_REMOVE_ASPECT:
            return AspectIPoint.POST_REMOVE_ASPECT;
        case POST_TAKE:
            return AspectIPoint.POST_TAKE;
        case POST_TEST:
            return AspectIPoint.POST_TEST;
        case POST_WRITE:
            return AspectIPoint.POST_WRITE;
        case PRE_ADD_ASPECT:
            return AspectIPoint.PRE_ADD_ASPECT;
        case PRE_DELETE:
            return AspectIPoint.PRE_DELETE;
        case PRE_DESTROY_CONTAINER:
            return AspectIPoint.PRE_DESTROY_CONTAINER;
        case PRE_LOCK_CONTAINER:
            return AspectIPoint.PRE_LOCK_CONTAINER;
        case PRE_READ:
            return AspectIPoint.PRE_READ;
        case PRE_REMOVE_ASPECT:
            return AspectIPoint.PRE_REMOVE_ASPECT;
        case PRE_TAKE:
            return AspectIPoint.PRE_TAKE;
        case PRE_TEST:
            return AspectIPoint.PRE_TEST;
        case PRE_WRITE:
            return AspectIPoint.PRE_WRITE;
        default:
            throw new IllegalArgumentException(ipoint.toString());
        }
    }

    private void setCommonAbstractRequestFields(final org.xvsm.protocol.AbstractRequest xmlRequest,
            final AbstractRequest<?> request) {
        xmlRequest.setContext(marshalRequestContext(request.getContext()));
    }

    private org.xvsm.protocol.AbstractResponse marshalResponse(final Response<?> response) {
        if (response instanceof GenericResponse<?>) {
            if (response.getResult() != null) {
                return marshalResultResponse(response.getResult());
            } else {
                org.xvsm.protocol.EmptyResponse xmlResponse = new org.xvsm.protocol.EmptyResponse();
                xmlResponse.setError(marshalErrorResponse(response.getError()));
                return xmlResponse;
            }
        }
        throw new IllegalArgumentException("Cannot marshal response of type " + response.getClass().getName());
    }

    private org.xvsm.protocol.Exception marshalErrorResponse(final Throwable error) {
        org.xvsm.protocol.Exception xmlError = new org.xvsm.protocol.Exception();
        // TODO add general (runtime) exceptions
        xmlError.setType(exceptionFactory.getTypeName((Exception) error));
        xmlError.setMessage(error.getMessage());
        return xmlError;
    }

    @SuppressWarnings("unchecked")
    private org.xvsm.protocol.AbstractResponse marshalResultResponse(final Object result) {
        if (result instanceof Nothing) {
            return new org.xvsm.protocol.EmptyResponse();
        }
        if (result instanceof ArrayList<?>) {
            return marshalEntryResponse((ArrayList<? extends Serializable>) result);
        }
        if (result instanceof Integer) {
            org.xvsm.protocol.IntegerResponse count = new org.xvsm.protocol.IntegerResponse();
            count.setResult((Integer) result);
            return count;
        }
        if (result instanceof TransactionReference) {
            org.xvsm.protocol.TransactionResponse reference = new org.xvsm.protocol.TransactionResponse();
            reference.setResult(((TransactionReference) result).getStringRepresentation());
            return reference;
        }
        if (result instanceof ContainerReference) {
            org.xvsm.protocol.ContainerResponse reference = new org.xvsm.protocol.ContainerResponse();
            reference.setResult(((ContainerReference) result).getStringRepresentation());
            return reference;
        }
        if (result instanceof AspectReference) {
            org.xvsm.protocol.AspectResponse reference = new org.xvsm.protocol.AspectResponse();
            reference.setResult(((AspectReference) result).getStringRepresentation());
            return reference;
        }
        throw new IllegalArgumentException("Cannot marshal result of type " + result.getClass().getName());
    }

    private org.xvsm.protocol.AbstractResponse marshalEntryResponse(final ArrayList<? extends Serializable> result) {
        org.xvsm.protocol.EntryResponse xmlResponse = new org.xvsm.protocol.EntryResponse();
        org.xvsm.protocol.EntryValueList entryValueList = new org.xvsm.protocol.EntryValueList();
        for (Serializable value : result) {
            entryValueList.getValues().add(marshalEntryValue(value));
        }
        xmlResponse.setValues(entryValueList);
        return xmlResponse;
    }

    private org.xvsm.protocol.EntryList marshalEntryList(final List<Entry> entries) {
        org.xvsm.protocol.EntryList entryList = new org.xvsm.protocol.EntryList();
        for (Entry entry : entries) {
            org.xvsm.protocol.Entry xmlEntry = new org.xvsm.protocol.Entry();
            xmlEntry.setCoordData(marshalCoordinationDataList(entry.getCoordinationData()));
            xmlEntry.setValue(marshalEntryValue(entry.getValue()));
            entryList.getEntries().add(xmlEntry);
        }
        return entryList;
    }

    private org.xvsm.protocol.EntryValue marshalEntryValue(final Serializable value) {
        // TODO move somewhere else
        org.xvsm.protocol.EntryValue xmlValue = new org.xvsm.protocol.EntryValue();
        xmlValue.setAny(marshalAnyDom(value));
        return xmlValue;
    }

    private Element marshalAnyDom(final Serializable value) {
        // TODO reuse Document, DomWriter?
        Document domDocument = domDocumentBuilder.get().newDocument();
        DomWriter domWriter = new DomWriter(domDocument);
        // TODO at least reuse XmlFriendlyReplacer (see constructor of
        // DomWriter, also see http://jira.codehaus.org/browse/XSTR-584)
        xstream.marshal(value, domWriter);
        return domDocument.getDocumentElement();
    }

    private org.xvsm.protocol.CoordinatorList marshalCoordinatorList(final List<? extends Coordinator> coordinators) {
        if (coordinators == null || coordinators.isEmpty()) {
            return null;
        }
        org.xvsm.protocol.CoordinatorList coordinatorList = new org.xvsm.protocol.CoordinatorList();
        for (Coordinator coord : coordinators) {
            String type = coordinatorFactory.getTypeName(coord);
            org.xvsm.protocol.Coordinator xmlCoord;
            if (coord instanceof AnyCoordinator) {
                xmlCoord = new org.xvsm.protocol.AnyCoordinator();
            } else if (coord instanceof FifoCoordinator) {
                xmlCoord = new org.xvsm.protocol.FifoCoordinator();
            } else if (coord instanceof KeyCoordinator) {
                xmlCoord = new org.xvsm.protocol.KeyCoordinator();
            } else if (coord instanceof LabelCoordinator) {
                xmlCoord = new org.xvsm.protocol.LabelCoordinator();
            } else if (coord instanceof LifoCoordinator) {
                xmlCoord = new org.xvsm.protocol.LifoCoordinator();
            } else if (coord instanceof LindaCoordinator) {
                org.xvsm.protocol.LindaCoordinator lindaCoord = new org.xvsm.protocol.LindaCoordinator();
                xmlCoord = lindaCoord;
                lindaCoord.setOnlyAnnotatedEntries(((LindaCoordinator) coord).isOnlyAnnotatedEntries());
            } else if (coord instanceof QueryCoordinator) {
                xmlCoord = new org.xvsm.protocol.QueryCoordinator();
            } else if (coord instanceof RandomCoordinator) {
                xmlCoord = new org.xvsm.protocol.RandomCoordinator();
            } else if (coord instanceof VectorCoordinator) {
                xmlCoord = new org.xvsm.protocol.VectorCoordinator();
            } else if (coord instanceof CustomCoordinator) {
                xmlCoord = new org.xvsm.protocol.CustomCoordinator();
                ((CustomCoordinator) xmlCoord).setType(type);
                // TODO marshal additional coordinator properties
            } else {
                throw new IllegalArgumentException("Cannot marshal coordinator of type " + coord.getClass().getName());
            }

            if (coord.getName() != null && !coord.getName().equals(type)) {
                xmlCoord.setName(coord.getName());
            } // else: do not marshal default name

            coordinatorList.getCoordinators().add(xmlCoord);
        }
        return coordinatorList;
    }

    private org.xvsm.protocol.CoordinationDataList marshalCoordinationDataList(
            final Collection<? extends CoordinationData> coordData) {
        org.xvsm.protocol.CoordinationDataList coordDataList = new org.xvsm.protocol.CoordinationDataList();
        for (CoordinationData data : coordData) {
            String type = coordinationDataFactory.getTypeName(data);
            org.xvsm.protocol.CoordinationData xmlData;
            // TODO omit data for implicit coordinators
            if (data instanceof AnyData) {
                xmlData = new org.xvsm.protocol.AnyData();
            } else if (data instanceof FifoData) {
                xmlData = new org.xvsm.protocol.FifoData();
            } else if (data instanceof KeyData) {
                xmlData = new org.xvsm.protocol.KeyData();
                ((org.xvsm.protocol.KeyData) xmlData).setKey(((KeyData) data).getKey());
            } else if (data instanceof LabelData) {
                xmlData = new org.xvsm.protocol.LabelData();
                ((org.xvsm.protocol.LabelData) xmlData).setLabel(((LabelData) data).getLabel());
            } else if (data instanceof LifoData) {
                xmlData = new org.xvsm.protocol.LifoData();
            } else if (data instanceof LindaData) {
                xmlData = new org.xvsm.protocol.LindaData();
            } else if (data instanceof QueryData) {
                xmlData = new org.xvsm.protocol.QueryData();
            } else if (data instanceof RandomData) {
                xmlData = new org.xvsm.protocol.RandomData();
            } else if (data instanceof VectorData) {
                xmlData = new org.xvsm.protocol.VectorData();
                ((org.xvsm.protocol.VectorData) xmlData).setIndex(((VectorData) data).getIndex());
            } else if (data instanceof CustomCoordinationData) {
                xmlData = new org.xvsm.protocol.CustomCoordinationData();
                ((CustomCoordinationData) xmlData).setType(type);
                // TODO marshal additional properties
            } else {
                throw new IllegalArgumentException("Cannot marshal coordination data of type "
                        + data.getClass().getName());
            }

            if (data.getName() != null && !data.getName().equals(type)) {
                xmlData.setName(data.getName());
            } // else: do not marshal default name

            coordDataList.getCoordDatas().add(xmlData);
        }
        return coordDataList;
    }

    private org.xvsm.protocol.SelectorList marshalSelectorList(final List<? extends Selector> selectors) {
        org.xvsm.protocol.SelectorList selectorList = new org.xvsm.protocol.SelectorList();
        for (Selector sel : selectors) {
            String type = selectorFactory.getTypeName(sel);
            org.xvsm.protocol.Selector xmlSel;
            if (sel instanceof AnySelector) {
                xmlSel = new org.xvsm.protocol.AnySelector();
            } else if (sel instanceof FifoSelector) {
                xmlSel = new org.xvsm.protocol.FifoSelector();
            } else if (sel instanceof KeySelector) {
                xmlSel = new org.xvsm.protocol.KeySelector();
                ((org.xvsm.protocol.KeySelector) xmlSel).setKey(((KeySelector) sel).getKey());
            } else if (sel instanceof LabelSelector) {
                xmlSel = new org.xvsm.protocol.LabelSelector();
                ((org.xvsm.protocol.LabelSelector) xmlSel).setLabel(((LabelSelector) sel).getLabel());
            } else if (sel instanceof LifoSelector) {
                xmlSel = new org.xvsm.protocol.LifoSelector();
            } else if (sel instanceof LindaSelector) {
                xmlSel = new org.xvsm.protocol.LindaSelector();
                org.xvsm.protocol.EntryValue value = marshalEntryValue(((LindaSelector) sel).getTemplate());
                ((org.xvsm.protocol.LindaSelector) xmlSel).setTemplate(value);
            } else if (sel instanceof QuerySelector) {
                xmlSel = new org.xvsm.protocol.QuerySelector();
                org.xvsm.protocol.Query query = marshalQuery(((QuerySelector) sel).getQuery());
                ((org.xvsm.protocol.QuerySelector) xmlSel).setQuery(query);
            } else if (sel instanceof RandomSelector) {
                xmlSel = new org.xvsm.protocol.RandomSelector();
            } else if (sel instanceof VectorSelector) {
                xmlSel = new org.xvsm.protocol.VectorSelector();
                ((org.xvsm.protocol.VectorSelector) xmlSel).setIndex(((VectorSelector) sel).getIndex());
            } else if (sel instanceof CustomSelector) {
                xmlSel = new org.xvsm.protocol.CustomSelector();
                ((CustomSelector) xmlSel).setType(type);
                // TODO marshal additional properties
            } else {
                throw new IllegalArgumentException("Cannot marshal selector of type " + sel.getClass().getName());
            }

            if (sel.getCount() != 1) {
                xmlSel.setCount(sel.getCount());
            } // else: do not marshal default count
            if (sel.getName() != null && !sel.getName().equals(type)) {
                xmlSel.setName(sel.getName());
            } // else: do not marshal default name

            selectorList.getSelectors().add(xmlSel);
        }
        return selectorList;
    }

    private org.xvsm.protocol.Query marshalQuery(final Query query) {
        // TODO marshal query
        return null;
    }

    private Long marshalRequestTimeout(final long timeout) {
        if (timeout == RequestTimeout.DEFAULT) {
            return null;
        }
        return timeout;
    }

    private org.xvsm.protocol.IsolationLevel marshalIsolationLevel(final IsolationLevel isolation) {
        if (isolation == null || isolation == MzsConstants.DEFAULT_ISOLATION) {
            return null;
        }
        switch (isolation) {
        case READ_COMMITTED:
            return org.xvsm.protocol.IsolationLevel.READ_COMMITTED;
        case REPEATABLE_READ:
            return org.xvsm.protocol.IsolationLevel.REPEATABLE_READ;
        default:
            throw new IllegalArgumentException(isolation.toString());
        }
    }

    private org.xvsm.protocol.Aspect marshalAspect(final ContainerAspect aspect) {
        org.xvsm.protocol.Aspect xmlAspect = new org.xvsm.protocol.Aspect();
        xmlAspect.setAny(marshalAnyDom(aspect));
        return xmlAspect;
    }

    private org.xvsm.protocol.PropertyList marshalRequestContext(final RequestContext context) {
        if (context == null) {
            return null;
        }
        org.xvsm.protocol.PropertyList xmlProperties = new org.xvsm.protocol.PropertyList();
        for (String key : context.getPropertyKeys()) {
            org.xvsm.protocol.Property xmlProperty = new org.xvsm.protocol.Property();
            xmlProperty.setKey(key);
            xmlProperty.setAny(marshalAnyDom((Serializable) context.getProperty(key)));
            xmlProperties.getProperties().add(xmlProperty);
        }
        return xmlProperties;
    }

}
