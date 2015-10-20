/**
 * Copyright (c) 2006-2015 Julien Gouesse
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
package engine.data.common.userdata;

import engine.data.common.AmmunitionBox;
import engine.weaponry.Ammunition;

/**
 * user date of an ammunition box
 * 
 * @author Julien Gouesse
 *
 */
public final class AmmunitionBoxUserData extends CollectibleUserData<AmmunitionBox>{
	
	public AmmunitionBoxUserData(final AmmunitionBox ammunitionBox){
		super(ammunitionBox,ammunitionBox.getAmmunition().getLabel());
	}
	
	@Override
	public String getPickingUpSoundSampleIdentifier(){
		return(collectible.getPickingUpSoundSampleIdentifier());
	}
	
	public final AmmunitionBox getAmmunitionBox(){
		return(collectible);
	}
	
	public final int getAmmunitionCount(){
		return(collectible.getAmmunitionCount());
	}
	
	public Ammunition getAmmunition(){
		return(collectible.getAmmunition());
	}
}