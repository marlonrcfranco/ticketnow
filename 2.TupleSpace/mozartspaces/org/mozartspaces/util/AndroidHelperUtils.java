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
package org.mozartspaces.util;

/**
 * Helper methods for Android compatibility.
 *
 * @author Tobias Doenz
 */
public final class AndroidHelperUtils {

    /**
     * Checks whether a string is empty, like the method {@link String#isEmpty()}. This helper method is used instead of
     * {@code String.isEmpty}, because the Android API Level 7 does not contain this method, but we want to use this
     * code also for this Android version.
     *
     * @param str
     *            a string
     * @return whether the string is empty
     */
    public static boolean isEmpty(final String str) {
        return str.length() == 0;
    }

    private AndroidHelperUtils() {
    }
}
