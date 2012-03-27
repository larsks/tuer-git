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
public class PlayerStateMachine extends StateMachineWithScheduler<PlayerState,PlayerTransition>{

    /**
     * Exit action that adds a scheduled task (starting when we enter another state) to the scheduler in order 
     * to go back to the idle state after several seconds
     * 
     * @author Julien Gouesse
     *
     */
    private static final class TimedTransitionalActionToIdleState implements Action<PlayerState,PlayerTransition>{

        private final Scheduler<PlayerState> scheduler;
    
        public TimedTransitionalActionToIdleState(final Scheduler<PlayerState> scheduler){
            this.scheduler=scheduler;
        }
    
        @Override
        public void onTransition(PlayerState from,PlayerState to,PlayerTransition event,Arguments arguments,StateMachine<PlayerState,PlayerTransition> stateMachine){
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
            
            private final StateMachine<PlayerState,PlayerTransition> stateMachine;
        
            public ToIdleStateRunnable(StateMachine<PlayerState,PlayerTransition> stateMachine){
                this.stateMachine=stateMachine;
            }
            
            @Override
            public void run(){
                stateMachine.fireEvent(PlayerTransition.IDLE);
            }
        }
    }    

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerTransition.class);
        //sets the initial state
        internalStateMachine.rawSetState(PlayerState.IDLE);
        //adds the states and their actions to the state machine
        addState(PlayerState.IDLE,null,new TimedTransitionalActionToIdleState(scheduler));
        //TODO: use an entry action to launch the animation (perhaps with scheduled tasks) and use an exit action to update the data
        final AttackAction attackAction=new AttackAction(playerData);
        addState(PlayerState.ATTACK,null,attackAction);
        final ReloadAction reloadAction=new ReloadAction(playerData);
        addState(PlayerState.RELOAD,null,reloadAction);        
        
        addState(PlayerState.SELECT_NEXT,null,null);
        
        addState(PlayerState.SELECT_PREVIOUS,null,null);
        //adds all transitions between states to the transition model
        //no condition is required but an attack may fail (because of a lack of ammo).
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.ATTACK,PlayerTransition.ATTACKING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerTransition>>emptyList());
        //creates a condition satisfied when the player can reload his weapon(s)
        final ReloadPossibleCondition reloadPossibleCondition=new ReloadPossibleCondition(playerData);
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.RELOAD,PlayerTransition.RELOADING,reloadPossibleCondition,Collections.<Action<PlayerState,PlayerTransition>>emptyList());
        //TODO: make a distinction between the check and the effective selection
        //TODO: put a condition into this transition: it must be possible to select another weapon
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.SELECT_NEXT,PlayerTransition.SELECTING_NEXT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerTransition>>emptyList());
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.SELECT_PREVIOUS,PlayerTransition.SELECTING_PREVIOUS,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerTransition>>emptyList());
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
