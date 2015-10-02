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
package engine.statemachine;

/**
 * Statistics of a game, brings together the score, the end status, ...
 * 
 * @author Julien Gouesse
 *
 */
public class GameStatistics{

	private MissionStatus missionStatus;
	
	private int enemiesCount;
	
	private int killedEnemiesCount;
	
	/**
	 * Constructor
	 */
	public GameStatistics(){
		super();
		
	}

	public MissionStatus getMissionStatus(){
		return missionStatus;
	}

	public void setMissionStatus(final MissionStatus missionStatus){
		this.missionStatus = missionStatus;
	}
	
	/**
	 * Returns the time spent in the mission
	 * 
	 * @return time spent in the mission
	 */
	public long getTime(){
		return(0L);
	}
	
	/**
	 * Returns the accuracy of the player during this mission, i.e the 
	 * proportion of hits
	 * 
	 * @return accuracy of the player during this mission
	 */
	public double getAccuracy(){
		return(1.0d);
	}
	
	public int getEnemiesCount(){
		return(enemiesCount);
	}
	
	public void setEnemiesCount(final int enemiesCount){
		if(enemiesCount<0)
		    throw new IllegalArgumentException("The enemies count cannot be negative");
		this.enemiesCount=enemiesCount;
	}
	
	public int getKilledEnemiesCount(){
		return(killedEnemiesCount);
	}
	
	public void setKilledEnemiesCount(final int killedEnemiesCount){
		if(killedEnemiesCount<0)
		    throw new IllegalArgumentException("The killed enemies count cannot be negative");
		this.killedEnemiesCount=killedEnemiesCount;
	}
	
	//TODO weapon of choice, shot total, kill total, head hits, body hits, limb hits, other hits
}
