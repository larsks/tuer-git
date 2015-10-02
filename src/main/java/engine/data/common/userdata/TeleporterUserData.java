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

import com.ardor3d.math.Vector3;
import engine.data.common.Teleporter;

public final class TeleporterUserData extends CollectibleUserData<Teleporter>{
	
	private final Vector3 destination;
	
	private final String destinationLevelIdentifier;
	
	public TeleporterUserData(final Teleporter teleporter,final Vector3 destination,final String destinationLevelIdentifier){
		super(teleporter,null);
		this.destination=destination;
		this.destinationLevelIdentifier=destinationLevelIdentifier;
	}
	
	public final Vector3 getDestination(){
		return(destination);
	}
	
	public final String getDestinationLevelIdentifier(){
		return(destinationLevelIdentifier);
	}
	
	@Override
	public String getPickingUpSoundSampleIdentifier(){
		return(collectible.getPickingUpSoundSampleIdentifier());
	}
}