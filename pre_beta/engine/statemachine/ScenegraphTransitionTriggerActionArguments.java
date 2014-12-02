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
package engine.statemachine;

import java.util.List;

import com.ardor3d.math.Vector3;

import engine.data.Objective;
import se.hiflyer.fettle.Arguments;

/**
 * Arguments used by scenegraph transitions triggers
 * 
 * @author Julien Gouesse
 *
 */
public class ScenegraphTransitionTriggerActionArguments extends Arguments {
	
	public ScenegraphTransitionTriggerActionArguments() {
		this(null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param tag hint used during the transition
	 */
	@SuppressWarnings("cast")
	public ScenegraphTransitionTriggerActionArguments(final String tag) {
		super(new int[]{-1,-1},tag,(Object)new GameStatistics[1],(Object)new List[1],(Object)new Vector3[2]);
	}

	public int getPreviousLevelIndex(){
		return(((int[])getFirst())[0]);
	}
	
	public void setPreviousLevelIndex(final int previousLevelIndex){
		((int[])getFirst())[0]=previousLevelIndex;
	}
	
	public int getNextLevelIndex(){
		return(((int[])getFirst())[1]);
	}
	
	public void setNextLevelIndex(final int nextLevelIndex){
		((int[])getFirst())[1]=nextLevelIndex;
	}
	
	public String getTag(){
		return((String)getArgument(1));
	}
	
	public GameStatistics getGameStatistics(){
		return(((GameStatistics[])getArgument(2))[0]);
	}
	
	public void setGameStatistics(final GameStatistics gameStats){
		((GameStatistics[])getArgument(2))[0]=gameStats;
	}
	
	@SuppressWarnings("unchecked")
	public List<Objective> getObjectives(){
		return((((List[])getArgument(3))[0]));
	}
	
	public void setObjectives(final List<Objective> objectives){
		((List[])getArgument(3))[0]=objectives;
	}
	
	public Vector3 getPreviousLocation(){
		return(((Vector3[])getArgument(4))[0]);
	}
	
	public void setPreviousLocation(final Vector3 previousLocation){
		((Vector3[])getArgument(4))[0]=previousLocation;
	}
	
	public Vector3 getNextLocation(){
		return(((Vector3[])getArgument(4))[1]);
	}
	
	public void setNextLocation(final Vector3 nextLocation){
		((Vector3[])getArgument(4))[1]=nextLocation;
	}
}
