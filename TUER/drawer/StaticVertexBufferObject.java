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
import javax.media.opengl.GL;


class StaticVertexBufferObject extends StaticVertexSet{
    
    
    private int[] id;
    
    
    StaticVertexBufferObject(GL gl,float[] array,int mode){
        this.mode=mode;
        this.gl=gl;
        this.buffer=BufferUtil.newFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER,id[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER,BufferUtil.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STATIC_DRAW_ARB);
        this.buffer.position(0);       
    }
    
    StaticVertexBufferObject(GL gl,FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.gl=gl;
        this.buffer=BufferUtil.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER,id[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER,BufferUtil.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STATIC_DRAW_ARB);
        this.buffer.position(0);
    }
    
    StaticVertexBufferObject(GL gl,IVertexSet vertexSet,int mode){
        this(gl,vertexSet.getBuffer(),mode);
    }    
            
    public void draw(){       		
        //draw the vertex buffer object
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER,id[0]);
        gl.glInterleavedArrays(VertexSet.interleavedFormat,0,0);                       
        buffer.rewind();      
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.rewind();       
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        //gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }
}
