/**
 * Copyright (c) 2006-2014 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.data.common.userdata;

import com.ardor3d.math.type.ReadOnlyMatrix3;
import engine.weaponry.AmmunitionContainer;
import engine.weaponry.Weapon;

public final class WeaponUserData extends CollectibleUserData<Weapon>{
	
	private final ReadOnlyMatrix3 rotation;
	
	private AmmunitionContainer ammunitionInMagazine;
	
	private int ownerUid;
	/**flag indicating whether a weapon can change of owner*/
	private boolean digitalWatermarkEnabled;
	/**flag indicating whether a weapon is primary*/
    private final boolean primary;
	
	
	public WeaponUserData(final Weapon weapon,final ReadOnlyMatrix3 rotation,final int ownerUid,final boolean digitalWatermarkEnabled,final boolean primary){
		super(weapon,null);
		if(!primary&&!weapon.isTwoHanded())
		    throw new IllegalArgumentException("The weapon " + weapon + " cannot be used as a secondary weapon");
		this.rotation=rotation;
		this.ownerUid=ownerUid;
		this.digitalWatermarkEnabled=digitalWatermarkEnabled;
		this.primary=primary;
		this.ammunitionInMagazine=new AmmunitionContainer(weapon.isForMelee()?0:weapon.getMagazineSize());
	}
	
	
	public final Weapon getWeapon(){
		return(collectible);
	}
	
	@Override
	public String getPickingUpSoundSampleIdentifier(){
		return(collectible.getPickingUpSoundSampleIdentifier());
	}
	
	public final String getBlowOrShotSourcename(){
		return(collectible.getBlowOrShotSoundSampleIdentifier());
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