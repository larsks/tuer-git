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

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map.Entry;

public abstract class VolumeParameters implements Serializable, Dirtyable {

    private static final long serialVersionUID = 1L;

    private boolean removalOfIdenticalFacesEnabled;

    private boolean mergeOfAdjacentFacesEnabled;

    abstract VolumeType getVolumeType();

    abstract IntBuffer getIndexBuffer();

    abstract IntBuffer getMergeableIndexBuffer();

    abstract FloatBuffer getNormalBuffer();

    abstract FloatBuffer getVertexBuffer();

    abstract FloatBuffer getTexCoordBuffer();

    /**
     * tell whether the removal of layered identical faces is enabled
     * 
     * @return
     */
    public final boolean isRemovalOfIdenticalFacesEnabled() {
        return (removalOfIdenticalFacesEnabled);
    }

    /**
     * enable or disable the removal of layered identical faces
     * 
     * @param removalOfIdenticalFacesEnabled
     */
    public final void setRemovalOfIdenticalFacesEnabled(final boolean removalOfIdenticalFacesEnabled) {
        this.removalOfIdenticalFacesEnabled = removalOfIdenticalFacesEnabled;
        markDirty();
    }

    /**
     * tell whether the merge of adjacent faces is enabled
     * 
     * @return
     */
    public final boolean isMergeOfAdjacentFacesEnabled() {
        return (mergeOfAdjacentFacesEnabled);
    }

    /**
     * enable or disable the merge of adjacent faces
     * 
     * @param mergeOfAdjacentFacesEnabled
     */
    public final void setMergeOfAdjacentFacesEnabled(final boolean mergeOfAdjacentFacesEnabled) {
        this.mergeOfAdjacentFacesEnabled = mergeOfAdjacentFacesEnabled;
        markDirty();
    }

    /**
     * get the vertices indices of faces whose vertices might be equal
     * 
     * @return vertices indices of faces whose vertices might be equal
     */
    public int[][][] getVerticesIndicesOfPotentiallyIdenticalFaces() {
        /**
         * It should perform the following operations: - compute the real
         * normals by using the vertices - detect the opposed faces - compute
         * the distances between the pairs of vertices - detect equivalent
         * triangles
         */
        return (null);
    }

    /**
     * get the vertices indices of faces whose vertices might be adjacent
     * 
     * @return vertices indices of faces whose vertices might be adjacent
     */
    public int[][][] getVerticesIndicesOfAdjacentMergeableFaces() {
        /**
         * It should perform the following operations: - for each coordinate
         * type, check if all coordinates are equal - if so, check if there is
         * any adjacency in u, v or u & v
         */
        return (null);
    }

    /**
     * get the vertices indices of faces whose vertices might be adjacent and
     * the adjacency coordinates indices of these faces and located on the
     * border of a grid section
     * 
     * @param grid
     *            regular grid
     * @return vertices indices of faces whose vertices might be adjacent and
     *         located on the border of a grid section and the adjacency
     *         coordinates indices of these faces
     */
    public Entry<int[][][], int[][]> getVerticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices(
            RegularGrid grid) {
        return (null);
    }

    /**
     * get the vertex coordinates of the extreme values of the bounding boxes
     * 
     * @param containerBoundingBox
     *            bounding box that contains the volume
     * @return vertex coordinates of the extreme values of the bounding boxes
     */
    public float[][][] getAxisAlignedBoundingBoxesExtremeCoordinates(final float[][] containerBoundingBox) {
        return (null);
    }
}
