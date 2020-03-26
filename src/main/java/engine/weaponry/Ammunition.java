/**
 * Copyright (c) 2006-2020 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.weaponry;

public class Ammunition implements Comparable<Ammunition> {

    /** name (can contain space) */
    private final String label;

    public Ammunition(final String label) {
        super();
        this.label = label;
    }

    @Override
    public int hashCode() {
        return (label.hashCode());
    }

    public String getLabel() {
        return (label);
    }

    @Override
    public String toString() {
        return (label);
    }

    @Override
    public int compareTo(final Ammunition ammunition) {
        return (label.compareTo(ammunition.label));
    }

    @Override
    public boolean equals(final Object o) {
        final boolean result;
        if (o == null || !(o instanceof Ammunition))
            result = false;
        else {
            final Ammunition ammunition = (Ammunition) o;
            result = getLabel().equals(ammunition.getLabel());
        }
        return (result);
    }
}
