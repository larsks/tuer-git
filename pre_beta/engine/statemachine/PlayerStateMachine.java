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
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.BasicConditions;
import se.hiflyer.fettle.StateMachine;

/**
 * State machine handling player states. All transitions go to and come from the idle state.
 * 
 * 
 * @author Julien Gouesse
 *
 */
public class PlayerStateMachine extends StateMachineWithScheduler<PlayerState,PlayerState>{

    /**
     * Exit action that adds a scheduled task (starting when we enter another state) to the scheduler in order 
     * to go back to the idle state after several seconds
     * 
     * @author Julien Gouesse
     *
     */
    private static final class TimedTransitionalActionToIdleState implements Action<PlayerState,PlayerState>{

        private final Scheduler<PlayerState> scheduler;
    
        public TimedTransitionalActionToIdleState(final Scheduler<PlayerState> scheduler){
            this.scheduler=scheduler;
        }
    
        @Override
        public void onTransition(PlayerState from,PlayerState to,PlayerState event,Arguments arguments,StateMachine<PlayerState,PlayerState> stateMachine){
            //this task must be executed only one time
            final int executionCount=1;
            //FIXME it should be set elsewhere
            final double timeOffsetInSeconds=0.2;
            //builds the runnable that fires the proper event
            final Runnable runnable=new ToIdleStateRunnable(stateMachine);
            //creates the task
            final StateChangeScheduledTask<PlayerState> stateChangeScheduledTask=new StateChangeScheduledTask<PlayerState>(to,StateChangeType.ENTRY,timeOffsetInSeconds,runnable,executionCount);
            //adds it to the scheduler
            scheduler.addScheduledTask(stateChangeScheduledTask);
        }
    
        /**
         * Runnable that fires the idle event to go back to the idle state
         * 
         * @author Julien Gouesse
         *
         */
        private static final class ToIdleStateRunnable implements Runnable{
            
            private final StateMachine<PlayerState,PlayerState> stateMachine;
        
            public ToIdleStateRunnable(StateMachine<PlayerState,PlayerState> stateMachine){
                this.stateMachine=stateMachine;
            }
            
            @Override
            public void run(){
                stateMachine.fireEvent(PlayerState.IDLE);
            }
        }
    }

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerState.class);
        //sets the initial state
        internalStateMachine.rawSetState(PlayerState.IDLE);
        //adds the states and their actions to the state machine
        addState(PlayerState.IDLE,null,new TimedTransitionalActionToIdleState(scheduler));
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
