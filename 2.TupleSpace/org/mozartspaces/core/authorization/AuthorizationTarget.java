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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mozartspaces.capi3.LocalContainerReference;

/**
 * Specifies the target of a rule.
 *
 * @author Stefan Crass
 * @author Tobias Doenz
 */
public final class AuthorizationTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Set<Subject> subjects;
    private final Set<ContainerAction> actions;
    private final Set<LocalContainerReference> containers;

    /**
     * @param subjects
     *            subjects for which the target matches, {@code null} if valid for all subjects
     * @param actions
     *            actions for which the target matches, {@code null} if valid for all actions
     * @param containers
     *            references to target containers for which the target matches, {@code null} if valid for all
     *            containers
     */
    public AuthorizationTarget(final Set<Subject> subjects, final Set<ContainerAction> actions,
            final Set<LocalContainerReference> containers) {
        this.subjects = (subjects == null) ? null : new HashSet<Subject>(subjects);
        this.actions = (actions == null) ? null : new HashSet<ContainerAction>(actions);
        this.containers = (containers == null) ? null : new HashSet<LocalContainerReference>(containers);
    }

    /**
     * @return the subjects
     */
    public Set<Subject> getSubjects() {
        return Collections.unmodifiableSet(subjects);
    }

    /**
     * @return the actions
     */
    public Set<ContainerAction> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    /**
     * @return the container references
     */
    public Set<LocalContainerReference> getContainers() {
        return Collections.unmodifiableSet(containers);
    }

    /**
     * Checks if data from an active request matches the target.
     *
     * @param requestInfo
     *            the target information of the request
     * @return true if the target matches the request data, false otherwise
     */
    public boolean matchesRequest(final RequestAuthTarget requestInfo) {
        if (actions == null || actions.contains(requestInfo.getAction())) {
            if (containers == null || containers.contains(requestInfo.getContainer())) {
                if (subjects == null || matchesSubjects(subjects, requestInfo.getSubject())) {
                    // if (subjects == null || subjects.contains(requestInfo.getSubject())) {
                    return true;
                }
            }
        }
        return false;
    }

    // the request subject has to match at least one of the target subjects (logical OR)
    private static boolean matchesSubjects(final Set<Subject> targetSubjects, final Subject requestSubject) {
        assert targetSubjects != null;
        if (requestSubject == null) {
            return false;
        }
        for (Subject target : targetSubjects) {
            boolean targetMatches = true;
            for (NamedValue targetAttr : target.getAttributes()) {
                // all attributes have to match
                targetMatches &= requestSubject.getAttributes().contains(targetAttr);
            }
            if (targetMatches) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof AuthorizationTarget) {
            AuthorizationTarget other = (AuthorizationTarget) obj;
            if (other.subjects != null) {
                if (!this.subjects.equals(other.subjects)) {
                    return false;
                }
            } else if (this.subjects != null) {
                return false;
            }
            if (other.actions != null) {
                if (!this.actions.equals(other.actions)) {
                    return false;
                }
            } else if (this.actions != null) {
                return false;
            }
            if (other.containers != null) {
                if (!this.containers.equals(other.containers)) {
                    return false;
                }
            } else if (this.containers != null) {
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
        result = 31 * result + ((subjects == null) ? 0 : subjects.hashCode());
        result = 31 * result + ((actions == null) ? 0 : actions.hashCode());
        result = 31 * result + ((containers == null) ? 0 : containers.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "AuthorizationTarget [subjects=" + subjects + ", actions=" + actions + ", containers=" + containers
                + "]";
    }

}
