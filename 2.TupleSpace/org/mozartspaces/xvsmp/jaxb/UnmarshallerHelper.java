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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.Query;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.Request;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.Response;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.InterceptionPoint;
import org.mozartspaces.core.aspects.SpaceIPoint;
import org.mozartspaces.core.authorization.AuthorizationLevel;
import org.mozartspaces.core.requests.AddAspectRequest;
import org.mozartspaces.core.requests.ClearSpaceRequest;
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
import org.mozartspaces.core.util.CoreUtils;
import org.mozartspaces.core.util.Nothing;
import org.mozartspaces.xvsmp.util.CoordinationDataFactory;
import org.mozartspaces.xvsmp.util.CoordinatorFactory;
import org.mozartspaces.xvsmp.util.ExceptionFactory;
import org.mozartspaces.xvsmp.util.MarshalFactories;
import org.mozartspaces.xvsmp.util.SelectorFactory;
import org.w3c.dom.Element;
import org.xvsm.protocol.AspectIPoint;
import org.xvsm.protocol.AspectIPointList;
import org.xvsm.protocol.CustomCoordinationData;
import org.xvsm.protocol.CustomSelector;
import org.xvsm.protocol.EntryValue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;

/**
 * Marshals JAXB message to MozartSpaces messages.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class UnmarshallerHelper {

    private final ExceptionFactory exceptionFactory;
    private final CoordinatorFactory coordinatorFactory;
    private final CoordinationDataFactory coordinationDataFactory;
    private final SelectorFactory selectorFactory;

    // TODO lazy init? load with reflection?
    private final XStream xstream;

    /**
     * Constructs an {@code UnmarshallerHelper}.
     *
     * @param factories
     *            provides references to the factories used for unmarshalling
     */
    public UnmarshallerHelper(final MarshalFactories factories) {
        exceptionFactory = factories.getExceptionFactory();
        coordinatorFactory = factories.getCoordinatorFactory();
        coordinationDataFactory = factories.getCoordinationDataFactory();
        selectorFactory = factories.getSelectorFactory();

        xstream = new XStream();
    }

    /**
     * Unmarshals a JAXB message to a MozartSpaces message.
     *
     * @param xmlMessage
     *            the JAXB message to unmarshal
     * @return the message as MozartSpaces message
     */
    public Object unmarshal(final Object xmlMessage) {
        if (xmlMessage instanceof org.xvsm.protocol.RequestMessage) {
            return unmarshalRequestMessage((org.xvsm.protocol.RequestMessage) xmlMessage);
        }
        if (xmlMessage instanceof org.xvsm.protocol.ResponseMessage) {
            return unmarshalResponseMessage((org.xvsm.protocol.ResponseMessage) xmlMessage);
        }
        throw new IllegalArgumentException("Cannot unmarshal object of type " + xmlMessage.getClass().getName());
    }

    private RequestMessage unmarshalRequestMessage(final org.xvsm.protocol.RequestMessage xmlMessage) {
        RequestReference requestRef = CoreUtils.parseRequestReferenceWithoutPrefix(xmlMessage.getRef());
        Request<?> request = unmarshalRequest(xmlMessage.getRequest());
        URI space = URI.create(xmlMessage.getDestinationSpace());
        AnswerContainerInfo aci = unmarshalAnswerContainerInfo(xmlMessage.getAnswerContainerInfo());
        return new RequestMessage(requestRef, request, space, aci);
    }

    private ResponseMessage unmarshalResponseMessage(final org.xvsm.protocol.ResponseMessage xmlMessage) {
        RequestReference requestRef = CoreUtils.parseRequestReferenceWithoutPrefix(xmlMessage.getRef());
        Response<?> response = unmarshalResponse(xmlMessage.getResponse());
        AnswerContainerInfo aci = unmarshalAnswerContainerInfo(xmlMessage.getAnswerContainerInfo());
        return new ResponseMessage(requestRef, response, aci);
    }

    private AnswerContainerInfo unmarshalAnswerContainerInfo(
            final org.xvsm.protocol.AnswerContainerInfo answerContainerInfo) {
        if (answerContainerInfo == null) {
            return null;
        }
        ContainerReference container = CoreUtils.parseContainerReference(answerContainerInfo.getContainer());
        return new AnswerContainerInfo(container, answerContainerInfo.getCoordinationKey());
    }

    private Request<?> unmarshalRequest(final org.xvsm.protocol.AbstractRequest xmlRequest) {
        // entries requests
        if (xmlRequest instanceof org.xvsm.protocol.ReadEntriesRequest) {
            return unmarshalReadEntriesRequest((org.xvsm.protocol.ReadEntriesRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.TakeEntriesRequest) {
            return unmarshalTakeEntriesRequest((org.xvsm.protocol.TakeEntriesRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.WriteEntriesRequest) {
            return unmarshalWriteEntriesRequest((org.xvsm.protocol.WriteEntriesRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.TestEntriesRequest) {
            return unmarshalTestEntriesRequest((org.xvsm.protocol.TestEntriesRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.DeleteEntriesRequest) {
            return unmarshalDeleteEntriesRequest((org.xvsm.protocol.DeleteEntriesRequest) xmlRequest);
        }
        // transaction requests
        if (xmlRequest instanceof org.xvsm.protocol.CreateTransactionRequest) {
            return unmarshalCreateTransactionRequest((org.xvsm.protocol.CreateTransactionRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.CommitTransactionRequest) {
            return unmarshalCommitTransactionRequest((org.xvsm.protocol.CommitTransactionRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.RollbackTransactionRequest) {
            return unmarshalRollbackTransactionRequest((org.xvsm.protocol.RollbackTransactionRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.PrepareTransactionRequest) {
            return unmarshalPrepareTransactionRequest((org.xvsm.protocol.PrepareTransactionRequest) xmlRequest);
        }
        // container requests
        if (xmlRequest instanceof org.xvsm.protocol.CreateContainerRequest) {
            return unmarshalCreateContainerRequest((org.xvsm.protocol.CreateContainerRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.LookupContainerRequest) {
            return unmarshalLookupContainerRequest((org.xvsm.protocol.LookupContainerRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.DestroyContainerRequest) {
            return unmarshalDestroyContainerRequest((org.xvsm.protocol.DestroyContainerRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.LockContainerRequest) {
            return unmarshalLockContainerRequest((org.xvsm.protocol.LockContainerRequest) xmlRequest);
        }
        // aspect requests
        if (xmlRequest instanceof org.xvsm.protocol.AddAspectRequest) {
            return unmarshalAddAspectRequest((org.xvsm.protocol.AddAspectRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.RemoveAspectRequest) {
            return unmarshalRemoveAspectRequest((org.xvsm.protocol.RemoveAspectRequest) xmlRequest);
        }
        // other requests
        if (xmlRequest instanceof org.xvsm.protocol.ClearSpaceRequest) {
            return unmarshalClearSpaceRequest((org.xvsm.protocol.ClearSpaceRequest) xmlRequest);
        }
        if (xmlRequest instanceof org.xvsm.protocol.ShutdownRequest) {
            return unmarshalShutdownRequest((org.xvsm.protocol.ShutdownRequest) xmlRequest);
        }
        throw new IllegalArgumentException("Cannot unmarshal request of type " + xmlRequest.getClass().getName());
    }

    private Request<?> unmarshalReadEntriesRequest(final org.xvsm.protocol.ReadEntriesRequest xmlRequest) {
        return new ReadEntriesRequest<Serializable>(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalSelectorList(xmlRequest.getSelectors()),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalTakeEntriesRequest(final org.xvsm.protocol.TakeEntriesRequest xmlRequest) {
        return new TakeEntriesRequest<Serializable>(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalSelectorList(xmlRequest.getSelectors()),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalWriteEntriesRequest(final org.xvsm.protocol.WriteEntriesRequest xmlRequest) {
        return new WriteEntriesRequest(
                unmarshalEntryList(xmlRequest.getEntries()),
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalTestEntriesRequest(final org.xvsm.protocol.TestEntriesRequest xmlRequest) {
        return new TestEntriesRequest(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalSelectorList(xmlRequest.getSelectors()),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalDeleteEntriesRequest(final org.xvsm.protocol.DeleteEntriesRequest xmlRequest) {
        return new DeleteEntriesRequest(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalSelectorList(xmlRequest.getSelectors()),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalCreateTransactionRequest(
            final org.xvsm.protocol.CreateTransactionRequest xmlRequest) {
        return new CreateTransactionRequest(
                xmlRequest.getTimeout(),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalCommitTransactionRequest(
            final org.xvsm.protocol.CommitTransactionRequest xmlRequest) {
        return new CommitTransactionRequest(
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalRollbackTransactionRequest(
            final org.xvsm.protocol.RollbackTransactionRequest xmlRequest) {
        return new RollbackTransactionRequest(
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalPrepareTransactionRequest(
            final org.xvsm.protocol.PrepareTransactionRequest xmlRequest) {
        return new PrepareTransactionRequest(
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalCreateContainerRequest(
            final org.xvsm.protocol.CreateContainerRequest xmlRequest) {
        Integer size = xmlRequest.getSize();
        return new CreateContainerRequest(
                xmlRequest.getName(),
                (size == null) ? Container.DEFAULT_SIZE : size,
                        unmarshalCoordinatorList(xmlRequest.getObligatoryCoords()),
                        unmarshalCoordinatorList(xmlRequest.getOptionalCoords()),
                        CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                        unmarshalIsolationLevel(xmlRequest.getIsolation()),
                        unmarshalAuthorizationLevel(xmlRequest.getAuthorization()),
                        xmlRequest.isForceInMemory(),
                        unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalLookupContainerRequest(
            final org.xvsm.protocol.LookupContainerRequest xmlRequest) {
        return new LookupContainerRequest(
                xmlRequest.getName(),
                unmarshalRequestTimeout(xmlRequest.getTimeout()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalDestroyContainerRequest(
            final org.xvsm.protocol.DestroyContainerRequest xmlRequest) {
        return new DestroyContainerRequest(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalLockContainerRequest(
            final org.xvsm.protocol.LockContainerRequest xmlRequest) {
        return new LockContainerRequest(
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalAddAspectRequest(
            final org.xvsm.protocol.AddAspectRequest xmlRequest) {
        return new AddAspectRequest(
                unmarshalContainerAspect(xmlRequest.getAspect()),
                CoreUtils.parseContainerReferenceWithoutPrefix(xmlRequest.getContainer()),
                unmarshalAspectIPoints(xmlRequest.getIpoints(), xmlRequest.getContainer() == null),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalRemoveAspectRequest(
            final org.xvsm.protocol.RemoveAspectRequest xmlRequest) {
        AspectReference aspectRef = CoreUtils.parseAspectReferenceWithoutPrefix(xmlRequest.getAspect());
        // HACK, depends on aspect reference creation in SimpleAspectManager
        boolean spaceAspect = !aspectRef.getId().contains("_");
        return new RemoveAspectRequest(
                aspectRef,
                unmarshalAspectIPoints(xmlRequest.getIpoints(), spaceAspect),
                CoreUtils.parseTransactionReferenceWithoutPrefix(xmlRequest.getTransaction()),
                unmarshalIsolationLevel(xmlRequest.getIsolation()),
                unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalClearSpaceRequest(
            final org.xvsm.protocol.ClearSpaceRequest xmlRequest) {
        return new ClearSpaceRequest(unmarshalRequestContext(xmlRequest.getContext()));
    }

    private Request<?> unmarshalShutdownRequest(
            final org.xvsm.protocol.ShutdownRequest xmlRequest) {
        return new ShutdownRequest(unmarshalRequestContext(xmlRequest.getContext()));
    }

    private ContainerAspect unmarshalContainerAspect(final org.xvsm.protocol.Aspect xmlAspect) {
        return (ContainerAspect) unmarshalAnyDom(xmlAspect.getAny());
    }

    private Serializable unmarshalAnyDom(final Element element) {
        // TODO reuse DomReader?
        // TODO at least reuse XmlFriendlyReplacer
        DomReader domReader = new DomReader(element);
        return (Serializable) xstream.unmarshal(domReader, null);
    }

    private Set<? extends InterceptionPoint> unmarshalAspectIPoints(final AspectIPointList xmlIPoints,
            final boolean spaceAspect) {
        if (xmlIPoints == null) {
            return null;
        }
        if (spaceAspect) {
            Set<SpaceIPoint> ipoints = new HashSet<SpaceIPoint>();
            for (AspectIPoint point : xmlIPoints.getIpoints()) {
                ipoints.add(unmarshalSpaceAspectIPoint(point));
            }
            return ipoints;
        } else {
            Set<ContainerIPoint> ipoints = new HashSet<ContainerIPoint>();
            for (AspectIPoint point : xmlIPoints.getIpoints()) {
                ipoints.add(unmarshalContainerAspectIPoint(point));
            }
            return ipoints;
        }
    }

    private SpaceIPoint unmarshalSpaceAspectIPoint(final AspectIPoint point) {
        switch(point) {
        case POST_ADD_ASPECT:
            return SpaceIPoint.POST_ADD_ASPECT;
        case POST_COMMIT_TRANSACTION:
            return SpaceIPoint.POST_COMMIT_TRANSACTION;
        case POST_CREATE_CONTAINER:
            return SpaceIPoint.POST_CREATE_CONTAINER;
        case POST_CREATE_TRANSACTION:
            return SpaceIPoint.POST_CREATE_TRANSACTION;
        case POST_DELETE:
            return SpaceIPoint.POST_DELETE;
        case POST_DESTROY_CONTAINER:
            return SpaceIPoint.POST_DESTROY_CONTAINER;
        case POST_LOCK_CONTAINER:
            return SpaceIPoint.POST_LOCK_CONTAINER;
        case POST_LOOKUP_CONTAINER:
            return SpaceIPoint.POST_LOOKUP_CONTAINER;
        case POST_PREPARE_TRANSACTION:
            return SpaceIPoint.POST_PREPARE_TRANSACTION;
        case POST_READ:
            return SpaceIPoint.POST_READ;
        case POST_REMOVE_ASPECT:
            return SpaceIPoint.POST_REMOVE_ASPECT;
        case POST_ROLLBACK_TRANSACTION:
            return SpaceIPoint.POST_ROLLBACK_TRANSACTION;
        case POST_TAKE:
            return SpaceIPoint.POST_TAKE;
        case POST_TEST:
            return SpaceIPoint.POST_TEST;
        case POST_WRITE:
            return SpaceIPoint.POST_WRITE;
        case PRE_ADD_ASPECT:
            return SpaceIPoint.PRE_ADD_ASPECT;
        case PRE_COMMIT_TRANSACTION:
            return SpaceIPoint.PRE_COMMIT_TRANSACTION;
        case PRE_CREATE_CONTAINER:
            return SpaceIPoint.PRE_CREATE_CONTAINER;
        case PRE_CREATE_TRANSACTION:
            return SpaceIPoint.PRE_CREATE_TRANSACTION;
        case PRE_DELETE:
            return SpaceIPoint.PRE_DELETE;
        case PRE_DESTROY_CONTAINER:
            return SpaceIPoint.PRE_DESTROY_CONTAINER;
        case PRE_LOCK_CONTAINER:
            return SpaceIPoint.PRE_LOCK_CONTAINER;
        case PRE_LOOKUP_CONTAINER:
            return SpaceIPoint.PRE_LOOKUP_CONTAINER;
        case PRE_PREPARE_TRANSACTION:
            return SpaceIPoint.PRE_PREPARE_TRANSACTION;
        case PRE_READ:
            return SpaceIPoint.PRE_READ;
        case PRE_REMOVE_ASPECT:
            return SpaceIPoint.PRE_REMOVE_ASPECT;
        case PRE_ROLLBACK_TRANSACTION:
            return SpaceIPoint.PRE_ROLLBACK_TRANSACTION;
        case PRE_SHUTDOWN:
            return SpaceIPoint.PRE_SHUTDOWN;
        case PRE_TAKE:
            return SpaceIPoint.PRE_TAKE;
        case PRE_TEST:
            return SpaceIPoint.PRE_TEST;
        case PRE_WRITE:
            return SpaceIPoint.PRE_WRITE;
        default:
            throw new IllegalArgumentException(point.toString());
        }
    }

    private ContainerIPoint unmarshalContainerAspectIPoint(final AspectIPoint point) {
        switch(point) {
        case POST_ADD_ASPECT:
            return ContainerIPoint.POST_ADD_ASPECT;
        case POST_DELETE:
            return ContainerIPoint.POST_DELETE;
        case POST_DESTROY_CONTAINER:
            return ContainerIPoint.POST_DESTROY_CONTAINER;
        case POST_LOCK_CONTAINER:
            return ContainerIPoint.POST_LOCK_CONTAINER;
        case POST_LOOKUP_CONTAINER:
            return ContainerIPoint.POST_LOOKUP_CONTAINER;
        case POST_READ:
            return ContainerIPoint.POST_READ;
        case POST_REMOVE_ASPECT:
            return ContainerIPoint.POST_REMOVE_ASPECT;
        case POST_TAKE:
            return ContainerIPoint.POST_TAKE;
        case POST_TEST:
            return ContainerIPoint.POST_TEST;
        case POST_WRITE:
            return ContainerIPoint.POST_WRITE;
        case PRE_ADD_ASPECT:
            return ContainerIPoint.PRE_ADD_ASPECT;
        case PRE_DELETE:
            return ContainerIPoint.PRE_DELETE;
        case PRE_DESTROY_CONTAINER:
            return ContainerIPoint.PRE_DESTROY_CONTAINER;
        case PRE_LOCK_CONTAINER:
            return ContainerIPoint.PRE_LOCK_CONTAINER;
        case PRE_READ:
            return ContainerIPoint.PRE_READ;
        case PRE_REMOVE_ASPECT:
            return ContainerIPoint.PRE_REMOVE_ASPECT;
        case PRE_TAKE:
            return ContainerIPoint.PRE_TAKE;
        case PRE_TEST:
            return ContainerIPoint.PRE_TEST;
        case PRE_WRITE:
            return ContainerIPoint.PRE_WRITE;
        default:
            throw new IllegalArgumentException(point.toString());
        }
    }

    private List<? extends Coordinator> unmarshalCoordinatorList(
            final org.xvsm.protocol.CoordinatorList xmlCoordinators) {
        if (xmlCoordinators == null) {
            return null;
        }
        List<Coordinator> coordinators = new ArrayList<Coordinator>();
        for (org.xvsm.protocol.Coordinator xmlCoord : xmlCoordinators.getCoordinators()) {
            String type = xmlCoord.getClass().getSimpleName();
            String name = xmlCoord.getName();
            // set not marshalled default name
            if (name == null) {
                name = type;
            }
            // TODO unmarshal additional coordinator properties
            Object[] params = null;
            if (xmlCoord instanceof org.xvsm.protocol.LindaCoordinator) {
                params = new Object[] {((org.xvsm.protocol.LindaCoordinator) xmlCoord).isOnlyAnnotatedEntries()};
            }
            Coordinator coord = coordinatorFactory.createCoordinator(type, name, params);
            coordinators.add(coord);
        }
        return coordinators;
    }

    private List<? extends CoordinationData> unmarshalCoordinationDataList(
            final org.xvsm.protocol.CoordinationDataList xmlCoordData) {
        List<CoordinationData> coordData = new ArrayList<CoordinationData>();
        for (org.xvsm.protocol.CoordinationData xmlData : xmlCoordData.getCoordDatas()) {
            String type = xmlData.getClass().getSimpleName();
            String name = xmlData.getName();
            if (name == null) {
                // set not marshalled default name
                name = type;
            }
            Object[] params = null;
            if (xmlData instanceof org.xvsm.protocol.AnyData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.FifoData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.KeyData) {
                params = new Object[] {((org.xvsm.protocol.KeyData) xmlData).getKey()};
            } else if (xmlData instanceof org.xvsm.protocol.LabelData) {
                params = new Object[] {((org.xvsm.protocol.LabelData) xmlData).getLabel()};
            } else if (xmlData instanceof org.xvsm.protocol.LifoData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.LindaData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.QueryData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.RandomData) {
                params = null;
            } else if (xmlData instanceof org.xvsm.protocol.VectorData) {
                params = new Object[] {((org.xvsm.protocol.VectorData) xmlData).getIndex()};
            } else if (xmlData instanceof org.xvsm.protocol.CustomCoordinationData) {
                type = ((CustomCoordinationData) xmlData).getType();
                params = null;
                // TODO unmarshal additional properties
            } else {
                throw new IllegalArgumentException("Cannot unmarshal coordination data of type "
                        + xmlData.getClass().getName());
            }
            CoordinationData sel = coordinationDataFactory.createCoordinationData(type, name, params);
            coordData.add(sel);
        }
        return coordData;
    }

    private List<? extends Selector> unmarshalSelectorList(final org.xvsm.protocol.SelectorList xmlSelectors) {
        List<Selector> selectors = new ArrayList<Selector>();
        for (org.xvsm.protocol.Selector xmlSel : xmlSelectors.getSelectors()) {
            String type = xmlSel.getClass().getSimpleName();
            String name = xmlSel.getName();
            if (name == null) {
                // set not marshalled default name
                name = type;
            }
            Integer count = xmlSel.getCount();
            if (count == null) {
                // set not marshalled default count
                count = 1;
            }
            Object[] params = null;
            if (xmlSel instanceof org.xvsm.protocol.AnySelector) {
                params = null;
            } else if (xmlSel instanceof org.xvsm.protocol.FifoSelector) {
                params = null;
            } else if (xmlSel instanceof org.xvsm.protocol.KeySelector) {
                params = new Object[] {((org.xvsm.protocol.KeySelector) xmlSel).getKey()};
            } else if (xmlSel instanceof org.xvsm.protocol.LabelSelector) {
                params = new Object[] {((org.xvsm.protocol.LabelSelector) xmlSel).getLabel()};
            } else if (xmlSel instanceof org.xvsm.protocol.LifoSelector) {
                params = null;
            } else if (xmlSel instanceof org.xvsm.protocol.LindaSelector) {
                EntryValue template = ((org.xvsm.protocol.LindaSelector) xmlSel).getTemplate();
                params = new Object[] {unmarshalEntryValue(template)};
            } else if (xmlSel instanceof org.xvsm.protocol.QuerySelector) {
                Query query = unmarshalQuery(((org.xvsm.protocol.QuerySelector) xmlSel).getQuery());
                params = new Object[] {query};
            } else if (xmlSel instanceof org.xvsm.protocol.RandomSelector) {
                params = null;
            } else if (xmlSel instanceof org.xvsm.protocol.VectorSelector) {
                params = new Object[] {((org.xvsm.protocol.VectorSelector) xmlSel).getIndex()};
            } else if (xmlSel instanceof org.xvsm.protocol.CustomSelector) {
                type = ((CustomSelector) xmlSel).getType();
                params = null;
                // TODO unmarshal additional properties
            } else {
                throw new IllegalArgumentException("Cannot unmarshal selector of type " + xmlSel.getClass().getName());
            }
            Selector sel = selectorFactory.createSelector(type, count, name, params);
            selectors.add(sel);
        }
        return selectors;
    }

    private Query unmarshalQuery(final org.xvsm.protocol.Query query) {
        // TODO unmarshal query
        return null;
    }

    private List<Entry> unmarshalEntryList(final org.xvsm.protocol.EntryList xmlEntries) {
        List<Entry> entries = new ArrayList<Entry>();
        for (org.xvsm.protocol.Entry xmlEntry : xmlEntries.getEntries()) {
            Serializable value = unmarshalEntryValue(xmlEntry.getValue());
            List<? extends CoordinationData> coordData = unmarshalCoordinationDataList(xmlEntry.getCoordData());
            Entry entry = new Entry(value, coordData);
            entries.add(entry);
        }
        return entries;
    }

    private Serializable unmarshalEntryValue(final org.xvsm.protocol.EntryValue xmlValue) {
        // TODO move to another class (user modifiable?)
        return unmarshalAnyDom(xmlValue.getAny());
    }

    private long unmarshalRequestTimeout(final Long timeout) {
        if (timeout == null) {
            return RequestTimeout.DEFAULT;
        } else {
            return timeout;
        }
    }

    private IsolationLevel unmarshalIsolationLevel(final org.xvsm.protocol.IsolationLevel isolation) {
        if (isolation == null) {
            return MzsConstants.DEFAULT_ISOLATION;
        }
        switch (isolation) {
        case READ_COMMITTED:
            return IsolationLevel.READ_COMMITTED;
        case REPEATABLE_READ:
            return IsolationLevel.REPEATABLE_READ;
        default:
            throw new IllegalArgumentException(isolation.toString());
        }
    }

    private AuthorizationLevel unmarshalAuthorizationLevel(final org.xvsm.protocol.AuthorizationLevel authLevel) {
        if (authLevel == null) {
            return MzsConstants.DEFAULT_AUTHORIZATION;
        }
        switch (authLevel) {
        case NONE:
            return AuthorizationLevel.NONE;
        case SECURE:
            return AuthorizationLevel.SECURE;
        default:
            throw new IllegalArgumentException(authLevel.toString());
        }
    }

    private RequestContext unmarshalRequestContext(final org.xvsm.protocol.PropertyList context) {
        if (context == null) {
            return null;
        }
        // TODO unmarshal request context
        return null;
    }

    private Response<?> unmarshalResponse(final org.xvsm.protocol.AbstractResponse response) {
        if (response instanceof org.xvsm.protocol.EmptyResponse) {
            if (response.getError() != null) {
                org.xvsm.protocol.Exception error = response.getError();
                Exception ex = exceptionFactory.createException(error.getType(), error.getMessage());
                // TODO add general (runtime) exceptions
                return new GenericResponse<Serializable>(null, ex);
            }
            return new GenericResponse<Nothing>(Nothing.INSTANCE, null);
        }
        if (response instanceof org.xvsm.protocol.EntryResponse) {
            Serializable result = unmarshalEntryResponse((org.xvsm.protocol.EntryResponse) response);
            return new GenericResponse<Serializable>(result, null);
        }
        if (response instanceof org.xvsm.protocol.IntegerResponse) {
            Integer count = ((org.xvsm.protocol.IntegerResponse) response).getResult();
            return new GenericResponse<Integer>(count, null);
        }
        if (response instanceof org.xvsm.protocol.TransactionResponse) {
            String refString = ((org.xvsm.protocol.TransactionResponse) response).getResult();
            return new GenericResponse<Serializable>(CoreUtils.parseTransactionReferenceWithoutPrefix(refString), null);
        }
        if (response instanceof org.xvsm.protocol.ContainerResponse) {
            String refString = ((org.xvsm.protocol.ContainerResponse) response).getResult();
            return new GenericResponse<Serializable>(CoreUtils.parseContainerReferenceWithoutPrefix(refString), null);
        }
        if (response instanceof org.xvsm.protocol.AspectResponse) {
            String refString = ((org.xvsm.protocol.AspectResponse) response).getResult();
            return new GenericResponse<Serializable>(CoreUtils.parseAspectReferenceWithoutPrefix(refString), null);
        }
        throw new IllegalArgumentException("Cannot unmarshal response of type " + response.getClass().getName());
    }

    private Serializable unmarshalEntryResponse(final org.xvsm.protocol.EntryResponse response) {
        org.xvsm.protocol.EntryValueList xmlValues = response.getValues();
        ArrayList<Serializable> entryValues = new ArrayList<Serializable>();
        for (org.xvsm.protocol.EntryValue xmlValue : xmlValues.getValues()) {
            Serializable value = unmarshalEntryValue(xmlValue);
            entryValues.add(value);
        }
        return entryValues;
    }

}
