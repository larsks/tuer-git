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

public class Ammunition extends Collectible implements Comparable<Ammunition>{
	
	/**unique name (cannot contain any space)*/
    private final String identifier;
    /**name (can contain space)*/
    private final String label;
	
    Ammunition(final String pickingUpSoundSamplePath,final String identifier,final String label){
    	super(pickingUpSoundSamplePath);
    	this.identifier=identifier;
    	this.label=label;
    }
    
    @Override
	public int hashCode(){
		return(identifier.hashCode());
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result;
		if(o==null||!(o instanceof Ammunition))
		    result=false;
		else
			result=hashCode()==((Ammunition)o).hashCode();
		return(result);
	}
	
	@Override
	public final int compareTo(final Ammunition ammunition){
		return(hashCode()-ammunition.hashCode());
	}
	
	@Override
	public final String toString(){
		return(identifier);
	}
	
	public final String getIdentifier(){
		return(identifier);
	}
	
	public final String getLabel(){
		return(label);
	}
}
