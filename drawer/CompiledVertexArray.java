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
import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;


class CompiledVertexArray extends VertexSet{                
    
    
    CompiledVertexArray(float[] array,int mode){
        this.mode=mode;
        this.buffer=BufferUtil.newFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);		
    }
    
    CompiledVertexArray(FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.buffer=BufferUtil.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);    
    }
    
    CompiledVertexArray(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }
            
    
    public void draw(){ 
        final GL gl=GLU.getCurrentGL();
        /*deprecated as it is a very slow method*/            
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);	
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        gl.glLockArraysEXT(0,buffer.capacity());
        buffer.position(0);
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.position(0);
        gl.glUnlockArraysEXT();		
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }
    
    void multiDraw(ArrayList<float[]> translation,boolean relative){
        final GL gl=GLU.getCurrentGL();
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);		
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        gl.glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.glPushMatrix();
        for(float[] tmp:translation)
            {if(!relative)
                 gl.glPushMatrix();
             if(tmp!=null)
                 gl.glVertex3f(tmp[0],tmp[1],tmp[2]);
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
             buffer.position(0);
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();
        gl.glUnlockArraysEXT();		
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }    
}
