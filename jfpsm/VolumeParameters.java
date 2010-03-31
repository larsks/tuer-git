/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package jfpsm;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import misc.SerializationHelper;

public abstract class VolumeParameters implements Serializable,Dirtyable {

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(VolumeParameters.class);}
    
    private static final long serialVersionUID=1L;
    
    private boolean mergeEnabled;
    
    abstract VolumeType getVolumeType();
    
    abstract IntBuffer getIndexBuffer();
    
    abstract IntBuffer getMergeableIndexBuffer();

    abstract FloatBuffer getNormalBuffer();

    abstract FloatBuffer getVertexBuffer();
    
    abstract FloatBuffer getTexCoordBuffer();

	public final boolean isMergeEnabled(){
		return(mergeEnabled);
	}

	public final void setMergeEnabled(final boolean mergeEnabled){
		this.mergeEnabled=mergeEnabled;
		markDirty();
	}
	
	/**
	 * get the vertices indices of faces whose vertices might be equal
	 * @return vertices indices of faces whose vertices might be equal
	 */
	public int[][][] getVerticesIndicesOfMergeableFaces(){
	    /**
	     * It should perform the following operations:
	     * - compute the real normals by using the vertices
	     * - detect the opposed faces
	     * - compute the distances between the pairs of vertices
	     * - detect equivalent triangles
	     * */
	    return(null);
	}
	
	/**
	 * get the vertices indices of faces whose vertices might be adjacent
	 * @return vertices indices of faces whose vertices might be adjacent
	 */
	public int[][][] getVerticesIndicesOfAdjacentMergeableFaces(){
	    /**
	     * It should perform the following operations:
	     * - for each coordinate type, check if all coordinates are equal
	     * - if so, check if there is any adjacency in u, v or u & v
	     * */
	    return(null);
	}
	
	/**
	 * get the vertex coordinates of the extreme values of the bounding boxes
	 * @param containerBoundingBox bounding box that contains the volume
	 * @return vertex coordinates of the extreme values of the bounding boxes
	 */
	public float[][][] getAxisAlignedBoundingBoxesExtremeCoordinates(final float[][] containerBoundingBox){
	    return(null);
	}
}
