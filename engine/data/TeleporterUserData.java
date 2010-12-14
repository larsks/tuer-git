package engine.data;

import com.ardor3d.math.Vector3;

public final class TeleporterUserData{
	
	private final Vector3 destination;
	
	public TeleporterUserData(final Vector3 destination){
		this.destination=destination;
	}
	
	public final Vector3 getDestination(){
		return(destination);
	}
}