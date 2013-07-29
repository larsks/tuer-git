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
 * Status at the end of a game
 * 
 * @author Julien Gouesse
 *
 */
public enum MissionStatus{

	/**
	 * the objectives of the mission have been achieved and the player is still alive
	 * */
	COMPLETED,
	/**
	 * the player is still alive and has reached the end of the mission but some objectives haven't been achieved
	 */
	FAILED,
	/**
	 * the player has just died during the mission
	 */
	DECEASED,
	/**
	 * the player has just aborted the mission
	 */
	ABORTED;
}
