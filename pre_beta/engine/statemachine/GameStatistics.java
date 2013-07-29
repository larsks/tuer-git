/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
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
	
	//TODO weapon of choice, shot total, kill total, head hits, body hits, limb hits, other hits
}
