/**
 * Copyright (c) 2006-2016 Julien Gouesse
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
package jfpsm;

/**
 * logical spatial abstract data type
 * 
 * @author Julien Gouesse
 *
 */
public interface Grid {

    /**
     * get the physical position of a section from its logical position
     * 
     * @param i
     *            logical abscissa
     * @param j
     *            logical ordinate
     * @param k
     *            logical applicate
     * @return physical position of a section
     */
    public float[] getSectionPhysicalPosition(int i, int j, int k);

    public int[] getSectionLogicalPosition(float x, float y, float z);

    public int getLogicalWidth();

    public int getLogicalHeight();

    public int getLogicalDepth();

    public float getSectionPhysicalWidth(int i, int j, int k);

    public float getSectionPhysicalHeight(int i, int j, int k);

    public float getSectionPhysicalDepth(int i, int j, int k);
}
