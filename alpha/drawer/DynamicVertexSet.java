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

public abstract class DynamicVertexSet extends VertexSet implements IDynamicVertexSet{


    public abstract void draw(int start,int count);

    public final float get(int index){
        return(buffer.get(index));
    }

    public final float[] get(){       
        return(buffer.array());
    }

    public final void put(int index,float value){
        buffer.put(index,value);
        /*call glBufferData for VBO???*/
    }
    
    public final void set(float[] value){
        buffer.position(0);
        buffer.put(value);
        buffer.position(0);
        /*call glBufferData for VBO???*/
    }
    
    /*"value" position incremented during the treatment*/
    public final void set(FloatBuffer value){
        buffer.position(0);
        buffer.put(value);
        buffer.position(0);
        /*call glBufferData for VBO???*/
    }
    
    public final void put(float[] value,int offset,int length){        
        buffer.position(offset);
        for(int i=0;i<length;i++)
            buffer.put(value[i]);
        buffer.position(0);
        /*call glBufferData for VBO???*/
    }
    
    /*"value" position incremented during the treatment*/
    public final void put(FloatBuffer value,int offset,int length){
        buffer.position(offset);
        buffer.put(value);
        buffer.position(0);
        /*call glBufferData for VBO???*/
    }
}
