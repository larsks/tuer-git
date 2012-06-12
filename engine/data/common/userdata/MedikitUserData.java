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

import engine.data.common.Medikit;

public final class MedikitUserData extends CollectibleUserData<Medikit>{
	
	public MedikitUserData(final Medikit medikit){
		super(medikit,"points of health");
	}
	
	public int getHealth(){
		return(collectible.getHealth());
	}
	
	@Override
	public String getPickingUpSoundSampleIdentifier(){
		return(collectible.getPickingUpSoundSampleIdentifier());
	}
}