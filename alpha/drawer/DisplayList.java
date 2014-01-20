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


/*TODO: use rather GL.GL_T2F_N3F_V3F when precalculating normals*/

class DisplayList extends StaticVertexSet{


    private int id;
    
    
    DisplayList(float[] array,int mode)throws RuntimeException{
        this.mode=mode;
        this.id=0;
        this.buffer=Buffers.newDirectFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);
        this.createDisplayList();
    }
    
    DisplayList(FloatBuffer floatBuffer,int mode)throws RuntimeException{
        this.mode=mode;
        this.id=0;
        this.buffer=Buffers.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);    
        this.createDisplayList();
    }
    
    DisplayList(IVertexSet vertexSet,int mode)throws RuntimeException{
        this(vertexSet.getBuffer(),mode);
    }
    
    
    private void createDisplayList()throws RuntimeException{
        final GL gl=GLContext.getCurrentGL();
        if((id=gl.getGL2().glGenLists(1))==0)
	        throw new RuntimeException("unable to create a display list");
        gl.getGL2().glNewList(id,GL2.GL_COMPILE);
        /*WARNING: format T2F_V3F*/
        gl.getGL2().glBegin(mode);
        for(int i=0;i<buffer.capacity();i+=VertexSet.primitiveCount)
            {//update it if you use the normals
             gl.getGL2().glTexCoord2f(buffer.get(),buffer.get());
             //gl.glNormal3f(buffer.get(),buffer.get(),buffer.get());
             gl.getGL2().glVertex3f(buffer.get(),buffer.get(),buffer.get());
            }
        gl.getGL2().glEnd();
        buffer.position(0);
        gl.getGL2().glEndList();
        buffer.position(0);
    }
    
    public void setMode(int mode){
        if(this.mode!=mode)
            {this.mode=mode;
             final GL gl=GLContext.getCurrentGL();
             if(id!=0)
                 gl.getGL2().glDeleteLists(id,1);
             createDisplayList();
            }
    }
    
    public void draw(){
        final GL gl=GLContext.getCurrentGL();
	    gl.getGL2().glCallList(id);
    } 
    
    protected void finalize(){
        final GL gl=GLContext.getCurrentGL();
        if(id>0)
	        gl.getGL2().glDeleteLists(id,1);
    }   
}
