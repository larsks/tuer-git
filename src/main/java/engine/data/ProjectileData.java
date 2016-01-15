/**
 * Copyright (c) 2006-2016 Julien Gouesse
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

import com.ardor3d.math.Vector3;

/**
 * 
 * @author Julien Gouesse
 *
 */
public class ProjectileData{
    
	private final String originator;
	
	private final Vector3 initialLocation;
	
	private final double initialSpeed;
	
	private final double initialAcceleration;
	
	private final Vector3 initialDirection;
	
	private final long initialTimeInNanos;
	
	public ProjectileData(final String originator,final Vector3 initialLocation,final double initialSpeed,final double initialAcceleration,
			final Vector3 initialDirection,final long initialTimeInNanos){
		this.originator=originator;
		this.initialLocation=initialLocation;
		this.initialSpeed=initialSpeed;
		this.initialAcceleration=initialAcceleration;
		this.initialDirection=initialDirection;
		this.initialTimeInNanos=initialTimeInNanos;
	}
	
	public String getOriginator(){
		return(originator);
	}
	
	public Vector3 getInitialLocation(){
		return(initialLocation);
	}

	public double getInitialSpeed(){
		return(initialSpeed);
	}

	public double getInitialAcceleration(){
		return(initialAcceleration);
	}
	
	public Vector3 getInitialDirection(){
		return(initialDirection);
	}

	public long getInitialTimeInNanos(){
		return(initialTimeInNanos);
	}
}
