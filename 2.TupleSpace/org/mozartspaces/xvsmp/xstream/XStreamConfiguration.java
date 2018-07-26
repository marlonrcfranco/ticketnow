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
package org.mozartspaces.xvsmp.xstream;

import java.net.URI;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.ContainerNotFoundException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.AbstractMessage;
import org.mozartspaces.core.AnswerContainerInfo;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.GenericResponse;
import org.mozartspaces.core.RequestMessage;
import org.mozartspaces.core.RequestReference;
import org.mozartspaces.core.ResponseMessage;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.SpaceIPoint;
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
import org.mozartspaces.core.util.Nothing;

import com.thoughtworks.xstream.XStream;

/**
 * Configuration of XStream for the MozartSpaces Core XVSMP.
 *
 * @author Tobias Doenz
 */
// TODO add more aliases and converters for better performance
public final class XStreamConfiguration {

    static void configure(final XStream xstream) {

        xstream.addImmutableType(URI.class);

        // aliases
        // request and response messages
        xstream.alias("RequestMessage", RequestMessage.class);
        xstream.alias("ResponseMessage", ResponseMessage.class);
        xstream.useAttributeFor(AbstractMessage.class, "requestRef");
        xstream.aliasField("ref", AbstractMessage.class, "requestRef");
        xstream.aliasField("Request", RequestMessage.class, "content");
        //xstream.aliasField("Response", ResponseMessage.class, "content");
        xstream.alias("AnswerContainerInfo", AnswerContainerInfo.class);
        xstream.alias("Response", GenericResponse.class);

        // references
        xstream.alias("AspectReference", AspectReference.class);
        xstream.alias("ContainerReference", ContainerReference.class);
        xstream.alias("RequestReference", RequestReference.class);
        xstream.alias("TransactionReference", TransactionReference.class);

        // requests
        xstream.alias("AddAspect", AddAspectRequest.class);
        xstream.alias("ClearSpace", ClearSpaceRequest.class);
        xstream.alias("CommitTransaction", CommitTransactionRequest.class);
        xstream.alias("CreateContainer", CreateContainerRequest.class);
        xstream.alias("CreateTransaction", CreateTransactionRequest.class);
        xstream.alias("Delete", DeleteEntriesRequest.class);
        xstream.alias("DestroyContainer", DestroyContainerRequest.class);
        xstream.alias("LockContainer", LockContainerRequest.class);
        xstream.alias("LookupContainer", LookupContainerRequest.class);
        xstream.alias("PrepareTransaction", PrepareTransactionRequest.class);
        xstream.alias("Read", ReadEntriesRequest.class);
        xstream.alias("RemoveAspect", RemoveAspectRequest.class);
        xstream.alias("RollbackTransaction", RollbackTransactionRequest.class);
        xstream.alias("Shutdown", ShutdownRequest.class);
        xstream.alias("Take", TakeEntriesRequest.class);
        xstream.alias("Test", TestEntriesRequest.class);
        xstream.alias("Write", WriteEntriesRequest.class);

        // misc. API classes
        xstream.alias("cipoint", ContainerIPoint.class);
        xstream.alias("sipoint", SpaceIPoint.class);
        xstream.alias("Entry", Entry.class);
        xstream.alias("Nothing", Nothing.class);

        // coordinators, coordination data, selectors
        xstream.alias("AnyCoordinator", AnyCoordinator.class);
        xstream.alias("AnyData", AnyCoordinator.AnyData.class);
        xstream.alias("AnySelector", AnyCoordinator.AnySelector.class);
        xstream.alias("FifoCoordinator", FifoCoordinator.class);
        xstream.alias("FifoData", FifoCoordinator.FifoData.class);
        xstream.alias("FifoSelector", FifoCoordinator.FifoSelector.class);
        xstream.alias("KeyCoordinator", KeyCoordinator.class);
        xstream.alias("KeyData", KeyCoordinator.KeyData.class);
        xstream.alias("KeySelector", KeyCoordinator.KeySelector.class);
        xstream.alias("LabelCoordinator", LabelCoordinator.class);
        xstream.alias("LabelData", LabelCoordinator.LabelData.class);
        xstream.alias("LabelSelector", LabelCoordinator.LabelSelector.class);
        xstream.alias("LifoCoordinator", LifoCoordinator.class);
        xstream.alias("LifoData", LifoCoordinator.LifoData.class);
        xstream.alias("LifoSelector", LifoCoordinator.LifoSelector.class);
        xstream.alias("LabelCoordinator", LabelCoordinator.class);
        xstream.alias("LabelData", LabelCoordinator.LabelData.class);
        xstream.alias("LabelSelector", LabelCoordinator.LabelSelector.class);
        xstream.alias("LindaCoordinator", LindaCoordinator.class);
        xstream.alias("LindaData", LindaCoordinator.LindaData.class);
        xstream.alias("LindaSelector", LindaCoordinator.LindaSelector.class);
        xstream.alias("QueryCoordinator", QueryCoordinator.class);
        xstream.alias("QueryData", QueryCoordinator.QueryData.class);
        xstream.alias("QuerySelector", QueryCoordinator.QuerySelector.class);
        xstream.alias("RandomCoordinator", RandomCoordinator.class);
        xstream.alias("RandomData", RandomCoordinator.RandomData.class);
        xstream.alias("RandomSelector", RandomCoordinator.RandomSelector.class);
        xstream.alias("VectorCoordinator", VectorCoordinator.class);
        xstream.alias("VectorData", VectorCoordinator.VectorData.class);
        xstream.alias("VectorSelector", VectorCoordinator.VectorSelector.class);
        // TODO query classes

        // exceptions
        xstream.alias("ContainerNotFoundException", ContainerNotFoundException.class);
        // TODO alias other exceptions

        // register converters
        xstream.registerConverter(new ReferenceConverters.AspectReferenceConverter());
        xstream.registerConverter(new ReferenceConverters.ContainerReferenceConverter());
        xstream.registerConverter(new ReferenceConverters.RequestReferenceConverter());
        xstream.registerConverter(new ReferenceConverters.TransactionReferenceConverter());
        xstream.registerConverter(new Converters.URIConverter());
        xstream.registerConverter(new Converters.NothingConverter());
        xstream.registerConverter(new ResponseMessageConverter(xstream));
    }

    private XStreamConfiguration() {
    }
}
