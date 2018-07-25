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
package org.mozartspaces.capi3.javanative.operation;

import java.io.Closeable;
import java.util.List;

import org.mozartspaces.capi3.Capi3Exception;
import org.mozartspaces.capi3.EntryOperationResult;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LocalContainerReference;
import org.mozartspaces.capi3.OperationResult;
import org.mozartspaces.capi3.javanative.coordination.NativeSelector;
import org.mozartspaces.capi3.javanative.isolation.NativeSubTransaction;
import org.mozartspaces.capi3.javanative.persistence.PersistenceContext;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreRuntimeException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.metamodel.MetaDataProvider;
import org.mozartspaces.core.metamodel.Navigable;

/**
 * This is the Container Interface for the CAPI3 JavaNative Implementation.
 *
 * @author Martin Barisits
 * @author Stefan Crass
 */
public interface NativeContainer extends Navigable, MetaDataProvider, Closeable {

    @Override
    void close() throws MzsCoreRuntimeException;

    /**
     * Returns a <code>LocalContainerReference</code> for this container.
     *
     * @return the LocalContainerReference
     */
    LocalContainerReference getReference();

    /**
     * Reads entries from the container.
     *
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeReadOperation(List<NativeSelector<?>> selectors, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context);

    /**
     * Writes an entry object to the container.
     *
     * @param entry
     *            the entry with the object that should be written
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an OperationResult containing the relevant information
     */
    OperationResult executeWriteOperation(Entry entry, IsolationLevel isolationLevel, NativeSubTransaction stx,
            RequestContext context);

    /**
     * Takes entries from the container.
     *
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return an EntryOperationResult containing the relevant information
     */
    EntryOperationResult executeTakeOperation(List<NativeSelector<?>> selectors, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context);

    @Override
    boolean equals(final Object obj);

    @Override
    int hashCode();

    /**
     * This function should be called when a ContainerDestroyOperation is committed for this Container.
     */
    void dispose();

    /**
     * This function is used to check if a container is still valid. Deleted containers are marked as invalid, but
     * references could stay in the system until that change is permanent (TX commit).
     *
     * @return true if valid, false otherwise
     */
    boolean isValid();

    /**
     * Return the Container Name.
     *
     * @return the name of the Container
     */
    String getName();

    /**
     * Return the Container Id.
     *
     * @return the Id of the Container
     */
    long getId();

    /**
     * Return the string representation of the Id.
     *
     * @return the Id of the Container
     */
    String getIdAsString();

    /**
     * Permanently removes an entry from the container and all associated coordinators.
     *
     * @param entry
     *            the entry to purge
     * @param context
     *            the context of the take/delete request
     * @param stx
     *            the sub-transaction of the take/delete request
     */
    void purgeEntry(NativeEntry entry, RequestContext context, NativeSubTransaction stx);

    /**
     * Get a serializable container descriptor that can be used to restore the container .
     *
     * @return a container descriptor for restoring the container.
     */
    PersistentContainerDescriptor getPersistentContainerDescriptor();

    /**
     * Get an entry with the given ID.
     *
     * @param id
     *            the ID of the entry
     * @return the entry with the given ID or {@code null} if no such entry is found.
     */
    NativeEntry getEntry(long id);

    /**
     * Restore the content of the container from persistent storage. This method is called during the restoration
     * process after the container has been recreated (based on the persistent descriptor) and before the space starts
     * accepting requests.
     *
     * @param persistenceContext
     *            the persistence context (the same one as is used during the instantiation of the container)
     * @param stx
     *            the sub-transaction of the restoration process.
     */
    void restoreContent(PersistenceContext persistenceContext, NativeSubTransaction stx);

    /**
     * Selects NativeEntries according to coordination properties from a container. Used by Access Manager.
     *
     * @param selectors
     *            the selector chain which is responsible for filtering entries
     * @param isolationLevel
     *            isolation level to be used
     * @param stx
     *            subTransaction to be used to isolate the data
     * @param context
     *            context information passed through the core, may be <code>null</code>
     * @return list of selected entries
     * @throws Capi3Exception
     *             if the selection fails
     */
    List<NativeEntry> selectEntries(List<NativeSelector<?>> selectors, IsolationLevel isolationLevel,
            NativeSubTransaction stx, RequestContext context) throws Capi3Exception;

}
