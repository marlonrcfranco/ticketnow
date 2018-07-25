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
package org.mozartspaces.xvsmp.util;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.ContainerFullException;
import org.mozartspaces.capi3.ContainerLockedException;
import org.mozartspaces.capi3.ContainerNameNotAvailableException;
import org.mozartspaces.capi3.ContainerNotFoundException;
import org.mozartspaces.capi3.CoordinatorLockedException;
import org.mozartspaces.capi3.CoordinatorNotRegisteredException;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.DuplicateCoordinatorException;
import org.mozartspaces.capi3.DuplicateKeyException;
import org.mozartspaces.capi3.EntryLockedException;
import org.mozartspaces.capi3.EntryNotAnnotatedException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.InvalidContainerException;
import org.mozartspaces.capi3.InvalidContainerNameException;
import org.mozartspaces.capi3.InvalidCoordinatorNameException;
import org.mozartspaces.capi3.InvalidEntryException;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.ObligatoryCoordinatorMissingException;
import org.mozartspaces.capi3.QueryCoordinator;
import org.mozartspaces.capi3.RandomCoordinator;
import org.mozartspaces.capi3.VectorCoordinator;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.aspects.AspectException;
import org.mozartspaces.core.util.SerializationException;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.AnyCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.FifoCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.KeyCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.LabelCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.LifoCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.LindaCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.QueryCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.RandomCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinationDataCreators.VectorCoordinationDataCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.AnyCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.FifoCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.KeyCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.LabelCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.LifoCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.LindaCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.QueryCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.RandomCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedCoordinatorCreators.VectorCoordinatorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.AnySelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.FifoSelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.KeySelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.LabelSelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.LifoSelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.LindaSelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.QuerySelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.RandomSelectorCreator;
import org.mozartspaces.xvsmp.util.PredefinedSelectorCreators.VectorSelectorCreator;

/**
 * Contains utility methods to configure the factories by adding the creators
 * for the predefined types.
 *
 * @author Tobias Doenz
 */
public final class FactoryConfigurations {

    // TODO add new exceptions
    static void addPredefinedExceptionCreators(final ExceptionFactory factory) {
        // Runtime exceptions
        factory.addExceptionCreator("AspectException", new ExceptionCreator<AspectException>() {
            @Override
            public AspectException newException(final String message) {
                return new AspectException(message);
            }
        });
        factory.addExceptionCreator("SerializationException", new ExceptionCreator<SerializationException>() {
            @Override
            public SerializationException newException(final String message) {
                return new SerializationException(message);
            }
        });
        factory.addExceptionCreator("MzsTimeoutException", new ExceptionCreator<MzsTimeoutException>() {
            @Override
            public MzsTimeoutException newException(final String message) {
                return new MzsTimeoutException(message);
            }
        });
        factory.addExceptionCreator("TransactionException", new ExceptionCreator<TransactionException>() {
            @Override
            public TransactionException newException(final String message) {
                return new TransactionException(message);
            }
        });

        // CAPI-3 exceptions
        factory.addExceptionCreator("ContainerNotFoundException", new ExceptionCreator<ContainerNotFoundException>() {
            @Override
            public ContainerNotFoundException newException(final String message) {
                return new ContainerNotFoundException(message);
            }
        });
        factory.addExceptionCreator("ContainerFullException", new ExceptionCreator<ContainerFullException>() {
            @Override
            public ContainerFullException newException(final String message) {
                return new ContainerFullException();
            }
        });
        factory.addExceptionCreator("ContainerNameNotAvailableException",
                new ExceptionCreator<ContainerNameNotAvailableException>() {
                    @Override
                    public ContainerNameNotAvailableException newException(final String message) {
                        return new ContainerNameNotAvailableException(message);
                    }
                });
        factory.addExceptionCreator("DuplicateKeyException", new ExceptionCreator<DuplicateKeyException>() {
            @Override
            public DuplicateKeyException newException(final String message) {
                return new DuplicateKeyException(null, null); // TODO
                                                              // coordinator,
                                                              // key
            }
        });
        factory.addExceptionCreator("InvalidContainerException", new ExceptionCreator<InvalidContainerException>() {
            @Override
            public InvalidContainerException newException(final String message) {
                return new InvalidContainerException();
            }
        });
        factory.addExceptionCreator("InvalidContainerNameException",
                new ExceptionCreator<InvalidContainerNameException>() {
                    @Override
                    public InvalidContainerNameException newException(final String message) {
                        return new InvalidContainerNameException(message);
                    }
                });
        factory.addExceptionCreator("ObligatoryCoordinatorMissingException",
                new ExceptionCreator<ObligatoryCoordinatorMissingException>() {
                    @Override
                    public ObligatoryCoordinatorMissingException newException(final String message) {
                        return new ObligatoryCoordinatorMissingException(message);
                    }
                });
        factory.addExceptionCreator("CountNotMetException", new ExceptionCreator<CountNotMetException>() {
            @Override
            public CountNotMetException newException(final String message) {
                return new CountNotMetException(null, 0, 0); // TODO selector,
                                                             // countNeeded,
                                                             // countAvailable
            }
        });
        factory.addExceptionCreator("CoordinatorNotRegisteredException",
                new ExceptionCreator<CoordinatorNotRegisteredException>() {
                    @Override
                    public CoordinatorNotRegisteredException newException(final String message) {
                        return new CoordinatorNotRegisteredException(message);
                    }
                });
        factory.addExceptionCreator("InvalidCoordinatorNameException",
                new ExceptionCreator<InvalidCoordinatorNameException>() {
                    @Override
                    public InvalidCoordinatorNameException newException(final String message) {
                        return new InvalidCoordinatorNameException(message);
                    }
                });
        factory.addExceptionCreator("DuplicateCoordinatorException",
                new ExceptionCreator<DuplicateCoordinatorException>() {
                    @Override
                    public DuplicateCoordinatorException newException(final String message) {
                        return new DuplicateCoordinatorException(message);
                    }
                });
        factory.addExceptionCreator("InvalidEntryException", new ExceptionCreator<InvalidEntryException>() {
            @Override
            public InvalidEntryException newException(final String message) {
                return new InvalidEntryException();
            }
        });
        factory.addExceptionCreator("EntryNotAnnotatedException", new ExceptionCreator<EntryNotAnnotatedException>() {
            @Override
            public EntryNotAnnotatedException newException(final String message) {
                return new EntryNotAnnotatedException();
            }
        });
        factory.addExceptionCreator("ContainerLockedException", new ExceptionCreator<ContainerLockedException>() {
            @Override
            public ContainerLockedException newException(final String message) {
                return new ContainerLockedException("", ""); // TODO TX IDs
            }
        });
        factory.addExceptionCreator("EntryLockedException", new ExceptionCreator<EntryLockedException>() {
            @Override
            public EntryLockedException newException(final String message) {
                return new EntryLockedException("", ""); // TODO TX IDs
            }
        });
        factory.addExceptionCreator("CoordinatorLockedException", new ExceptionCreator<CoordinatorLockedException>() {
            @Override
            public CoordinatorLockedException newException(final String message) {
                return new CoordinatorLockedException("", "", ""); // TODO name, TX IDs
            }
        });
    }

    // Note: The type names need to correspond with the type in the XVSMP Schema
    static void addPredefinedCoordinatorCreators(final CoordinatorFactory factory) {
        factory.addCoordinatorCreator(AnyCoordinator.DEFAULT_NAME, new AnyCoordinatorCreator());
        factory.addCoordinatorCreator(FifoCoordinator.DEFAULT_NAME, new FifoCoordinatorCreator());
        factory.addCoordinatorCreator(KeyCoordinator.DEFAULT_NAME, new KeyCoordinatorCreator());
        factory.addCoordinatorCreator(LabelCoordinator.DEFAULT_NAME, new LabelCoordinatorCreator());
        factory.addCoordinatorCreator(LifoCoordinator.DEFAULT_NAME, new LifoCoordinatorCreator());
        factory.addCoordinatorCreator(LindaCoordinator.DEFAULT_NAME, new LindaCoordinatorCreator());
        factory.addCoordinatorCreator(QueryCoordinator.DEFAULT_NAME, new QueryCoordinatorCreator());
        factory.addCoordinatorCreator(RandomCoordinator.DEFAULT_NAME, new RandomCoordinatorCreator());
        factory.addCoordinatorCreator(VectorCoordinator.DEFAULT_NAME, new VectorCoordinatorCreator());
    }

    // Note: The type names need to correspond with the type in the XVSMP Schema
    static void addPredefinedCoordinationDataCreators(final CoordinationDataFactory factory) {
        factory.addCoordinationDataCreator("AnyData", new AnyCoordinationDataCreator());
        factory.addCoordinationDataCreator("FifoData", new FifoCoordinationDataCreator());
        factory.addCoordinationDataCreator("KeyData", new KeyCoordinationDataCreator());
        factory.addCoordinationDataCreator("LabelData", new LabelCoordinationDataCreator());
        factory.addCoordinationDataCreator("LifoData", new LifoCoordinationDataCreator());
        factory.addCoordinationDataCreator("LindaData", new LindaCoordinationDataCreator());
        factory.addCoordinationDataCreator("QueryData", new QueryCoordinationDataCreator());
        factory.addCoordinationDataCreator("RandomData", new RandomCoordinationDataCreator());
        factory.addCoordinationDataCreator("VectorData", new VectorCoordinationDataCreator());
    }

    // Note: The type names need to correspond with the type in the XVSMP Schema
    static void addPredefinedSelectorCreators(final SelectorFactory factory) {
        factory.addSelectorCreator("AnySelector", new AnySelectorCreator());
        // factory.addSelectorCreator(FifoCoordinator.DEFAULT_NAME, new
        // FifoSelectorCreator());
        factory.addSelectorCreator("FifoSelector", new FifoSelectorCreator());
        // factory.addSelectorCreator(KeyCoordinator.DEFAULT_NAME, new
        // KeySelectorCreator());
        factory.addSelectorCreator("KeySelector", new KeySelectorCreator());
        // factory.addSelectorCreator(LabelCoordinator.DEFAULT_NAME, new
        // LabelSelectorCreator());
        factory.addSelectorCreator("LabelSelector", new LabelSelectorCreator());
        // factory.addSelectorCreator(LifoCoordinator.DEFAULT_NAME, new
        // LifoSelectorCreator());
        factory.addSelectorCreator("LifoSelector", new LifoSelectorCreator());
        // factory.addSelectorCreator(LindaCoordinator.DEFAULT_NAME, new
        // LindaSelectorCreator());
        factory.addSelectorCreator("LindaSelector", new LindaSelectorCreator());
        // factory.addSelectorCreator(QueryCoordinator.DEFAULT_NAME, new
        // QuerySelectorCreator());
        factory.addSelectorCreator("QuerySelector", new QuerySelectorCreator());
        // factory.addSelectorCreator(RandomCoordinator.DEFAULT_NAME, new
        // RandomSelectorCreator());
        factory.addSelectorCreator("RandomSelector", new RandomSelectorCreator());
        // factory.addSelectorCreator(VectorCoordinator.DEFAULT_NAME, new
        // VectorSelectorCreator());
        factory.addSelectorCreator("VectorSelector", new VectorSelectorCreator());
    }

    private FactoryConfigurations() {
    }

}
