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
package org.mozartspaces.core.authorization;

import java.io.Serializable;

import org.mozartspaces.capi3.LocalContainerReference;

/**
 * Specifies the target of a request relevant for authorization purposes.
 * @author Stefan Crass
 */
public final class RequestAuthTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Subject subject;
    private final ContainerAction action;
    private final LocalContainerReference container;

    /**
     * @param subject
     *          subject of the request, use {@code null} to match all subjects
     * @param action
     *          action of the request, use {@code null} to match all actions
     * @param container
     *          target container of the request, use {@code null} to match all containers
     */
    public RequestAuthTarget(final Subject subject, final ContainerAction action,
            final LocalContainerReference container) {
        this.subject = subject;
        this.action = action;
        this.container = container;
    }

    /**
     * @return the subject
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * @return the action
     */
    public ContainerAction getAction() {
        return this.action;
    }

    /**
     * @return the container reference
     */
    public LocalContainerReference getContainer() {
        return this.container;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof RequestAuthTarget) {
            RequestAuthTarget other = (RequestAuthTarget) obj;
            if (other.subject != null) {
                if (!this.subject.equals(other.subject)) {
                    return false;
                }
            } else if (this.subject != null) {
                return false;
            }
            if (other.action != null) {
                if (!this.action.equals(other.action)) {
                    return false;
                }
            } else if (this.action != null) {
                return false;
            }
            if (other.container != null) {
                if (!this.container.equals(other.container)) {
                    return false;
                }
            } else if (this.container != null) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ((subject == null) ? 0 : subject.hashCode());
        result = 31 * result + ((action == null) ? 0 : action.hashCode());
        result = 31 * result + ((container == null) ? 0 : container.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RequestAuthTarget [subject=" + subject + ", action=" + action + ", container=" + container + "]";
    }

}