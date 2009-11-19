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
package connection;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import engine.ILevelRelativeVolumeElement;

final class LevelRelativeVolumeElementConnector implements ILevelRelativeVolumeElement{

    private final jfpsm.ILevelRelativeVolumeElement delegate;

    LevelRelativeVolumeElementConnector(jfpsm.ILevelRelativeVolumeElement delegate){
        this.delegate=delegate;
    }

    public float[] getLevelRelativePosition(){
        return(delegate.getLevelRelativePosition());
    }

    @Override
    public IntBuffer getIndexBuffer(){
        return(delegate.getIndexBuffer());
    }

    @Override
    public FloatBuffer getNormalBuffer(){
        return(delegate.getNormalBuffer());
    }

    @Override
    public FloatBuffer getVertexBuffer(){
        return(delegate.getVertexBuffer());
    }
    
    @Override
    public FloatBuffer getTexCoordBuffer(){
        return(delegate.getTexCoordBuffer());
    }
}
