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
package engine.data;

import com.ardor3d.math.type.ReadOnlyMatrix3;
import engine.weaponry.AmmunitionContainer;
import engine.weaponry.Weapon;

public final class WeaponUserData extends CollectibleUserData{
	
	
	private final Weapon weapon;
	
	private final ReadOnlyMatrix3 rotation;
	
	private AmmunitionContainer ammunitionInMagazine;
	
	private int ownerUid;
	/**flag indicating whether a weapon can change of owner*/
	private boolean digitalWatermarkEnabled;
	/**flag indicating whether a weapon is primary*/
    private final boolean primary;
	
	
	public WeaponUserData(final String sourcename,final Weapon weapon,final ReadOnlyMatrix3 rotation,final int ownerUid,final boolean digitalWatermarkEnabled,final boolean primary){
		super(sourcename,null);
		this.weapon=weapon;
		this.rotation=rotation;
		this.ownerUid=ownerUid;
		this.digitalWatermarkEnabled=digitalWatermarkEnabled;
		this.primary=primary;
		this.ammunitionInMagazine=new AmmunitionContainer(weapon.isForMelee()?0:weapon.getMagazineSize());
	}
	
	
	public final Weapon getWeapon(){
		return(weapon);
	}
	
	public final ReadOnlyMatrix3 getRotation(){
		return(rotation);
	}
	
	public final int getOwnerUid(){
		return(ownerUid);
	}
	
	public final void setOwnerUid(final int ownerUid){
		if(!digitalWatermarkEnabled)
		    this.ownerUid=ownerUid;
	}
	
	public final boolean isDigitalWatermarkEnabled(){
		return(digitalWatermarkEnabled);
	}
	
	public final void setDigitalWatermarkEnabled(final boolean digitalWatermarkEnabled){
		this.digitalWatermarkEnabled=digitalWatermarkEnabled;
	}
	
	/**
     * tells whether a weapon is primary
     * @return <code>true</code> if a weapon is primary, otherwise <code>false</code>
     */
    public final boolean isPrimary(){
        return(primary);
    }
	
	public final int getAmmunitionCountInMagazine(){
		return(ammunitionInMagazine.getAmmunitionCount());
	}
	
	public final int addAmmunitionIntoMagazine(final int ammunitionCountToAddIntoMagazine){
		return(ammunitionInMagazine.add(ammunitionCountToAddIntoMagazine));
	}
	
	public final int removeAmmunitionFromMagazine(final int ammunitionCountToRemoveFromMagazine){
		return(ammunitionInMagazine.remove(ammunitionCountToRemoveFromMagazine));
	}
}