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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import common.EngineServiceProviderInterface;
import jfpsm.ArrayHelper.Vector2i;

/**
 * Helper to compute the bounding volumes
 * 
 * @author Julien Gouesse
 *
 */
public class VolumeHelper {

    private final EngineServiceProviderInterface<?, ?, ?, ?, ?> seeker;

    public VolumeHelper(final EngineServiceProviderInterface<?, ?, ?, ?, ?> seeker) {
        super();
        this.seeker = seeker;
    }

    /**
     * Computes the 3D bounding boxes from a map of 2D full arrays
     * 
     * @param fullArrayMap
     *            map of full arrays
     * @param z0
     *            first applicate of the bounding boxes for the main layer
     * @param z1
     *            second applicate of the bounding boxes for the main layer
     * @param generateFloorBoundingBox
     *            <code>true</code> if a single bounding box below the main
     *            layer has to be computed, otherwise <code>false</code>
     * @param generateCeilingBoundingBox
     *            <code>true</code> if a single bounding box above the main
     *            layer has to be computed, otherwise <code>false</code>
     * @return list of 3D bounding boxes
     */
    public <T> List<Object> computeBoundingBoxListFromFullArrayMap(final java.util.Map<Vector2i, T[][]> fullArrayMap,
            final double z0, final double z1, final boolean generateFloorBoundingBox,
            final boolean generateCeilingBoundingBox) {
        final List<Object> boundingBoxList = new ArrayList<>();
        final Vector2i zero = new Vector2i(0, 0);
        int smallestRowIndex = Integer.MAX_VALUE;
        int biggestRowIndex = Integer.MIN_VALUE;
        int smallestColumnIndex = Integer.MAX_VALUE;
        int biggestColumnIndex = Integer.MIN_VALUE;
        // extent of the applicates
        final double zExtent = Math.abs(z1 - z0) / 2.0;
        // for each full array
        for (final Entry<Vector2i, T[][]> fullArrayMapEntry : fullArrayMap.entrySet()) {
            final Vector2i location = fullArrayMapEntry.getKey() == null ? zero : fullArrayMapEntry.getKey();
            final T[][] fullArray = fullArrayMapEntry.getValue();
            if (fullArray != null) {
                final int fullArrayColumnCount = fullArray.length;
                // all columns have the same size
                final int fullArrayRowCount = fullArrayColumnCount == 0 ? 0
                        : fullArray[0] == null ? 0 : fullArray[0].length;
                if (fullArrayColumnCount > 0 && fullArrayRowCount > 0) {// computes
                                                                        // the
                                                                        // minimum
                                                                        // and
                                                                        // maximum
                                                                        // coordinates
                                                                        // abscissa
                    final int x0 = location.getX();
                    final int x1 = x0 + fullArrayColumnCount;
                    // ordinate
                    final int y0 = location.getY();
                    final int y1 = y0 + fullArrayRowCount;
                    // updates the minimum indices and the maximum indices
                    smallestColumnIndex = Math.min(smallestColumnIndex, x0);
                    smallestRowIndex = Math.min(smallestRowIndex, y0);
                    biggestColumnIndex = Math.max(biggestColumnIndex, x1);
                    biggestRowIndex = Math.max(biggestRowIndex, y1);
                    // other extents
                    final double xExtent = Math.abs(x1 - x0) / 2.0;
                    final double yExtent = Math.abs(y1 - y0) / 2.0;
                    // center
                    final double xCenter = (x0 + x1) / 2.0;
                    final double yCenter = (y0 + y1) / 2.0;
                    final double zCenter = (z0 + z1) / 2.0;
                    // bounding box
                    final Object boundingBox = seeker.createBoundingBox(xCenter, yCenter, zCenter, xExtent, yExtent,
                            zExtent);
                    // adds the bounding box into the list
                    boundingBoxList.add(boundingBox);
                }
            }
        }
        if (!boundingBoxList.isEmpty() && (generateFloorBoundingBox || generateCeilingBoundingBox)) {
            final double x2 = smallestColumnIndex;
            final double x3 = biggestColumnIndex;
            final double y2 = smallestRowIndex;
            final double y3 = biggestRowIndex;
            if (generateFloorBoundingBox) {
                final double z2 = Math.min(z0, z1);
                final double z3 = z2 - (zExtent * 2.0);
                final double xExtent = Math.abs(x2 - x3) / 2.0;
                final double yExtent = Math.abs(y2 - y3) / 2.0;
                final double xCenter = (x2 + x3) / 2.0;
                final double yCenter = (y2 + y3) / 2.0;
                final double zCenter = (z2 + z3) / 2.0;
                final Object boundingBox = seeker.createBoundingBox(xCenter, yCenter, zCenter, xExtent, yExtent,
                        zExtent);
                boundingBoxList.add(boundingBox);
            }
            if (generateCeilingBoundingBox) {
                final double z2 = Math.max(z0, z1);
                final double z3 = z2 + (zExtent * 2.0);
                final double xExtent = Math.abs(x2 - x3) / 2.0;
                final double yExtent = Math.abs(y2 - y3) / 2.0;
                final double xCenter = (x2 + x3) / 2.0;
                final double yCenter = (y2 + y3) / 2.0;
                final double zCenter = (z2 + z3) / 2.0;
                final Object boundingBox = seeker.createBoundingBox(xCenter, yCenter, zCenter, xExtent, yExtent,
                        zExtent);
                boundingBoxList.add(boundingBox);
            }
        }
        return (boundingBoxList);
    }
}
