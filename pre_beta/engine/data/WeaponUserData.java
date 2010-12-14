package engine.data;

import com.ardor3d.math.type.ReadOnlyMatrix3;
import engine.statemachine.GameState;
import engine.weaponry.AmmunitionContainer;
import engine.weaponry.Weapon;

public final class WeaponUserData extends CollectibleUserData{
	
	
	private final Weapon weapon;
	
	private final ReadOnlyMatrix3 rotation;
	
	private AmmunitionContainer ammunitionInMagazine;
	
	private int ownerUid;
	/**flag indicating whether a weapon can change of owner*/
	private boolean digitalWatermarkEnabled;
	
	
	public WeaponUserData(final Weapon weapon,final ReadOnlyMatrix3 rotation,final int ownerUid,final boolean digitalWatermarkEnabled){
		super(GameState.pickupWeaponSourcename);
		this.weapon=weapon;
		this.rotation=rotation;
		this.ownerUid=ownerUid;
		this.digitalWatermarkEnabled=digitalWatermarkEnabled;
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