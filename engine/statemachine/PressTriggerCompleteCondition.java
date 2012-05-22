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

import engine.data.PlayerData;

/**
 * Condition satisfied when the "press trigger" is complete, when the attack is going to start very soon
 * 
 * @author Julien Gouesse
 *
 */
public class PressTriggerCompleteCondition extends ScheduledTaskCondition<PlayerState>{

	private final PlayerData playerData;
	
	public PressTriggerCompleteCondition(final PlayerData playerData){
		this.playerData=playerData;
	}
	
	@Override
	public boolean isSatisfied(final PlayerState previousState,final PlayerState currentState){
		/**
		 * TODO implement here some behavior depending on weapons. Some weapons might need to press the trigger
		 * for some time before really shooting
		 */
		return(true);
	}

}
