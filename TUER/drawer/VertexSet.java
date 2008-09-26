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


abstract class VertexSet implements IVertexSet{
    
    
    //update it to 8 if you use the normals
    static final int primitiveCount=5;
    
    //update it to GL.GL_T2F_N3F_V3F if you use the normals 
    static final int interleavedFormat=GL.GL_T2F_V3F;
    
    protected FloatBuffer buffer;
    
    protected int mode;
    
    
    public abstract void draw();
    
    public final FloatBuffer getBuffer(){
        return(buffer);
    }   
}
