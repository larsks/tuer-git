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
    /**texture coordinates sorted by side*/
    private float[][] texCoord;
    
    public enum Side{BACK,RIGHT,FRONT,LEFT,TOP,BOTTOM};
    
    public enum Orientation{OUTWARDS,INWARDS,NONE};
    
    private Orientation[] faceOrientation;
    
    private transient FloatBuffer vertexBuffer;
    
    private transient FloatBuffer normalBuffer;
    
    private transient IntBuffer indexBuffer;
    
    private transient FloatBuffer texCoordBuffer;
    
    
    public CuboidParameters(){
        this(new float[]{0,0,0},new float[]{1,1,1},
             new Orientation[]{Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS,Orientation.OUTWARDS},
             new float[][]{new float[]{0,1,0,1},new float[]{0,1,0,1},new float[]{0,1,0,1},new float[]{0,1,0,1},new float[]{0,1,0,1},new float[]{0,1,0,1}});
    }
    
    public CuboidParameters(final float[] offset,
            final float[] size,final Orientation[] faceOrientation,
            final float[][] texCoord){
        this.offset=offset;
        this.size=size;
        this.texCoord=texCoord;
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
    
    private final void recomputeBuffersIfNeeded(){
        if(buffersRecomputationNeeded)
            {int visibleFacesCount=0;
             for(int i=0;i<6;i++)
                 if(faceOrientation[i]!=Orientation.NONE)
                     visibleFacesCount++;
        	 if(vertexBuffer==null||vertexBuffer.capacity()!=visibleFacesCount*12)
                 {//6 faces * 4 vertices * 3 coordinates
                  vertexBuffer=BufferUtil.newFloatBuffer(visibleFacesCount*12);                 
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
             if(faceOrientation[Side.BACK.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[0]);
                  vertexBuffer.put(vertices[1]);
                  vertexBuffer.put(vertices[2]);
                  vertexBuffer.put(vertices[3]);
                 }
             // Right
             if(faceOrientation[Side.RIGHT.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[1]);
                  vertexBuffer.put(vertices[4]);
                  vertexBuffer.put(vertices[6]);
                  vertexBuffer.put(vertices[2]);
                 }
             // Front
             if(faceOrientation[Side.FRONT.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[4]);
                  vertexBuffer.put(vertices[5]);
                  vertexBuffer.put(vertices[7]);
                  vertexBuffer.put(vertices[6]);
                 }
             // Left
             if(faceOrientation[Side.LEFT.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[5]);
                  vertexBuffer.put(vertices[0]);
                  vertexBuffer.put(vertices[3]);
                  vertexBuffer.put(vertices[7]);
                 }
             // Top
             if(faceOrientation[Side.TOP.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[2]);
                  vertexBuffer.put(vertices[6]);
                  vertexBuffer.put(vertices[7]);
                  vertexBuffer.put(vertices[3]);
                 }
             // Bottom
             if(faceOrientation[Side.BOTTOM.ordinal()]!=Orientation.NONE)
                 {vertexBuffer.put(vertices[0]);
                  vertexBuffer.put(vertices[5]);
                  vertexBuffer.put(vertices[4]);
                  vertexBuffer.put(vertices[1]);                  
                 }
             vertexBuffer.rewind();
             if(indexBuffer==null||indexBuffer.capacity()!=visibleFacesCount*6)
                 {//6 faces * 2 triangles * 3 indices
                  indexBuffer=BufferUtil.newIntBuffer(visibleFacesCount*6);
                 }
             //fill the index buffer
             indexBuffer.rewind();
             int indexOffset=0;
             for(Side side:Side.values())
                  {if(faceOrientation[side.ordinal()]==Orientation.OUTWARDS)
                	   {indexBuffer.put(2+indexOffset).put(1+indexOffset).put(0+indexOffset).put(3+indexOffset).put(2+indexOffset).put(0+indexOffset);
                	    indexOffset+=4;
                	   }
                   else
                	   if(faceOrientation[side.ordinal()]==Orientation.INWARDS)
                		   {indexBuffer.put(0+indexOffset).put(1+indexOffset).put(2+indexOffset).put(0+indexOffset).put(2+indexOffset).put(3+indexOffset);
                		    indexOffset+=4;
                		   }
                  }
             indexBuffer.rewind();
             if(normalBuffer==null||normalBuffer.capacity()!=visibleFacesCount*12)
                 {//6 faces * 4 vertices * 3 coordinates
                  normalBuffer=BufferUtil.newFloatBuffer(visibleFacesCount*12);
                 }            
             //fill the normal buffer
             normalBuffer.rewind();
             int value;
             if(faceOrientation[Side.BACK.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.BACK.ordinal()]==Orientation.OUTWARDS?-1:1;
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);           	  
                 }
             if(faceOrientation[Side.RIGHT.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.RIGHT.ordinal()]==Orientation.OUTWARDS?1:-1;
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                 }
             if(faceOrientation[Side.FRONT.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.FRONT.ordinal()]==Orientation.OUTWARDS?1:-1;
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);
                  normalBuffer.put(0).put(0).put(value);
                 }
             if(faceOrientation[Side.LEFT.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.LEFT.ordinal()]==Orientation.OUTWARDS?-1:1;
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                  normalBuffer.put(value).put(0).put(0);
                 }
             if(faceOrientation[Side.TOP.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.TOP.ordinal()]==Orientation.OUTWARDS?1:-1;
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                 }
             if(faceOrientation[Side.BOTTOM.ordinal()]!=Orientation.NONE)
                 {value=faceOrientation[Side.BOTTOM.ordinal()]==Orientation.OUTWARDS?-1:1;
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                  normalBuffer.put(0).put(value).put(0);
                 }
             normalBuffer.rewind();
             if(texCoordBuffer==null||texCoordBuffer.capacity()!=visibleFacesCount*8)
                 {//6 faces * 4 vertices * 2 coordinates
                  texCoordBuffer=BufferUtil.newFloatBuffer(visibleFacesCount*8);
                 }
             texCoordBuffer.rewind();
             //fill the texture coord buffer
             float u0,u1,v0,v1;
             for(Side side:Side.values())
            	 if(faceOrientation[side.ordinal()]!=Orientation.NONE)
                     {u0=texCoord[side.ordinal()][0];
                      u1=texCoord[side.ordinal()][1];
                      v0=texCoord[side.ordinal()][2];
                      v1=texCoord[side.ordinal()][3];
                      texCoordBuffer.put(u1).put(v0);
                      texCoordBuffer.put(u0).put(v0);
                      texCoordBuffer.put(u0).put(v1);
                      texCoordBuffer.put(u1).put(v1);
                     }
             texCoordBuffer.rewind();
             buffersRecomputationNeeded=false;
            }
    }
    
    public final void setOrientation(Side side,Orientation orientation){
        faceOrientation[side.ordinal()]=orientation;
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    public final Orientation getOrientation(Side side){
        return(faceOrientation[side.ordinal()]);
    }
    
    public final Orientation[] getFaceOrientation(){
        return(faceOrientation);
    }
    
    public final void setFaceOrientation(final Orientation[] faceOrientation){
        this.faceOrientation=faceOrientation;
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    public final float[][] getTexCoord(){
        return(texCoord);
    }
    
    public final float getTexCoord(final Side side,final int texCoordIndex){
        return(texCoord[side.ordinal()][texCoordIndex]);
    }
    
    public final void setTexCoord(final Side side,final int texCoordIndex,final float value){
        this.texCoord[side.ordinal()][texCoordIndex]=value;
        buffersRecomputationNeeded=true;
        markDirty();
    }
    
    public final void setTexCoord(final float[][] texCoord){
        this.texCoord=texCoord;
        buffersRecomputationNeeded=true;
        markDirty();
    }

    @Override
    public final IntBuffer getIndexBuffer(){
        recomputeBuffersIfNeeded();
        return(indexBuffer);
    }

    @Override
    public final FloatBuffer getNormalBuffer(){
        recomputeBuffersIfNeeded();
        return(normalBuffer);
    }

    @Override
    public final FloatBuffer getVertexBuffer(){
        recomputeBuffersIfNeeded();
        return(vertexBuffer);
    }
    
    @Override
    public final FloatBuffer getTexCoordBuffer(){
        recomputeBuffersIfNeeded();
        return(texCoordBuffer);
    }
}