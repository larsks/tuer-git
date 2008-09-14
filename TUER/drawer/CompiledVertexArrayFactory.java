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


class CompiledVertexArrayFactory{
    
    
    private GL gl;
    
    
    CompiledVertexArrayFactory(GL gl)throws RuntimeException{
        if(gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")
	    && gl.isFunctionAvailable("glLockArraysEXT")
	    && gl.isFunctionAvailable("glUnlockArraysEXT"))
	        this.gl=gl;
	    else
	        throw new RuntimeException("compiled vertex array not supported");
    }           
    
    CompiledVertexArray newVertexSet(float[] array,int mode){
        return(new CompiledVertexArray(gl,array,mode));
    }
    
    CompiledVertexArray newVertexSet(FloatBuffer floatBuffer,int mode){
        return(new CompiledVertexArray(gl,floatBuffer,mode));
    }
    
    CompiledVertexArray newVertexSet(IVertexSet vertexSet,int mode){
        return(new CompiledVertexArray(gl,vertexSet,mode));
    }
}

