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
package main;

import java.nio.FloatBuffer;
import javax.media.opengl.GL;

public interface IVertexSetProvider{
    
        
    public void bindVertexSetProvider(IVertexSetProvider provider);
    
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,float[] array,int mode);
    
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,FloatBuffer floatBuffer,int mode);
    
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,IVertexSet vertexSet,int mode);
    
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,float[] array,int mode);
    
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,FloatBuffer floatBuffer,int mode);
    
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,IVertexSet vertexSet,int mode);
}
