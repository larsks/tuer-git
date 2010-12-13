package engine.weapon;

import java.util.HashMap;

public final class AmmunitionFactory {

	
private final HashMap<String,Ammunition> ammunitionsMap;
	
	private final HashMap<Integer,Ammunition> ammunitionsIndicesMap;
	
	
	public AmmunitionFactory(){
		ammunitionsMap=new HashMap<String,Ammunition>();
		ammunitionsIndicesMap=new HashMap<Integer,Ammunition>();
	}	
	
	public final boolean addNewAmmunition(final String identifier){
		final boolean success=identifier!=null&&!ammunitionsMap.containsKey(identifier);
		if(success)
			{final Ammunition ammunition=new Ammunition(identifier);
			 ammunitionsMap.put(identifier,ammunition);
			 ammunitionsIndicesMap.put(ammunition.getUid(),ammunition);
			}
		return(success);
	}
	
	public final Ammunition getAmmunition(final int index){
		return(ammunitionsIndicesMap.get(Integer.valueOf(index)));
	}
	
	public final Ammunition getAmmunition(final String identifier){
		return(ammunitionsMap.get(identifier));
	}
	
	public final int getSize(){
		return(ammunitionsMap.size());
	}
}
