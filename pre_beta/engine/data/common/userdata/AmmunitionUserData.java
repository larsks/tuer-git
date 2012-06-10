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

import engine.weaponry.Ammunition;

public final class AmmunitionUserData extends CollectibleUserData<Ammunition>{
	
	private final int ammunitionCount;
	
	public AmmunitionUserData(final Ammunition ammunition,final int ammunitionCount){
		super(ammunition,ammunition.getLabel());
		this.ammunitionCount=ammunitionCount;
	}
	
	@Override
	public String getPickingUpSoundSampleSourcename(){
		return(collectible.getPickingUpSoundSampleSourcename());
	}
	
	public final Ammunition getAmmunition(){
		return(collectible);
	}
	
	public final int getAmmunitionCount(){
		return(ammunitionCount);
	}
}