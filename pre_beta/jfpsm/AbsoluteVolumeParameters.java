/**
 * 
 */
package jfpsm;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    
    public final boolean isMergeEnabled(){
    	return(volumeParam!=null?volumeParam.isMergeEnabled():false);
    }

    public final void setVolumeParam(final VolumeParameters volumeParam){
        this.volumeParam=volumeParam;
    }
    
    public final int[][][] getVerticesIndicesOfMergeableFaces(){
        return(volumeParam!=null?volumeParam.getVerticesIndicesOfMergeableFaces():null);
    }
    
    public final String getName(){
        return(name);
    }
    
    public final void setName(final String name){
        this.name=name;
    }
}