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
package engine.data;

import java.util.Objects;

/**
 * 
 * 
 * @author Julien Gouesse
 *
 */
public class Skybox implements Comparable<Skybox>{

	/**name (can contain space)*/
    private final String label;
    /**unique name (cannot contain any space)*/
    private final String identifier;
    /**resource names of the textures used by the sky box*/
    private final String[] textureResourceNames;
    
	
	public Skybox(final String label,final String identifier,final String[] textureResourceNames){
		super();
		this.label=label;
		this.identifier=Objects.requireNonNull(identifier,"the identifier must not be null");
		this.textureResourceNames=textureResourceNames;
	}
	
	@Override
	public String toString(){
		return(identifier);
	}
	
	public String getLabel(){
		return(label);
	}
	
	public String getIdentifier(){
		return(identifier);
	}
	
	@Override
	public int hashCode(){
		return(identifier.hashCode());
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result=o!=null&&o instanceof Skybox&&identifier.equals(((Skybox)o).identifier);
		return(result);
	}
	
	@Override
	public int compareTo(final Skybox skybox){
		return(identifier.compareTo(skybox.identifier));
	}
	
	public int getTextureResourceNameCount(){
		return(textureResourceNames==null?0:textureResourceNames.length);
	}
	
	public String getTextureResourceName(final int index){
		return(textureResourceNames[index]);
	}
}
