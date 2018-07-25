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
package org.mozartspaces.runtime.aspects;

import java.io.Serializable;
import java.util.List;

import net.jcip.annotations.Immutable;

import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.aspects.AspectException;
import org.mozartspaces.core.aspects.AspectReference;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.aspects.AspectStatus;
import org.mozartspaces.core.aspects.ContainerAspect;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.core.aspects.SpaceAspect;
import org.mozartspaces.core.aspects.SpaceIPoint;
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
import org.mozartspaces.util.LoggerFactory;
import org.slf4j.Logger;

/**
 * An <code>AspectInvoker</code> that serially invokes, in the method for a
 * specific interception point, first all space aspects and then all container
 * aspects in the order the Aspect Manager returns them.
 * <p>
 * If an aspect returns with a status that is not <code>OK</code> or
 * <code>null</code> (which is interpreted as <code>OK</code>), the aspect
 * invocation is aborted. If an aspect throws a runtime exception, an aspect
 * result with status <code>NOTOK</code> and the runtime exception is returned.
 *
 * @author Tobias Doenz
 */
@Immutable
public final class SerialAspectInvoker implements AspectInvoker {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.get();

    private final AspectManager aspectManager;

    /**
     * Constructs a <code>SerialAspectInvoker</code>.
     *
     * @param aspectManager
     *            the Aspect Manager, used to get the registered aspects for an
     *            interception point
     */
    public SerialAspectInvoker(final AspectManager aspectManager) {
        this.aspectManager = aspectManager;
        assert this.aspectManager != null;
    }

    @Override
    public AspectResult postAddAspect(final AddAspectRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final AspectReference aspectRef) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        //assert capi3 != null;
        assert executionCount > 0;
        //assert aspect != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postAddAspect");
            AddAspectRequest req = request;
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(req.getContainer(),
                    ContainerIPoint.POST_ADD_ASPECT, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postAddAspect(request, tx, stx, capi3, executionCount, aspectRef);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postAddAspect");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_ADD_ASPECT, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postAddAspect(request, tx, stx, capi3, executionCount, aspectRef);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert entries != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postDelete");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_DELETE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postDelete(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postDelete");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_DELETE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postDelete(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postDestroyContainer");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_DESTROY_CONTAINER, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postDestroyContainer(request, tx, stx, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postDestroyContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_DESTROY_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postDestroyContainer(request, tx, stx, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert entries != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postRead");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_READ, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postRead(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postRead");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_READ, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postRead(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference cRef, final Capi3AspectPort capi3,
            final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        // assert cRef != null;
        // assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postRemoveAspect");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(aspectManager
                    .getContainerWhereAspectIsRegistered(request.getAspect(), tx),
                    ContainerIPoint.POST_REMOVE_ASPECT, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postRemoveAspect(request, tx, stx, cRef, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postRemoveAspect");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_REMOVE_ASPECT, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postRemoveAspect(request, tx, stx, cRef, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postSetContainerLock");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_LOCK_CONTAINER, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postLockContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postSetContainerLock");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_LOCK_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postLockContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert entries != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postTake");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_TAKE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postTake(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postTake");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_TAKE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postTake(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount, final List<Serializable> entries) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert entries != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postTest");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_TEST, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postTest(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postTest");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_TEST, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postTest(request, tx, stx, capi3, executionCount, entries);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postWrite");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.POST_WRITE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postWrite(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postWrite");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.POST_WRITE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postWrite(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preAddAspect(final AddAspectRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        //assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preAddAspect");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_ADD_ASPECT,
                    tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preAddAspect(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preAddAspect");
            AddAspectRequest req = request;
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(req.getContainer(),
                    ContainerIPoint.PRE_ADD_ASPECT, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preAddAspect(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preDelete(final DeleteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preDelete");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_DELETE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preDelete(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preDelete");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_DELETE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preDelete(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preDestroyContainer(final DestroyContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preDestroyContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_DESTROY_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preDestroyContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preDestroyContainer");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_DESTROY_CONTAINER, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preDestroyContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preRead(final ReadEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preRead");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_READ, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preRead(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preRead");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_READ, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preRead(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preRemoveAspect(final RemoveAspectRequest request, final Transaction tx,
            final SubTransaction stx, final ContainerReference cRef, final Capi3AspectPort capi3,
            final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        // assert cRef != null;
        // assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preRemoveAspect");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_REMOVE_ASPECT, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preRemoveAspect(request, tx, stx, cRef, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preRemoveAspect");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(aspectManager
                    .getContainerWhereAspectIsRegistered(request.getAspect(), tx),
                    ContainerIPoint.PRE_REMOVE_ASPECT, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preRemoveAspect(request, tx, stx, cRef, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preLockContainer(final LockContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preSetContainerLock");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_LOCK_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preLockContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preSetContainerLock");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_LOCK_CONTAINER, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preLockContainer(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preTake(final TakeEntriesRequest<?> request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preTake");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_TAKE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preTake(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preTake");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_TAKE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preTake(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preTest(final TestEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preTest");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_TEST, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preTest(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preTest");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_TEST, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preTest(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preWrite(final WriteEntriesRequest request, final Transaction tx, final SubTransaction stx,
            final Capi3AspectPort capi3, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preWrite");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_WRITE, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preWrite(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing container aspects for preWrite");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(request
                    .getContainer(), ContainerIPoint.PRE_WRITE, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.preWrite(request, tx, stx, capi3, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for postCommitTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_COMMIT_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postCommitTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final ContainerReference cRef) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert cRef != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for postCreateContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_CREATE_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postCreateContainer(request, tx, stx, capi3, executionCount, cRef);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postCreateTransaction(final CreateTransactionRequest request,
            final TransactionReference txRef, final Transaction tx) {

        assert request != null;
        assert txRef != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for postCreateTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_CREATE_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postCreateTransaction(request, txRef, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final Capi3AspectPort capi3, final int executionCount,
            final ContainerReference cRef) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert capi3 != null;
        assert executionCount > 0;
        assert cRef != null;

        AspectResult result = null;
        try {
            log.debug("Executing container aspects for postLookupContainer");
            List<ContainerAspect> registeredContainerAspects = aspectManager.getContainerAspects(cRef,
                    ContainerIPoint.POST_LOOKUP_CONTAINER, tx);
            for (ContainerAspect aspect : registeredContainerAspects) {
                result = aspect.postLookupContainer(request, tx, stx, capi3, executionCount, cRef);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }

            log.debug("Executing space aspects for postLookupContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_LOOKUP_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postLookupContainer(request, tx, stx, capi3, executionCount, cRef);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postPrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for postPrepareTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_PREPARE_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postPrepareTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult postRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for postRollbackTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.POST_ROLLBACK_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.postRollbackTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preCommitTransaction(final CommitTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preCommitTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_COMMIT_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preCommitTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preCreateContainer(final CreateContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preCreateContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_CREATE_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preCreateContainer(request, tx, stx, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preCreateTransaction(final CreateTransactionRequest request) {

        assert request != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preCreateTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_CREATE_TRANSACTION, null);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preCreateTransaction(request);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preLookupContainer(final LookupContainerRequest request, final Transaction tx,
            final SubTransaction stx, final int executionCount) {

        assert request != null;
        assert tx != null;
        assert stx != null;
        assert executionCount > 0;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preLookupContainer");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_LOOKUP_CONTAINER, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preLookupContainer(request, tx, stx, executionCount);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult prePrepareTransaction(final PrepareTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for prePrepareTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_PREPARE_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.prePrepareTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preRollbackTransaction(final RollbackTransactionRequest request, final Transaction tx) {

        assert request != null;
        assert tx != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preRollbackTransaction");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(
                    SpaceIPoint.PRE_ROLLBACK_TRANSACTION, tx);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preRollbackTransaction(request, tx);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

    @Override
    public AspectResult preShutdown(final ShutdownRequest request) {

        assert request != null;

        AspectResult result = null;
        try {
            log.debug("Executing space aspects for preShutdown");
            List<SpaceAspect> registeredAspects = aspectManager.getSpaceAspects(SpaceIPoint.PRE_SHUTDOWN,
                    null);
            for (SpaceAspect aspect : registeredAspects) {
                result = aspect.preShutdown(request);
                if (result == null) {
                    return new AspectResult(new AspectException("Aspect returned null"));
                }
                if (result.getStatus() != AspectStatus.OK) {
                    return result;
                }
            }
        } catch (RuntimeException ex) {
            return new AspectResult(AspectStatus.NOTOK, ex);
        }

        return new AspectResult(AspectStatus.OK);
    }

}
