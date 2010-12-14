package engine.data;

public final class MedikitUserData extends CollectibleUserData{
	
	private final int health;
	
	public MedikitUserData(final int health){
		//TODO: add a source name
		super(null);
		this.health=health;
	}
	
	public final int getHealth(){
		return(health);
	}
}