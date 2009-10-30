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
package engine;

import java.io.IOException;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.shape.Box;
//import com.ardor3d.util.export.InputCapsule;
//import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;

final class Cuboid extends Box{

    
    //private boolean normalsHeadedToOutside;
    
    public Cuboid(final String name, final ReadOnlyVector3 center, final double xExtent, final double yExtent,final double zExtent,final boolean normalsHeadedToOutside){
        super(name,center,xExtent,yExtent,zExtent);
        //this.normalsHeadedToOutside=normalsHeadedToOutside;
        if(!normalsHeadedToOutside)
            {//TODO: modify the index buffer and the normal buffer
            }
    }
    
    @Override
    public void write(final Ardor3DExporter e)throws IOException{
        super.write(e);
        /*final OutputCapsule capsule=e.getCapsule(this);
        capsule.write(normalsHeadedToOutside,"normalsHeadedToOutside",true);*/
    }
    
    @Override
    public void read(final Ardor3DImporter e)throws IOException{
        super.read(e);
        /*final InputCapsule capsule=e.getCapsule(this);
        normalsHeadedToOutside=capsule.readBoolean("normalsHeadedToOutside",true);*/
    }
}
