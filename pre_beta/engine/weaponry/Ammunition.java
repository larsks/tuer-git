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

public class Ammunition extends Collectible{
    /**resource name of the texture used by the box of ammunition*/
    private final String textureResourceName;
	
    public Ammunition(final String label,final String textureResourceName,final String pickingUpSoundSamplePath){
    	super(label,pickingUpSoundSamplePath);
    	this.textureResourceName=textureResourceName;
    }
	
	public String getTextureResourceName(){
		return(textureResourceName);
	}
}
