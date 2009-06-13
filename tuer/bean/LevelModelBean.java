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
package bean;

import java.io.Serializable;

public final class LevelModelBean implements ILevelModelBean{
    
    
    private static final long serialVersionUID = 1L;
    /**
     * position when the player appears at the first time
     * he enters the level
     */
    private float[] initialSpawnPosition;
    /**
     * names of the nodes (cells and portals)
     */
    private String[] identifiedNodeNames;
    
    
    public LevelModelBean(){}


    @Override
    public final float[] getInitialSpawnPosition(){
        return(initialSpawnPosition);
    }

    @Override
    public final void setInitialSpawnPosition(float[] initialSpawnPosition){
        this.initialSpawnPosition=initialSpawnPosition;
    }
    
    @Override
    public final Serializable getSerializableBean(){
        return(this);    
    }

    @Override
    public final String[] getIdentifiedNodeNames(){
        return(identifiedNodeNames);
    }

    @Override
    public final void setIdentifiedNodeNames(String[] identifiedNodeNames){
        this.identifiedNodeNames=identifiedNodeNames;
    }
}
