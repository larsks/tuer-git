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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import com.jogamp.common.nio.Buffers;


class VertexArray extends DynamicVertexSet{


    VertexArray(float[] array,int mode){
        this.mode=mode;
        this.buffer=Buffers.newDirectFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);		
    }
    
    VertexArray(FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.buffer=Buffers.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);    
    }

    VertexArray(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }
    
    
    public void draw(){    
        final GL gl=GLContext.getCurrentGL();
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);	
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);              
        buffer.rewind();       
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.rewind();       
	    gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	    //gl.getGL2().glDisableClientState(GL2.GL_NORMAL_ARRAY);
	    gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }
    
    public void draw(int start,int count){
        final GL gl=GLContext.getCurrentGL();
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);	
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);        
        buffer.rewind();
        gl.glDrawArrays(mode,start,count);
        buffer.rewind();        
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }      
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,int limit,boolean relative){
        final GL gl=GLContext.getCurrentGL();
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);		
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.getGL2().glPushMatrix();
	    for(int i=0;i<limit;i++)
            {if(!relative)
	             gl.getGL2().glPushMatrix();	     
             gl.getGL2().glTranslatef(translation.get(),translation.get(),translation.get());
             gl.getGL2().glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
             buffer.position(0);
             if(!relative)
                 gl.getGL2().glPopMatrix();
	        }
	    if(relative)
	        gl.getGL2().glPopMatrix();
	    //gl.getGL2().glUnlockArraysEXT();		
	    gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
	    //gl.getGL2().glDisableClientState(GL.GL_NORMAL_ARRAY);
	    gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
	    rotation.position(0);
	    translation.position(0);
    }
    
    public void multiDraw(FloatBuffer matrix,int limit,boolean relative){  
        final GL gl=GLContext.getCurrentGL();
        //matrices in column-major order!!!!!!
        float[] m=new float[16];
        matrix.position(0);
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);		
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.getGL2().glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.getGL2().glPushMatrix();
             //maybe useless, could I do "gl.glMultMatrixf(matrix)" rather than this?	     
             matrix.get(m,0,16);
             gl.getGL2().glMultMatrixf(m,0);
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
             buffer.position(0);
             if(!relative)
                 gl.getGL2().glPopMatrix();
            }
        if(relative)
            gl.getGL2().glPopMatrix();
        //gl.getGL2().glUnlockArraysEXT();		
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.getGL2().glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
        matrix.position(0);
    }
    
    public void drawByPiece(IntBuffer first,IntBuffer count,int limit){	
        final GL gl=GLContext.getCurrentGL();
        first.position(0);
        count.position(0);
        buffer.position(0);
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);	
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        if(DynamicVertexSetFactory.multiDrawSupported)
            gl.getGL2().glMultiDrawArrays(mode,first,count,limit);
        else
            for(int i=0;i<limit;i++)
                {buffer.position(0);
                 gl.glDrawArrays(mode,first.get(),count.get());
                 buffer.position(0);
                }
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.getGL2().glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
        first.position(0);
        count.position(0);
        buffer.position(0);
    } 
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,IntBuffer first,IntBuffer count,int limit,boolean relative){
        final GL gl=GLContext.getCurrentGL();
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);
        first.position(0);
        count.position(0);
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);		
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        //gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.getGL2().glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.getGL2().glPushMatrix();	     
             gl.getGL2().glTranslatef(translation.get(),translation.get(),translation.get());
             gl.getGL2().glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             buffer.position(0);
             gl.glDrawArrays(mode,first.get(),count.get());	     
             buffer.position(0);
             if(!relative)
                 gl.getGL2().glPopMatrix();
            }
        if(relative)
            gl.getGL2().glPopMatrix();
        //gl.getGL2().glUnlockArraysEXT();		
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.getGL2().glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
        first.position(0);
        count.position(0);
        rotation.position(0);
        translation.position(0);
    }
}
