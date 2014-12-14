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

/**
 * 
 * @author Julien Gouesse
 *
 */
public class Enemy{

	/**name (can contain space)*/
    private final String label;
	/**unique name*/
    private final String identifier;
	/**name of the resource, i.e the binary file containing the 3D model*/
	private final String resourceName;
	/**paths of the sounds played when the enemy is hurt*/
	private final String[] painSoundSamplePaths;
	/**source names of the sounds played when the enemy is hurt*/
	private final String[] painSoundSampleIdentifiers;
	
	public Enemy(final String label,final String identifier,final String resourceName,final String[] painSoundSamplePaths){
		super();
		this.label=label;
		this.identifier=identifier;
		this.resourceName=resourceName;
		this.painSoundSamplePaths=painSoundSamplePaths;
		this.painSoundSampleIdentifiers=painSoundSamplePaths==null?null:new String[painSoundSamplePaths.length];
	}
	
	public String getLabel(){
		return(label);
	}
	
	public String getIdentifier(){
		return(identifier);
	}
	
	public String getResourceName(){
		return(resourceName);
	}
	
	public String getPainSoundSamplePath(final int index){
		return(painSoundSamplePaths[index]);
	}
	
	public int getPainSoundSampleCount(){
		return(painSoundSamplePaths==null?0:painSoundSamplePaths.length);
	}
	
	public String getPainSoundSampleIdentifier(final int index){
		return(painSoundSampleIdentifiers[index]);
	}
	
	public void setPainSoundSampleIdentifier(final int index,final String painSoundSampleIdentifier){
		painSoundSampleIdentifiers[index]=painSoundSampleIdentifier;
	}
}
