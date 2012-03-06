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

import java.util.Collections;
import com.ardor3d.util.ReadOnlyTimer;
import engine.data.PlayerData;
import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.BasicConditions;


public class PlayerStateMachine extends StateMachineWithScheduler<PlayerState,PlayerState>{

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerState.class);
        //sets the initial state
        internalStateMachine.rawSetState(PlayerState.IDLE);
        //adds the states and their actions to the state machine
        addState(PlayerState.IDLE,null,null);
        //TODO: add some entry actions that add scheduled tasks to the scheduler in order to go back to the idle state after several seconds
        addState(PlayerState.ATTACKING,null,null);
        addState(PlayerState.RELOADING,null,null);
        addState(PlayerState.SELECTING,null,null);
        //adds all transitions between states to the transition model
        //TODO: put a condition into this transition: the attack must consume some ammo
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.ATTACKING,PlayerState.ATTACKING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerState>>emptyList());
        //TODO: put a condition into this transition: the weapon factory must not be empty, at least one (not for melee) weapon must be selected and at least one magazine must not be full
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.RELOADING,PlayerState.RELOADING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerState>>emptyList());
        //TODO: put a condition into this transition: the weapon factory must not be empty
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.SELECTING,PlayerState.SELECTING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerState>>emptyList());       
    }
    
    /**
     * Updates the logical layer of the player
     * 
     * @param timer timer (using the applicative timer is highly recommended)
     */
    @Override
    public void updateLogicalLayer(final ReadOnlyTimer timer){
        super.updateLogicalLayer(timer);
    }
}
