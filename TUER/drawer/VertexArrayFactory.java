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


class VertexArrayFactory extends AbstractDynamicVertexSetFactory{
          
    
    VertexArrayFactory(){}
        
    
    @Override
    DynamicVertexSet newVertexSet(float[] array,int mode){
        return(new VertexArray(array,mode));
    }
    
    @Override
    DynamicVertexSet newVertexSet(FloatBuffer floatBuffer,int mode){
        return(new VertexArray(floatBuffer,mode));
    }
    
    @Override
    DynamicVertexSet newVertexSet(IVertexSet vertexSet,int mode){
        return(new VertexArray(vertexSet,mode));
    }
}
