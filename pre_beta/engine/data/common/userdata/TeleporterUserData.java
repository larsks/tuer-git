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
package engine.data.common.userdata;

import com.ardor3d.math.Vector3;
import engine.data.common.Teleporter;

public final class TeleporterUserData extends CollectibleUserData<Teleporter>{
	
	private final Vector3 destination;
	
	public TeleporterUserData(final Teleporter teleporter,final Vector3 destination){
		super(teleporter,null);
		this.destination=destination;
	}
	
	public final Vector3 getDestination(){
		return(destination);
	}
	
	@Override
	public String getPickingUpSoundSampleIdentifier(){
		return(collectible.getPickingUpSoundSampleIdentifier());
	}
}