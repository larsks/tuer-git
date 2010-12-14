package engine.data;

import engine.statemachine.GameState;
import engine.weaponry.Ammunition;

public final class AmmunitionUserData extends CollectibleUserData{
	
	
	private final Ammunition ammunition;
	
	private final int ammunitionCount;
	
	public AmmunitionUserData(final Ammunition ammunition,final int ammunitionCount){
		super(GameState.pickupAmmoSourcename);
		this.ammunition=ammunition;
		this.ammunitionCount=ammunitionCount;
	}
	
	public final Ammunition getAmmunition(){
		return(ammunition);
	}
	
	public final int getAmmunitionCount(){
		return(ammunitionCount);
	}
}