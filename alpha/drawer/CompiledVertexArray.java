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
import java.util.ArrayList;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;

class CompiledVertexArray extends VertexSet{                
    
    
    CompiledVertexArray(float[] array,int mode){
        this.mode=mode;
        this.buffer=Buffers.newDirectFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);		
    }
    
    CompiledVertexArray(FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.buffer=Buffers.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);    
    }
    
    CompiledVertexArray(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }
            
    
    public void draw(){ 
        final GL gl=GLContext.getCurrentGL();
        /*deprecated as it is a very slow method*/            
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);	
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        gl.getGL2().glLockArraysEXT(0,buffer.capacity());
        buffer.position(0);
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.position(0);
        gl.getGL2().glUnlockArraysEXT();		
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }
    
    void multiDraw(ArrayList<float[]> translation,boolean relative){
        final GL gl=GLContext.getCurrentGL();
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);		
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,buffer);
        gl.getGL2().glLockArraysEXT(0,buffer.capacity());
        if(relative)
            gl.getGL2().glPushMatrix();
        for(float[] tmp:translation)
            {if(!relative)
                 gl.getGL2().glPushMatrix();
             if(tmp!=null)
                 gl.getGL2().glVertex3f(tmp[0],tmp[1],tmp[2]);
             buffer.position(0);
             gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
             buffer.position(0);
             if(!relative)
                 gl.getGL2().glPopMatrix();
            }
        if(relative)
            gl.getGL2().glPopMatrix();
        gl.getGL2().glUnlockArraysEXT();		
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.getGL2().glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }    
}
