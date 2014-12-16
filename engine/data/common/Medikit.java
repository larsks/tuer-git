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

public class Medikit extends Collectible implements Comparable<Medikit>{

	/**unique name (cannot contain any space)*/
    private final String identifier;
    /**resource name of the texture used by the box of ammunition*/
    private final String textureResourceName;
	
	private final int health;
	
	public Medikit(final String label,final String identifier,final String textureResourceName,final String pickingUpSoundSamplePath,final int health){
		super(label,pickingUpSoundSamplePath);
		this.identifier=identifier;
    	this.textureResourceName=textureResourceName;
		this.health=health;
	}
	
	@Override
	public int hashCode(){
		return(identifier.hashCode());
	}
	
	@Override
	public String toString(){
		return(identifier);
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result;
		if(o==null||!(o instanceof Medikit))
		    result=false;
		else
			result=hashCode()==((Medikit)o).hashCode();
		return(result);
	}
	
	@Override
	public int compareTo(final Medikit medikit){
		return(hashCode()-medikit.hashCode());
	}
	
	public int getHealth(){
		return(health);
	}
	
	public String getIdentifier(){
		return(identifier);
	}
	
	public String getTextureResourceName(){
		return(textureResourceName);
	}
}
