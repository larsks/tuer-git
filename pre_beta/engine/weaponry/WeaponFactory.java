package engine.weaponry;

import java.util.HashMap;

public final class WeaponFactory {

	
	private final HashMap<String,Weapon> weaponsMap;
	
	private final HashMap<Integer,Weapon> weaponsIndicesMap;
	
	
	public WeaponFactory(){
		weaponsMap=new HashMap<String,Weapon>();
		weaponsIndicesMap=new HashMap<Integer,Weapon>();
	}	
	
	public final boolean addNewWeapon(final String identifier,final boolean twoHanded,final int magazineSize,final Ammunition ammunition,final int ammunitionPerShot,final int attackDurationInMillis){
		final boolean success=identifier!=null&&!weaponsMap.containsKey(identifier);
		if(success)
			{final Weapon weapon=new Weapon(identifier,twoHanded,magazineSize,ammunition,ammunitionPerShot,attackDurationInMillis);
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
