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
package engine.weaponry;

import engine.data.common.Collectible;

public final class Weapon extends Collectible implements Comparable<Weapon>{
	
    /**path of the sound sample played during a shot or a blow*/
    private final String blowOrShotSoundSamplePath;
    /**source name of the sound sample played during a shot or a blow*/
    private String blowOrShotSoundSampleIdentifier;
    /**path of the sound sample played during a reload*/
    private final String reloadSoundSamplePath;
    /**source name of the sound sample played during a reload*/
    private String reloadSoundSampleIdentifier;
    /**unique name*/
    private final String identifier;
    /**flag indicating whether a weapon can be used in both hands*/
    private final boolean twoHanded;
	/**size of the magazine, -1 for melee weapons*/
	private final int magazineSize;
	/**ammunition (might be null especially for melee weapons)*/
	private final Ammunition ammunition;
	/**ammo per shot, -1 for melee weapons*/
	private final int ammunitionPerShot;
	/**duration of a blow or a shot in milliseconds*/
	private final int blowOrShotDurationInMillis;
	/**
	 * flag indicating whether this weapon is fully automatic, which continues to 
	 * load and fire ammunition until the trigger (or other activating device) is 
	 * released, the ammunition is exhausted, or the firearm is jammed
	 * */
	private final boolean fullyAutomatic;
	//TODO store the duration necessary to reload
	//TODO: URL to the binary file
	//TODO: template node for cloning without I/O interruption, lazily instantiated
	    
	public Weapon(final String pickingUpSoundSamplePath,final String blowOrShotSoundSamplePath,final String reloadSoundSamplePath,final String identifier,final boolean twoHanded,final int magazineSize,
	        final Ammunition ammunition,final int ammunitionPerShot,
	        final int blowOrShotDurationInMillis,final boolean fullyAutomatic){
		super(pickingUpSoundSamplePath);
		this.blowOrShotSoundSamplePath=blowOrShotSoundSamplePath;
		this.reloadSoundSamplePath=reloadSoundSamplePath;
		this.identifier=identifier;
		this.twoHanded=twoHanded;
		this.magazineSize=magazineSize;
		this.ammunition=ammunition;
		this.ammunitionPerShot=ammunitionPerShot;
		this.blowOrShotDurationInMillis=blowOrShotDurationInMillis;
		this.fullyAutomatic=fullyAutomatic;
	}
	
	/**
     * tells whether a weapon can be used in both hands
     * @return <code>true</code> if a weapon can be used in both hands, otherwise <code>false</code>
     */
    public final boolean isTwoHanded(){
        return(twoHanded);
    }
    
    public final boolean isFullyAutomatic(){
    	return(fullyAutomatic);
    }
    
    public final String getBlowOrShotSoundSamplePath(){
    	return(blowOrShotSoundSamplePath);
    }
	    
	public String getBlowOrShotSoundSampleIdentifier(){
		return(blowOrShotSoundSampleIdentifier);
	}

	public void setBlowOrShotSoundSampleIdentifier(String blowOrShotSoundSampleIdentifier){
		this.blowOrShotSoundSampleIdentifier=blowOrShotSoundSampleIdentifier;
	}
	
	public final String getReloadSoundSamplePath(){
    	return(reloadSoundSamplePath);
    }
	    
	public String getReloadSoundSampleIdentifier(){
		return(reloadSoundSampleIdentifier);
	}

	public void setReloadSoundSampleIdentifier(String reloadSoundSampleIdentifier){
		this.reloadSoundSampleIdentifier=reloadSoundSampleIdentifier;
	}

	/**
	 * gets the size of the magazine
	 * @return size of the magazine
	 */
	public final int getMagazineSize(){
	    return(magazineSize);
	}
	
	/**
	 * gets the ammunition used by this weapon
	 * @return ammunition used by this weapon if it is not a contact weapon, otherwise null (typically for knives, swords, etc..)
	 */
	public final Ammunition getAmmunition(){
		return(ammunition);
	}
	
	public boolean isForMelee(){
		return(ammunition==null);
	}
	
	public final int getAmmunitionPerShot(){
		return(ammunitionPerShot);
	}
	
	@Override
	public int hashCode(){
		return(identifier.hashCode());
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result;
		if(o==null||!(o instanceof Weapon))
		    result=false;
		else
			result=hashCode()==((Weapon)o).hashCode();
		return(result);
	}
	
	@Override
	public final int compareTo(final Weapon weapon){
		return(hashCode()-weapon.hashCode());
	}
	
	@Override
	public final String toString(){
		return(identifier);
	}
	
	public final String getIdentifier(){
		return(identifier);
	}
	
	public final int getBlowOrShotDurationInMillis(){
		return(blowOrShotDurationInMillis);
	}
}
