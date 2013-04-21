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
package engine.input;

/**
 * Default actions that the player can do, bounded to a key mapping
 * 
 * @author Julien Gouesse
 *
 */
public enum Action{

	MOVE_FORWARD,
	MOVE_BACKWARD,
	STRAFE_LEFT,
	STRAFE_RIGHT,
	TURN_LEFT,
	TURN_RIGHT,
	LOOK_UP,
	LOOK_DOWN,
	CROUCH,
	PAUSE,
	RELOAD,
	ACTIVATE,
	RUN,
	ATTACK,
	NEXT_WEAPON,
	PREVIOUS_WEAPON,
	QUIT;
}
