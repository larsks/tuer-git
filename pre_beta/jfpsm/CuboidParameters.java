package jfpsm;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.sun.opengl.util.BufferUtil;
import misc.SerializationHelper;

public final class CuboidParameters extends VolumeParameters{

    
    private static final long serialVersionUID = 1L;

    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(CuboidParameters.class);}
    
    private transient boolean dirty;
    
    private transient boolean buffersRecomputationNeeded;
    
    private float[] offset;
    
    private float[] size;
    
    public enum Side{BACK,RIGHT,FRONT,LEFT,TOP,BOTTOM};
    
    public enum Orientation{OUTWARDS,INWARDS,NONE};
    
    private Orientation[] faceOrientation;
    
    private transient FloatBuffer vertexBuffer;
    
    private transient FloatBuffer normalBuffer;
    
    private transient IntBuffer indexBuffer;
    
    private transient FloatBuffer texCoordBuffer;
    
    
    public CuboidParameters(){
        this(new float[]{0,0,0},new float[]{1,1,1},new Orientation[]{Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS});
    }
    
    public CuboidParameters(float[] offset,float[] size,Orientation[] faceOrientation){
        this.offset=offset;
        this.size=size;
        this.faceOrientation=faceOrientation;
        //6 faces * 4 vertices * 3 coordinates
        this.vertexBuffer=BufferUtil.newFloatBuffer(72);
        this.normalBuffer=BufferUtil.newFloatBuffer(72);
        //6 faces * 2 triangles * 3 indices
        this.indexBuffer=BufferUtil.newIntBuffer(36);
        this.texCoordBuffer=BufferUtil.newFloatBuffer(72);
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    
    @Override
    final VolumeType getVolumeType(){
        return(VolumeType.CUBOID);
    }

    @Override
    public final boolean isDirty(){
        return(dirty);
    }

    @Override
    public final void markDirty(){
        dirty=true;
    }

    @Override
    public final void unmarkDirty(){
        dirty=false;
    }

    public final float[] getOffset(){
        return(offset);
    }

    public final void setOffset(float[] offset){
        this.offset=offset;
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    public final void setOffset(int index,float value){
        this.offset[index]=value;
        buffersRecomputationNeeded=true;
        markDirty();
    }

    public final float[] getSize(){
        return(size);
    }

    public final void setSize(float[] size){
        this.size=size;
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    public final void setSize(int index,float value){
        this.size[index]=value;
        buffersRecomputationNeeded=true;
        markDirty();
    }
	
	private void recomputeBuffersIfNeeded(){
	    if(buffersRecomputationNeeded)
	        {if(vertexBuffer==null)
	            {//6 faces * 4 vertices * 3 coordinates
	             vertexBuffer=BufferUtil.newFloatBuffer(72);	             
	            }	         
	         float[] center=new float[3];
	         for(int i=0;i<3;i++)
	             center[i]=(size[i]/2)+offset[i];
	         float[][] vertices=new float[8][3];
	         vertices[0]=new float[]{center[0]-size[0]/2,center[1]-size[1]/2,center[2]-size[2]/2};
	         vertices[1]=new float[]{center[0]+size[0]/2,center[1]-size[1]/2,center[2]-size[2]/2};
	         vertices[2]=new float[]{center[0]+size[0]/2,center[1]+size[1]/2,center[2]-size[2]/2};
	         vertices[3]=new float[]{center[0]-size[0]/2,center[1]+size[1]/2,center[2]-size[2]/2};	         
	         vertices[4]=new float[]{center[0]+size[0]/2,center[1]-size[1]/2,center[2]+size[2]/2};
	         vertices[5]=new float[]{center[0]-size[0]/2,center[1]-size[1]/2,center[2]+size[2]/2};
	         vertices[6]=new float[]{center[0]+size[0]/2,center[1]+size[1]/2,center[2]+size[2]/2};
	         vertices[7]=new float[]{center[0]-size[0]/2,center[1]+size[1]/2,center[2]+size[2]/2};
             //fill the vertex buffer
	         vertexBuffer.rewind();
	         // Back
	         vertexBuffer.put(vertices[0]);
	         vertexBuffer.put(vertices[1]);
	         vertexBuffer.put(vertices[2]);
	         vertexBuffer.put(vertices[3]);
	         // Right
	         vertexBuffer.put(vertices[1]);
             vertexBuffer.put(vertices[4]);
             vertexBuffer.put(vertices[6]);
             vertexBuffer.put(vertices[2]);
             // Front
             vertexBuffer.put(vertices[4]);
             vertexBuffer.put(vertices[5]);
             vertexBuffer.put(vertices[7]);
             vertexBuffer.put(vertices[6]);
             // Left
             vertexBuffer.put(vertices[5]);
             vertexBuffer.put(vertices[0]);
             vertexBuffer.put(vertices[3]);
             vertexBuffer.put(vertices[7]);
             // Top
             vertexBuffer.put(vertices[2]);
             vertexBuffer.put(vertices[6]);
             vertexBuffer.put(vertices[7]);
             vertexBuffer.put(vertices[3]);
             // Bottom
             vertexBuffer.put(vertices[0]);
             vertexBuffer.put(vertices[5]);
             vertexBuffer.put(vertices[4]);
             vertexBuffer.put(vertices[1]);
             vertexBuffer.rewind();
             int visibleFacesCount=0;
             for(int i=0;i<6;i++)
                 if(faceOrientation[i]!=Orientation.NONE)
                     visibleFacesCount++;
             if(indexBuffer==null||indexBuffer.capacity()!=visibleFacesCount*6)
                 {//6 faces * 2 triangles * 3 indices
                  indexBuffer=BufferUtil.newIntBuffer(visibleFacesCount*6);
                 }
             //fill the index buffer
             indexBuffer.rewind();
             if(faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(2).put(1).put(0).put(3).put(2).put(0);
             else
                 if(faceOrientation[Side.BACK.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(0).put(1).put(2).put(0).put(2).put(3);
             if(faceOrientation[Side.RIGHT.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(6).put(5).put(4).put(7).put(6).put(4);
             else 
                 if(faceOrientation[Side.RIGHT.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(4).put(5).put(6).put(4).put(6).put(7);
             if(faceOrientation[Side.FRONT.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(10).put(9).put(8).put(11).put(10).put(8);
             else
                 if(faceOrientation[Side.FRONT.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(8).put(9).put(10).put(8).put(10).put(11);
             if(faceOrientation[Side.LEFT.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(14).put(13).put(12).put(15).put(14).put(12);
             else
                 if(faceOrientation[Side.LEFT.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(12).put(13).put(14).put(12).put(14).put(15);
             if(faceOrientation[Side.TOP.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(18).put(17).put(16).put(19).put(18).put(16);
             else
                 if(faceOrientation[Side.TOP.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(16).put(17).put(18).put(16).put(18).put(19);
             if(faceOrientation[Side.BOTTOM.ordinal()]==Orientation.OUTWARDS)
                 indexBuffer.put(22).put(21).put(20).put(23).put(22).put(20);
             else
                 if(faceOrientation[Side.BOTTOM.ordinal()]==Orientation.INWARDS)
                     indexBuffer.put(20).put(21).put(22).put(20).put(22).put(23);
             indexBuffer.rewind();
             if(normalBuffer==null)
                 {//6 faces * 4 vertices * 3 coordinates
                  normalBuffer=BufferUtil.newFloatBuffer(72);
                 }            
             //fill the normal buffer
             normalBuffer.rewind();
             int value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?-1:1;
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);    
             value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?1:-1;
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?1:-1;
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);
             normalBuffer.put(0).put(0).put(value);
             value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?-1:1;
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             normalBuffer.put(value).put(0).put(0);
             value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?1:-1;
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?-1:1;
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.put(0).put(value).put(0);
             normalBuffer.rewind();
             if(texCoordBuffer==null)
                 {//6 faces * 4 vertices * 3 coordinates
                  texCoordBuffer=BufferUtil.newFloatBuffer(72);
                 }
             texCoordBuffer.rewind();
             //fill the texture coord buffer
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.put(1).put(0);
             texCoordBuffer.put(0).put(0);
             texCoordBuffer.put(0).put(1);
             texCoordBuffer.put(1).put(1);
             
             texCoordBuffer.rewind();
	         buffersRecomputationNeeded=false;
	        }
	}
	
	public void setOrientation(Side side,Orientation orientation){
	    faceOrientation[side.ordinal()]=orientation;
	    buffersRecomputationNeeded=true;
	}
	
	@Override
    public final IntBuffer getIndexBuffer(){
        recomputeBuffersIfNeeded();
        return(indexBuffer);
    }

    @Override
    public FloatBuffer getNormalBuffer(){
        recomputeBuffersIfNeeded();
        return(normalBuffer);
    }

    @Override
    public FloatBuffer getVertexBuffer(){
        recomputeBuffersIfNeeded();
        return(vertexBuffer);
    }
    
    @Override
    public FloatBuffer getTexCoordBuffer(){
        recomputeBuffersIfNeeded();
        return(texCoordBuffer);
    }
}
