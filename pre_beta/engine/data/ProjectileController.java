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

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;

import engine.misc.ApplicativeTimer;

/**
 * Controller of a projectile which makes a uniformly accelerated rectilinear motion.
 * 
 * @author Julien Gouesse
 *
 */
public class ProjectileController implements SpatialController<Node>{
	
	protected final ApplicativeTimer timer;
	
	protected final ProjectileData projectileData;
	
	public ProjectileController(final ApplicativeTimer timer,final ProjectileData projectileData){
		this.timer=timer;
		this.projectileData=projectileData;
	}
	
	@Override
	public void update(double timeSinceLastCall,Node caller){
		  final long absoluteElapsedTimeInNanoseconds=timer.getElapsedTimeInNanoseconds();
		  //computes the elapsed time between the last call and now
		  final long elapsedTimeSinceLatestCallInNanos=absoluteElapsedTimeInNanoseconds-projectileData.getInitialTimeInNanos();
		  //computes the length of the whole move since the very beginning
		  final double translationLength=((projectileData.getInitialAcceleration()/2)*elapsedTimeSinceLatestCallInNanos*elapsedTimeSinceLatestCallInNanos)+
				  (projectileData.getInitialSpeed()*elapsedTimeSinceLatestCallInNanos);
		  final Vector3 initialDirection=projectileData.getInitialDirection();
		  final Vector3 initialLocation=projectileData.getInitialLocation();
		  //computes the whole translation since the very beginning
		  final double x=initialLocation.getX()+(initialDirection.getX()*translationLength);
		  final double y=initialLocation.getY()+(initialDirection.getY()*translationLength);
		  final double z=initialLocation.getZ()+(initialDirection.getZ()*translationLength);
		  //updates its translation
		  caller.setTranslation(x,y,z);
	  }

}
