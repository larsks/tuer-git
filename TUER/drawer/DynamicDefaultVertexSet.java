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


class DynamicDefaultVertexSet extends DynamicVertexSet{
    
    
    DynamicDefaultVertexSet(GL gl,float[] array,int mode){
        this.mode=mode;
        this.gl=gl;
        this.buffer=BufferUtil.newFloatBuffer(array.length);
        this.buffer.put(array);	
        this.buffer.position(0);	
    }
    
    DynamicDefaultVertexSet(GL gl,FloatBuffer floatBuffer,int mode){
        this.mode=mode;
        this.gl=gl;
        this.buffer=BufferUtil.copyFloatBuffer(floatBuffer);
        this.buffer.position(0);
    }

    DynamicDefaultVertexSet(GL gl,IVertexSet vertexSet,int mode){
        this(gl,vertexSet.getBuffer(),mode);
    }    
            
    public void draw(){
        buffer.position(0);
        gl.glBegin(mode);
        for(int i=0;i<buffer.capacity();i+=VertexSet.primitiveCount)
            {gl.glTexCoord2f(buffer.get(),buffer.get());
             //gl.glNormal3f(buffer.get(),buffer.get(),buffer.get());
             gl.glVertex3f(buffer.get(),buffer.get(),buffer.get());
            }
        gl.glEnd();
        buffer.position(0);
    } 
    
    public void draw(int start,int count){
        buffer.position(VertexSet.primitiveCount*start);
	    gl.glBegin(mode);
	    for(int i=0;i<VertexSet.primitiveCount*count && i<buffer.capacity();i+=VertexSet.primitiveCount)
	        {gl.glTexCoord2f(buffer.get(),buffer.get());
	         //gl.glNormal3f(buffer.get(),buffer.get(),buffer.get());
	         gl.glVertex3f(buffer.get(),buffer.get(),buffer.get());
	        }
	    gl.glEnd();
	    buffer.position(0);
    }
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,int limit,boolean relative){
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);	
        if(relative)
            gl.glPushMatrix();
	    for(int i=0;i<limit;i++)
            {if(!relative)
	         gl.glPushMatrix();	     
             gl.glTranslatef(translation.get(),translation.get(),translation.get());
             gl.glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             this.draw();
             if(!relative)
                 gl.glPopMatrix();
	        }
	    if(relative)
	        gl.glPopMatrix();	
	    rotation.position(0);
	    translation.position(0);
    }
    
    public void multiDraw(FloatBuffer matrix,int limit,boolean relative){       
        //matrices in column-major order!!!!!!
        float[] m=new float[16];
        matrix.position(0);	
        if(relative)
            gl.glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.glPushMatrix();
             //maybe useless, could I do "gl.glMultMatrixf(matrix)" rather than this?	     
             matrix.get(m,0,16);
             gl.glMultMatrixf(m,0);
             this.draw();
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();	
        matrix.position(0);
    }
        
    public void drawByPiece(IntBuffer first,IntBuffer count,int limit){	
        first.position(0);
        count.position(0);
        buffer.position(0);
        gl.glBegin(mode);			     
        for(int i=0;i<limit;i++)
            {buffer.position(VertexSet.primitiveCount*first.get());
             for(i=0;i<VertexSet.primitiveCount*count.get() && i<buffer.capacity();i+=VertexSet.primitiveCount)
                 {gl.glTexCoord2f(buffer.get(),buffer.get());
                  //gl.glNormal3f(buffer.get(),buffer.get(),buffer.get());
                  gl.glVertex3f(buffer.get(),buffer.get(),buffer.get());
                 }
             buffer.position(0);
            }
        gl.glEnd();
        first.position(0);
        count.position(0);
        buffer.position(0);
    } 
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,IntBuffer first,IntBuffer count,int limit,boolean relative){        	
        //limit*3 <= translation.capacity()
        translation.position(0);
        rotation.position(0);	
        first.position(0);
        count.position(0);
        if(relative)
            gl.glPushMatrix();
        for(int i=0;i<limit;i++)
            {if(!relative)
                 gl.glPushMatrix();	     
             gl.glTranslatef(translation.get(),translation.get(),translation.get());
             gl.glRotatef(rotation.get(),rotation.get(),rotation.get(),rotation.get());
             this.draw(first.get(),count.get());
             if(!relative)
                 gl.glPopMatrix();
            }
        if(relative)
            gl.glPopMatrix();
        first.position(0);
        count.position(0);	
        rotation.position(0);
        translation.position(0);
    }      
}
