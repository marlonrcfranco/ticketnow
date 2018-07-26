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
package org.mozartspaces.core.aspects;

import org.mozartspaces.capi3.OperationStatus;

/**
 * The constants an aspect can have as return status values.
 *
 * @author Tobias Doenz
 */
public enum AspectStatus {

    /**
     * Status indicating that the following aspects, and the operation itself if
     * it is a pre-aspect, should be skipped.
     */
    SKIP,

    // constants and comments below are from OperationStatus
    /**
     * Status indicating that an operation has finished successful.
     */
    OK,
    /**
     * Status indicating that an operation has not finished successful, e.g. an
     * error occurred or no entries could be selected.
     */
    NOTOK,
    /**
     * Status indicating that an operation may be finished successful at a later
     * time.
     */
    DELAYABLE,
    /**
     * Status indicating that an operation tried to access locked entries.
     */
    LOCKED;

    /**
     * Converts an operation status to an aspect status.
     * <code>AspectStatus</code> has the constant <code>SKIP</code> in addition
     * to the constants defined in <code>OperationStatus</code>.
     *
     * @param opStatus the operation status to convert
     * @return the aspect status
     */
    public static AspectStatus fromOperationStatus(final OperationStatus opStatus) {
        if (opStatus == null) {
            throw new NullPointerException();
        }
        switch (opStatus) {
        case OK:
            return AspectStatus.OK;
        case NOTOK:
            return AspectStatus.NOTOK;
        case LOCKED:
            return AspectStatus.LOCKED;
        case DELAYABLE:
            return AspectStatus.DELAYABLE;
        default:
            throw new AssertionError("Unknown status " + opStatus);
        }
    }

    /**
     * Converts the aspect status to an operation status.
     *
     * @return the operation status
     * @throws RuntimeException if the aspect status is <code>SKIP</code>
     */
    public OperationStatus toOperationStatus() {
        switch (this) {
        case OK:
            return OperationStatus.OK;
        case NOTOK:
            return OperationStatus.NOTOK;
        case LOCKED:
            return OperationStatus.LOCKED;
        case DELAYABLE:
            return OperationStatus.DELAYABLE;
        case SKIP:
            throw new RuntimeException("SKIP is not a valid OperationStatus");
        default:
            throw new AssertionError("Unknown status " + this);
        }
    }
}
