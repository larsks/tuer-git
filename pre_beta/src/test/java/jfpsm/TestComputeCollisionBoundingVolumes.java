/**
 * Copyright (c) 2006-2017 Julien Gouesse
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

import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;
import jfpsm.ArrayHelper.OccupancyMap;
import jfpsm.ArrayHelper.Vector2i;
import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.util.resource.URLResourceSource;
import common.EngineServiceProviderInterface;

public class TestComputeCollisionBoundingVolumes {

    public static void main(String[] args) {
        JoglImageLoader.registerLoader();
        final URL mapUrl = TestComputeCollisionBoundingVolumes.class.getResource("/images/containermap5.png");
        final URLResourceSource mapSource = new URLResourceSource(mapUrl);
        final Image map = ImageLoaderUtil.loadImage(mapSource, false);
        final Boolean[][] collisionMap = new Boolean[map.getWidth()][map.getHeight()];
        for (int y = 0; y < map.getHeight(); y++)
            for (int x = 0; x < map.getWidth(); x++) {
                final int argb = ImageUtils.getARGB(map, x, y);
                collisionMap[x][y] = argb == ColorRGBA.BLUE.asIntARGB() ? Boolean.TRUE : null;
            }
        System.out.println("Input: ");
        final ArrayHelper arrayHelper = new ArrayHelper();
        System.out.println(arrayHelper.toString(collisionMap, false, null));
        final OccupancyMap occupancyMap = arrayHelper.createPackedOccupancyMap(collisionMap);
        System.out.println("occupied cell count: " + occupancyMap.getOccupiedCellCount());
        System.out.println("rowCount: " + occupancyMap.getRowCount());
        System.out.println("columnCount: " + occupancyMap.getColumnCount());
        System.out.println("smallestRowIndex: " + occupancyMap.getSmallestRowIndex());
        System.out.println("biggestRowIndex: " + occupancyMap.getBiggestRowIndex());
        System.out.println("smallestColumnIndex: " + occupancyMap.getSmallestColumnIndex());
        System.out.println("biggestColumnIndex: " + occupancyMap.getBiggestColumnIndex());

        boolean occupancyMapConsistentWithCollisionMap = true;
        final int smallestColumnIndex = occupancyMap.getSmallestColumnIndex();
        final int smallestRowIndex = occupancyMap.getSmallestRowIndex();
        for (int y = 0; y < map.getHeight() && occupancyMapConsistentWithCollisionMap; y++)
            for (int x = 0; x < map.getWidth() && occupancyMapConsistentWithCollisionMap; x++) {
                final boolean cellInArray = 0 <= x - smallestColumnIndex
                        && x - smallestColumnIndex < occupancyMap.getColumnCount()
                        && occupancyMap.hasNonNullColumn(x - smallestColumnIndex) && 0 <= y - smallestRowIndex
                        && y - smallestRowIndex < occupancyMap.getRowCount(x - smallestColumnIndex);
                if (collisionMap[x][y] != null && collisionMap[x][y].booleanValue()) {
                    // checks whether there is a cell at these coordinates and
                    // the cell is occupied
                    occupancyMapConsistentWithCollisionMap &= cellInArray
                            && occupancyMap.getValue(x - smallestColumnIndex, y - smallestRowIndex);
                } else {
                    // checks whether there is no cell at these coordinates or
                    // the cell isn't occupied
                    occupancyMapConsistentWithCollisionMap &= !cellInArray
                            || !occupancyMap.getValue(x - smallestColumnIndex, y - smallestRowIndex);
                }
            }
        System.out.println("Occupancy check: " + (occupancyMapConsistentWithCollisionMap ? "OK" : "NOK"));
        final long startTime = System.currentTimeMillis();
        System.out.println("Start");
        final java.util.Map<Vector2i, Boolean[][]> fullArrayMap = arrayHelper
                .computeFullArraysFromNonFullArray(collisionMap, occupancyMap);
        final long durationInMilliseconds = System.currentTimeMillis() - startTime;
        System.out.println("Occupied cell count: " + occupancyMap.getOccupiedCellCount());
        if (occupancyMap.getOccupiedCellCount() > 0) {
            for (int columnIndex = 0; columnIndex < occupancyMap.getColumnCount(); columnIndex++) {
                if (occupancyMap.hasNonNullColumn(columnIndex)) {
                    for (int rowIndex = 0; rowIndex < occupancyMap.getRowCount(columnIndex); rowIndex++) {
                        if (occupancyMap.getValue(columnIndex, rowIndex)) {
                            System.out.println("Occupied cell at [" + columnIndex + "][" + rowIndex + "]");
                        }
                    }
                }
            }
        }
        System.out.println("Output:");
        System.out.println(arrayHelper.toString(fullArrayMap, map.getHeight(), map.getWidth()));
        System.out.println("End. Duration: " + durationInMilliseconds + " ms");

        // computes the bounding boxes
        final EngineServiceProviderInterface<?, ?, ?, ?, ?> seeker = ServiceLoader
                .load(EngineServiceProviderInterface.class).iterator().next();
        List<?> boundingBoxList = new VolumeHelper(seeker).computeBoundingBoxListFromFullArrayMap(fullArrayMap, 0, 1,
                false, false);
        System.out.println("Bounding boxes: " + boundingBoxList.size());
    }
}
