/**
 * Copyright (c) 2006-2019 Julien Gouesse
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
 * finite grid with sections of the same size
 * 
 * @author Julien Gouesse
 *
 */
public class RegularGrid extends FiniteGrid {

    private float sectionPhysicalWidth;

    private float sectionPhysicalHeight;

    private float sectionPhysicalDepth;

    public RegularGrid(final int logicalWidth, final int logicalHeight, final int logicalDepth,
            final float sectionPhysicalWidth, final float sectionPhysicalHeight, final float sectionPhysicalDepth) {
        super(logicalWidth, logicalHeight, logicalDepth);
        this.sectionPhysicalWidth = sectionPhysicalWidth;
        this.sectionPhysicalHeight = sectionPhysicalHeight;
        this.sectionPhysicalDepth = sectionPhysicalDepth;
    }

    @Override
    public float[] getSectionPhysicalPosition(final int i, final int j, final int k) {
        final float[] result;
        if (0 <= i && i < getLogicalWidth() && 0 <= j && j < getLogicalHeight() && 0 <= k && k < getLogicalDepth())
            result = new float[] { i * sectionPhysicalWidth, j * sectionPhysicalHeight, k * sectionPhysicalDepth };
        else
            result = null;
        return (result);
    }

    @Override
    public int[] getSectionLogicalPosition(float x, float y, float z) {
        int i = (int) Math.floor(x / sectionPhysicalWidth);
        int j = (int) Math.floor(y / sectionPhysicalHeight);
        int k = (int) Math.floor(z / sectionPhysicalDepth);
        final int[] result;
        if (0 <= i && i < getLogicalWidth() && 0 <= j && j < getLogicalHeight() && 0 <= k && k < getLogicalDepth())
            result = new int[] { i, j, k };
        else
            result = null;
        return (result);
    }

    @Override
    public final float getSectionPhysicalWidth(final int i, final int j, final int k) {
        return (getSectionPhysicalWidth());
    }

    public final float getSectionPhysicalWidth() {
        return (sectionPhysicalWidth);
    }

    @Override
    public final float getSectionPhysicalHeight(final int i, final int j, final int k) {
        return (getSectionPhysicalHeight());
    }

    public final float getSectionPhysicalHeight() {
        return (sectionPhysicalHeight);
    }

    @Override
    public final float getSectionPhysicalDepth(final int i, final int j, final int k) {
        return (getSectionPhysicalDepth());
    }

    public final float getSectionPhysicalDepth() {
        return (sectionPhysicalDepth);
    }

    public final float getSectionPhysicalSize(int coordIndex) {
        if (0 > coordIndex || coordIndex > 2)
            throw new IllegalArgumentException("the coordinate index should");
        float value;
        switch (coordIndex) {
        case 0: {
            value = getSectionPhysicalWidth();
            break;
        }
        case 1: {
            value = getSectionPhysicalHeight();
            break;
        }
        case 2: {
            value = getSectionPhysicalDepth();
            break;
        }
        default:
            value = Float.NaN;
        }
        return (value);
    }
}
