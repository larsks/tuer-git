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
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import com.jogamp.common.nio.Buffers;

class StaticVertexBufferObject extends StaticVertexSet{
    
    
    private int[] id;
    
    
    StaticVertexBufferObject(float[] array,int mode){
        final GL gl=GLContext.getCurrentGL();
        this.mode=mode;
        this.buffer=Buffers.newDirectFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffers(1,id,0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,id[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER,Buffers.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STATIC_DRAW);
        this.buffer.position(0);       
    }
    
    StaticVertexBufferObject(FloatBuffer floatBuffer,int mode){
        final GL gl=GLContext.getCurrentGL();
        this.mode=mode;
        this.buffer=Buffers.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);
        this.id=new int[1];
        gl.glGenBuffers(1,id,0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,id[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER,Buffers.SIZEOF_FLOAT*buffer.capacity(),buffer,GL.GL_STATIC_DRAW);
        this.buffer.position(0);
    }
    
    StaticVertexBufferObject(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }    
            
    public void draw(){    
        final GL gl=GLContext.getCurrentGL();
        //draw the vertex buffer object
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.getGL2().glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.getGL2().glBindBuffer(GL.GL_ARRAY_BUFFER,id[0]);
        gl.getGL2().glInterleavedArrays(VertexSet.interleavedFormat,0,0);                       
        buffer.rewind();      
        gl.glDrawArrays(mode,0,buffer.capacity()/VertexSet.primitiveCount);
        buffer.rewind();       
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        //gl.getGL2().glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }
}
