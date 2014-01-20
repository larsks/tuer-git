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
import javax.media.opengl.GLContext;
import javax.media.opengl.glu.GLU;

import com.jogamp.common.nio.Buffers;


class StaticDefaultVertexSet extends StaticVertexSet{


    StaticDefaultVertexSet(float[] array,int mode){
        this.mode=mode;
        this.buffer=Buffers.newDirectFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);	
    }
    
    StaticDefaultVertexSet(FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.buffer=Buffers.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);
    }
    
    StaticDefaultVertexSet(IVertexSet vertexSet,int mode){
        this(vertexSet.getBuffer(),mode);
    }    
            
    public void draw(){
        final GL gl=GLContext.getCurrentGL();
        buffer.position(0);
	    gl.getGL2().glBegin(mode);
	    for(int i=0;i<buffer.capacity();i+=VertexSet.primitiveCount)
	        {//update it if you use the normals
	         gl.getGL2().glTexCoord2f(buffer.get(),buffer.get());
	         //gl.getGL2().glNormal3f(buffer.get(),buffer.get(),buffer.get());
	         gl.getGL2().glVertex3f(buffer.get(),buffer.get(),buffer.get());
	        }
	    gl.getGL2().glEnd();
	    buffer.position(0);
    }
}
