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
package org.mozartspaces.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;

/**
 * Thread factory with a configurable prefix for the created thread's names.
 * <p>
 * This is an adapted version of the inner static class DefaultThreadFactory
 * from the class edu.emory.mathcs.backport.java.util.concurrent.Executors (CVS
 * Revision 1.69 from 2008-05-18, 23:47:56 UTC), part of the JSR166 classes
 * available at http://gee.cs.oswego.edu/dl/concurrency-interest/.
 * <p>
 * The original Copyright comment is as follows: "Written by Doug Lea with
 * assistance from members of JCP JSR-166 Expert Group and released to the
 * public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain"
 *
 * @author Doug Lea (original author, not involved in this project)
 * @author Tobias Doenz
 */
@Immutable
public final class NamedThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final ThreadGroup group;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /**
     * Constructs a <code>NamedThreadFactory</code>.
     *
     * @param namePrefix the prefix for the thread's names
     */
    public NamedThreadFactory(final String namePrefix) {
        this.namePrefix = namePrefix;

        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(final Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
