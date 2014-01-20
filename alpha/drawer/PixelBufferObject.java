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


import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;


public class PixelBufferObject{
    
    
    private int[] id;

    private IntBuffer buffer;

    private GL gl;


    public PixelBufferObject(GL gl,int[] array){
        //this.mode=GL.GL_TRIANGLES;
        this.gl=gl;
        this.buffer=Buffers.newDirectIntBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        //create the pixel buffer object
        this.id=new int[1];
        gl.glGenBuffers(1,id,0);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER,id[0]);
        gl.glBufferData(GL2.GL_PIXEL_UNPACK_BUFFER,Buffers.SIZEOF_INT*buffer.capacity(),buffer,GL2.GL_STREAM_DRAW);
        this.buffer.position(0);	
    }
    
    public PixelBufferObject(GL gl,IntBuffer intBuffer){
        //this.mode=GL.GL_TRIANGLES;
        this.gl=gl;
        this.buffer=Buffers.copyIntBuffer(intBuffer);
        this.buffer.position(0);
        /*TODO : create the vertex buffer object*/
        this.id=new int[1];
        gl.glGenBuffers(1,id,0);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER,id[0]);
        gl.glBufferData(GL2.GL_PIXEL_UNPACK_BUFFER,Buffers.SIZEOF_INT*buffer.capacity(),buffer,GL2.GL_STREAM_DRAW);
        this.buffer.position(0);
    }
    
    /*DynamicVertexBufferObject(GL gl,IVertexSet vertexSet){
        this(gl,vertexSet.getBuffer());
    } */   
            
    public void draw(){       
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER,id[0]);
        //gl.getGL2().glInterleavedArrays(GL2.GL_T2F_V3F,0,0);
        buffer.position(0);
        gl.glDrawArrays(GL.GL_POINTS,0,buffer.capacity()/5);
        buffer.position(0);
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    } 
    
    public void draw(int start,int count){
        gl.getGL2().glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.getGL2().glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER,id[0]);
        //gl.getGL2().glInterleavedArrays(GL2.GL_T2F_V3F,0,0);
        buffer.position(0);
        gl.glDrawArrays(GL.GL_POINTS,start,count);
        buffer.position(0);
        gl.getGL2().glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.getGL2().glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }
    
    public void update(IntBuffer intBuffer){
        gl.glBindBuffer(GL2.GL_PIXEL_UNPACK_BUFFER,id[0]);
	    gl.glBufferData(GL2.GL_PIXEL_UNPACK_BUFFER,Buffers.SIZEOF_INT*buffer.capacity(),buffer,GL2.GL_STREAM_DRAW);
    }
}
