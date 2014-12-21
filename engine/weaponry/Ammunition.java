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

import java.util.Objects;

import engine.data.common.Collectible;

public class Ammunition extends Collectible implements Comparable<Ammunition>{
	
	/**unique name (cannot contain any space)*/
    private final String identifier;
    /**resource name of the texture used by the box of ammunition*/
    private final String textureResourceName;
	
    public Ammunition(final String label,final String identifier,final String textureResourceName,final String pickingUpSoundSamplePath){
    	super(label,pickingUpSoundSamplePath);
    	this.identifier=Objects.requireNonNull(identifier,"the identifier must not be null");
    	this.textureResourceName=textureResourceName;
    }
    
    @Override
	public int hashCode(){
		return(identifier.hashCode());
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result=o!=null&&o instanceof Ammunition&&identifier.equals(((Ammunition)o).identifier);
		return(result);
	}
	
	@Override
	public int compareTo(final Ammunition ammunition){
		return(identifier.compareTo(ammunition.identifier));
	}
	
	@Override
	public String toString(){
		return(identifier);
	}
	
	public String getIdentifier(){
		return(identifier);
	}
	
	public String getTextureResourceName(){
		return(textureResourceName);
	}
}
