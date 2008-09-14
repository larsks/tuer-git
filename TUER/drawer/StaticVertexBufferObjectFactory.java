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


class StaticVertexBufferObjectFactory extends AbstractStaticVertexSetFactory{
    
    
    private GL gl;
    
    
    StaticVertexBufferObjectFactory(GL gl){
        this.gl=gl;
    }
            
    
    @Override
    StaticVertexSet newVertexSet(float[] array,int mode){
        return(new StaticVertexBufferObject(gl,array,mode));
    }
    
    @Override
    StaticVertexSet newVertexSet(FloatBuffer floatBuffer,int mode){       
        return(new StaticVertexBufferObject(gl,floatBuffer,mode));
    }
    
    @Override
    StaticVertexSet newVertexSet(IVertexSet vertexSet,int mode){
        return(new StaticVertexBufferObject(gl,vertexSet,mode));
    }
}
