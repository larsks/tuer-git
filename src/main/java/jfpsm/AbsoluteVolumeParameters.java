/**
 * Copyright (c) 2006-2015 Julien Gouesse
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map.Entry;

final class AbsoluteVolumeParameters{
	
	
	private final float[] translation;
	
	private VolumeParameters volumeParam;
	
	private String name;
	
	AbsoluteVolumeParameters(){
		translation=new float[3];
	}

    public final float[] getLevelRelativePosition(){
        return(translation);
    }
    
    public final void setLevelRelativePosition(final float x,
            final float y,final float z){
        translation[0]=x;
        translation[1]=y;
        translation[2]=z;
    }

    public final IntBuffer getIndexBuffer(){
        return(volumeParam!=null?volumeParam.getIndexBuffer():null);
    }

    public final IntBuffer getMergeableIndexBuffer(){
        return(volumeParam!=null?volumeParam.getMergeableIndexBuffer():null);
    }
    
    public final FloatBuffer getNormalBuffer(){
        return(volumeParam!=null?volumeParam.getNormalBuffer():null);
    }

    public final FloatBuffer getVertexBuffer(){
        return(volumeParam!=null?volumeParam.getVertexBuffer():null);
    }
    
    public final FloatBuffer getTexCoordBuffer(){
        return(volumeParam!=null?volumeParam.getTexCoordBuffer():null);
    }
    
    public final int getVolumeParamIdentifier(){
        return(volumeParam!=null?volumeParam.hashCode():Integer.MAX_VALUE);
    }
    
    public final boolean isVoid(){
    	return(volumeParam==null);
    }
    
    public final boolean isRemovalOfIdenticalFacesEnabled(){
    	return(volumeParam!=null?volumeParam.isRemovalOfIdenticalFacesEnabled():false);
    }
    
    public final boolean isMergeOfAdjacentFacesEnabled(){
    	return(volumeParam!=null?volumeParam.isMergeOfAdjacentFacesEnabled():false);
    }

    public final void setVolumeParam(final VolumeParameters volumeParam){
        this.volumeParam=volumeParam;
    }
    
    public final int[][][] getVerticesIndicesOfPotentiallyIdenticalFaces(){
        return(volumeParam!=null?volumeParam.getVerticesIndicesOfPotentiallyIdenticalFaces():null);
    }
    
    public final int[][][] getVerticesIndicesOfAdjacentMergeableFaces(){
        return(volumeParam!=null?volumeParam.getVerticesIndicesOfAdjacentMergeableFaces():null);
    }
    
    public final Entry<int[][][],int[][]> getVerticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices(RegularGrid grid){
        return(volumeParam!=null?volumeParam.getVerticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices(grid):null);
    }
    
    public final String getName(){
        return(name);
    }
    
    public final void setName(final String name){
        this.name=name;
    }
}