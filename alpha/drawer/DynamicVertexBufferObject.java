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
package drawer;

import com.sun.opengl.util.BufferUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;


class DynamicVertexBufferObject extends DynamicVertexSet{
    
    
    private int[] id;
    
    
    DynamicVertexBufferObject(float[] array,int mode){
        final GL gl=GLU.getCurrentGL();
        this.mode=mode;
        this.buffer=BufferUtil.newFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB,BufferUtil.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STREAM_DRAW_ARB);
        this.buffer.position(0);        
    }
    
    DynamicVertexBufferObject(FloatBuffer floatBuffer,int mode){
        final GL gl=GLU.getCurrentGL();
        this.mode=mode;
        this.buffer=BufferUtil.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB,BufferUtil.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STREAM_DRAW_ARB);
        this.buffer.position(0);       
    }
    
    DynamicVertexBufferObject(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }    
            
    public void draw(){
        final GL gl=GLU.getCurrentGL();
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);       
        buffer.rewind();       
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.rewind();        
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    } 

    public void draw(int start,int count){
        final GL gl=GLU.getCurrentGL();
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);       
        buffer.rewind();
        gl.glDrawArrays(mode,start,count);
        buffer.rewind();        
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }

    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,int limit,boolean relative){
        final GL gl=GLU.getCurrentGL();
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);		
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.glPushMatrix();	     
             gl.glTranslatef(translation.get(),translation.get(),translation.get());
             gl.glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);	     
             buffer.position(0);
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();
        //gl.glUnlockArraysEXT();		
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        rotation.position(0);
        translation.position(0);
    }

    public void multiDraw(FloatBuffer matrix,int limit,boolean relative){  
        final GL gl=GLU.getCurrentGL();
        //matrices in column-major order!!!!!!
        float[] m=new float[16];
        matrix.position(0);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);		
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.glPushMatrix();
             //maybe useless, could I do "gl.glMultMatrixf(matrix)" rather than this?	     
             matrix.get(m,0,16);
             gl.glMultMatrixf(m,0);
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
             buffer.position(0);
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();
        //gl.glUnlockArraysEXT();       
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        matrix.position(0);
    }                

    public void drawByPiece(IntBuffer first,IntBuffer count,int limit){	
        final GL gl=GLU.getCurrentGL();
        first.position(0);
        count.position(0);
        buffer.position(0);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);
        //gl.glLockArraysEXT(0,buffer.capacity());				
        if(DynamicVertexSetFactory.multiDrawSupported)
            gl.glMultiDrawArrays(mode,first,count,limit);
        else
            for(int i=0;i<limit;i++)
                {buffer.position(0);
                 gl.glDrawArrays(mode,first.get(),count.get());
                 buffer.position(0);
                } 	
        //gl.glUnlockArraysEXT();        
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        first.position(0);
        count.position(0);
        buffer.position(0);
    }

    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,IntBuffer first,IntBuffer count,int limit,boolean relative){
        final GL gl=GLU.getCurrentGL();
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);
        first.position(0);
        count.position(0);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB,id[0]);		
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.glPushMatrix();	     
             gl.glTranslatef(translation.get(),translation.get(),translation.get());
             gl.glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             buffer.position(0);
             gl.glDrawArrays(mode,first.get(),count.get());	     
             buffer.position(0);
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();
        //gl.glUnlockArraysEXT();       
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        first.position(0);
        count.position(0);
        rotation.position(0);
        translation.position(0);
    }
}
