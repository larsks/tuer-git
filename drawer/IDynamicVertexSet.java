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
import java.nio.IntBuffer;

public interface IDynamicVertexSet extends IVertexSet{


    public void draw(int start,int count);
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,int limit,boolean relative);
    
    public void multiDraw(FloatBuffer matrix,int limit,boolean relative);       
    
    public void drawByPiece(IntBuffer first,IntBuffer count,int limit);
    
    public void multiDraw(FloatBuffer translation,FloatBuffer rotation,IntBuffer first,IntBuffer count,int limit,boolean relative);
    
    public float get(int index);
    
    public float[] get();
    
    public void put(int index,float value);
    
    public void set(float[] value);
    
    /*"value" position incremented during the treatment*/
    public void set(FloatBuffer value);
    
    public void put(float[] value,int offset,int length);
    
    /*"value" position incremented during the treatment*/
    public void put(FloatBuffer value,int offset,int length);
}
