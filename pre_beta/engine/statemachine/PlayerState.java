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
 * State of player's state machine
 * 
 * @author Julien Gouesse
 *
 */
public enum PlayerState{
	/**
	 * the entity is not yet really available (only used very early as an initial state)
	 */
	NOT_YET_AVAILABLE,
	/**
	 * no operation currently run
	 */
	IDLE,
	/**
	 * the current weapon is being put back (possibly before switching to another one)
	 */
	PUT_BACK,
	/**
	 * the current weapon is being used to attack
	 */
	ATTACK,
	/**
	 * the current weapon is being reloaded
	 */
	RELOAD,
	/**
	 * the previous weapon is going to be selected very soon
	 */
	SELECT_PREVIOUS,
	/**
	 * the next weapon is going to be selected very soon
	 */
	SELECT_NEXT
}