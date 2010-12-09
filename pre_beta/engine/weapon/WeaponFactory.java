package engine.weapon;

import java.util.LinkedHashMap;

public final class WeaponFactory {

	
	private final LinkedHashMap<String,Weapon> weaponsMap;
	
	
	public WeaponFactory(){
		weaponsMap=new LinkedHashMap<String,Weapon>();
	}	
	
	public final boolean addNewWeapon(final String identifier,final boolean twoHanded,final int magazineSize,final Ammunition ammunition,final int ammunitionPerShot){
		final boolean success=identifier!=null&&!weaponsMap.containsKey(identifier);
		if(success)
			weaponsMap.put(identifier,null/*new Weapon(identifier,twoHanded,magazineSize,ammunition,ammunitionPerShot)*/);
		return(success);
	}
	
	public final Weapon getWeapon(final int index){
		return(weaponsMap.get(index));
	}
	
	public final Weapon getWeapon(final String identifier){
		return(weaponsMap.get(identifier));
	}
	
	public final int getWeaponCount(){
		return(weaponsMap.size());
	}
}
