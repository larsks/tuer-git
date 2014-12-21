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

import java.util.Objects;

public class Teleporter extends Collectible implements Comparable<Teleporter>{

	/**unique name (cannot contain any space)*/
    private final String identifier;
	
	public Teleporter(final String label,final String identifier,final String pickingUpSoundSamplePath){
		super(label,pickingUpSoundSamplePath);
		this.identifier=Objects.requireNonNull(identifier,"the identifier must not be null");
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
		final boolean result=o!=null&&o instanceof Teleporter&&identifier.equals(((Teleporter)o).identifier);
		return(result);
	}
	
	@Override
	public int compareTo(final Teleporter teleporter){
		return(identifier.compareTo(teleporter.identifier));
	}
	
	public String getIdentifier(){
		return(identifier);
	}
}
