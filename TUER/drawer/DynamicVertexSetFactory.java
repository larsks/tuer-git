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


class DynamicVertexSetFactory extends AbstractDynamicVertexSetFactory{

    
    private AbstractDynamicVertexSetFactory delegate;
    
    static boolean multiDrawSupported;
    
    
    DynamicVertexSetFactory(GL gl){
        DynamicVertexSetFactory.multiDrawSupported=gl.isFunctionAvailable("glMultiDrawArrays");	
        if((gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")
                || gl.isExtensionAvailable("GL_EXT_vertex_buffer_object"))
                && (gl.isFunctionAvailable("glBindBufferARB")
                 || gl.isFunctionAvailable("glBindBuffer"))
                && (gl.isFunctionAvailable("glBufferDataARB")
                 || gl.isFunctionAvailable("glBufferData"))
                && (gl.isFunctionAvailable("glDeleteBuffersARB")
                 || gl.isFunctionAvailable("glDeleteBuffers"))
                && (gl.isFunctionAvailable("glGenBuffersARB")
                 || gl.isFunctionAvailable("glGenBuffers")))
            delegate=new DynamicVertexBufferObjectFactory(gl);
        else
            if(gl.isExtensionAvailable("GL_EXT_vertex_array")
                    && gl.isFunctionAvailable("glColorPointer")
                    && gl.isFunctionAvailable("glDrawArrays")
                    && gl.isFunctionAvailable("glDrawElements")
                    && gl.isFunctionAvailable("glDrawRangeElements")
                    && gl.isFunctionAvailable("glIndexPointer")
                    && gl.isFunctionAvailable("glNormalPointer")
                    && gl.isFunctionAvailable("glTexCoordPointer")
                    && gl.isFunctionAvailable("glVertexPointer"))
                delegate=new VertexArrayFactory(gl);
            else	        
                delegate=new DynamicDefaultVertexSetFactory(gl);
    }
    
    @Override
    DynamicVertexSet newVertexSet(float[] array,int mode){
        return(delegate.newVertexSet(array,mode));
    }
    
    @Override
    DynamicVertexSet newVertexSet(FloatBuffer floatBuffer,int mode){
        return(delegate.newVertexSet(floatBuffer,mode));
    }
    
    @Override
    DynamicVertexSet newVertexSet(IVertexSet vertexSet,int mode){
        return(delegate.newVertexSet(vertexSet,mode));
    }
}
