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
package engine.data.common;

public abstract class Collectible{
	
	/**name (can contain space)*/
    private final String label;
    /**path of the sound played when picking up this kind of object*/
	protected String pickingUpSoundSamplePath;
	/**source name of the sound played when picking up this kind of object*/
	protected String pickingUpSoundSampleIdentifier;
	
	public Collectible(final String label,final String pickingUpSoundSamplePath){
		super();
		this.label=label;
		this.pickingUpSoundSamplePath=pickingUpSoundSamplePath;
	}
	
	public String getLabel(){
		return(label);
	}
	
	public String getPickingUpSoundSamplePath(){
		return(pickingUpSoundSamplePath);
	}

	public void setPickingUpSoundSamplePath(final String pickingUpSoundSamplePath){
		this.pickingUpSoundSamplePath=pickingUpSoundSamplePath;
	}
	
	public String getPickingUpSoundSampleIdentifier(){
		return(pickingUpSoundSampleIdentifier);
	}
	
	public void setPickingUpSoundSampleIdentifier(final String pickingUpSoundSampleIdentifier){
		this.pickingUpSoundSampleIdentifier=pickingUpSoundSampleIdentifier;
	}	
}
