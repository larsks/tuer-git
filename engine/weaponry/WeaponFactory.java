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
package engine.weaponry;

import java.util.HashMap;

public final class WeaponFactory {

	
	private final HashMap<String,Weapon> weaponsMap;
	
	private final HashMap<Integer,Weapon> weaponsIndicesMap;
	
	
	public WeaponFactory(){
		weaponsMap=new HashMap<String,Weapon>();
		weaponsIndicesMap=new HashMap<Integer,Weapon>();
	}	
	
	public final boolean addNewWeapon(final String identifier,final boolean twoHanded,final int magazineSize,
	        final Ammunition ammunition,final int ammunitionPerShot,
	        final int attackDurationInMillis,final boolean fullyAutomatic){
		final boolean success=identifier!=null&&!weaponsMap.containsKey(identifier);
		if(success)
			{final Weapon weapon=new Weapon(identifier,twoHanded,magazineSize,ammunition,ammunitionPerShot,attackDurationInMillis,fullyAutomatic);
			 weaponsMap.put(identifier,weapon);
			 weaponsIndicesMap.put(weapon.getUid(),weapon);
			}
		return(success);
	}
	
	public final Weapon getWeapon(final int index){
		return(0<=index&&index<getWeaponCount()?weaponsIndicesMap.get(Integer.valueOf(index)):null);
	}
	
	public final Weapon getWeapon(final String identifier){
		return(weaponsMap.get(identifier));
	}
	
	public final int getWeaponCount(){
		return(weaponsMap.size());
	}
}
