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
import java.nio.IntBuffer;
import javax.media.opengl.GL;


public class PixelBufferObject{
    
    
    private int[] id;

    private IntBuffer buffer;

    private GL gl;


    public PixelBufferObject(GL gl,int[] array){
        //this.mode=GL.GL_TRIANGLES;
        this.gl=gl;
        this.buffer=BufferUtil.newIntBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        //create the pixel buffer object
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,id[0]);
        gl.glBufferDataARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,BufferUtil.SIZEOF_INT*buffer.capacity(),buffer,GL.GL_STREAM_DRAW_ARB);
        this.buffer.position(0);	
    }
    
    public PixelBufferObject(GL gl,IntBuffer intBuffer){
        //this.mode=GL.GL_TRIANGLES;
        this.gl=gl;
        this.buffer=BufferUtil.copyIntBuffer(intBuffer);
        this.buffer.position(0);
        /*TODO : create the vertex buffer object*/
        this.id=new int[1];
        gl.glGenBuffersARB(1,id,0);
        gl.glBindBufferARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,id[0]);
        gl.glBufferDataARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,BufferUtil.SIZEOF_INT*buffer.capacity(),buffer,GL.GL_STREAM_DRAW_ARB);
        this.buffer.position(0);
    }
    
    /*DynamicVertexBufferObject(GL gl,IVertexSet vertexSet){
        this(gl,vertexSet.getBuffer());
    } */   
            
    public void draw(){       
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,id[0]);
        //gl.glInterleavedArrays(GL.GL_T2F_V3F,0,0);
        buffer.position(0);
        gl.glDrawArrays(GL.GL_POINTS,0,buffer.capacity()/5);
        buffer.position(0);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    } 
    
    public void draw(int start,int count){
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBufferARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,id[0]);
        //gl.glInterleavedArrays(GL.GL_T2F_V3F,0,0);
        buffer.position(0);
        gl.glDrawArrays(GL.GL_POINTS,start,count);
        buffer.position(0);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
    }
    
    public void update(IntBuffer intBuffer){
        gl.glBindBufferARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,id[0]);
	    gl.glBufferDataARB(GL.GL_PIXEL_UNPACK_BUFFER_ARB,BufferUtil.SIZEOF_INT*buffer.capacity(),buffer,GL.GL_STREAM_DRAW_ARB);
    }
}
