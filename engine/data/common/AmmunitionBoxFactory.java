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

import engine.abstraction.AbstractFactory;
import engine.weaponry.Ammunition;

public class AmmunitionBoxFactory extends AbstractFactory<AmmunitionBox>{

	public AmmunitionBoxFactory(){
		super();
	}
	
	public boolean addNewAmmunitionBox(final String label,final String identifier,final String pickingUpSoundSamplePath,final Ammunition ammunition,final String textureResourceName,final int ammunitionCount){
		boolean success=identifier!=null&&!componentMap.containsKey(identifier);
		if(success)
			{final AmmunitionBox ammunitionBox=new AmmunitionBox(label,pickingUpSoundSamplePath,ammunition,textureResourceName,ammunitionCount);
			 success=add(identifier,ammunitionBox);
			}
		return(success);
	}
}
