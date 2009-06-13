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

import java.io.Serializable;

final class LevelModelBeanConnector implements tools.ILevelModelBean{

    
    private bean.ILevelModelBean delegate;
    
    
    LevelModelBeanConnector(bean.ILevelModelBean delegate){
        this.delegate=delegate;
    }
    
    
    @Override
    public final float[] getInitialSpawnPosition(){
        return(delegate.getInitialSpawnPosition());
    }

    @Override
    public final void setInitialSpawnPosition(float[] initialSpawnPosition){
        delegate.setInitialSpawnPosition(initialSpawnPosition);
    }

    @Override
    public final Serializable getSerializableBean(){
        return(delegate.getSerializableBean());
    }

    @Override
    public String[] getIdentifiedNodeNames(){
        return(delegate.getIdentifiedNodeNames());
    }

    @Override
    public final void setIdentifiedNodeNames(String[] identifiedNodeNames){
        delegate.setIdentifiedNodeNames(identifiedNodeNames);
    }
}
