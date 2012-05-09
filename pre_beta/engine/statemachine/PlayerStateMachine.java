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
public class PlayerStateMachine extends StateMachineWithScheduler<PlayerState,PlayerEvent>{

    /**
     * Conditional transitional action that puts a (conditional) scheduled task into the scheduler and fires an event when the condition is satisfied
     * 
     * @author Julien Gouesse
     *
     */
    private static final class PutBackToSelectionTransitionAction implements Action<PlayerState,PlayerEvent>{

    	private final PlayerData playerData;
    	
    	private final Scheduler<PlayerState> scheduler;
    	
		public PutBackToSelectionTransitionAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			this.playerData=playerData;
			this.scheduler=scheduler;
		}
    	
		@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
			stateMachine.fireEvent(event);
			//FIXME uncomment the lines below when the "put back" test is really working
			/*if(event.equals(PlayerEvent.SELECTING_NEXT)||event.equals(PlayerEvent.SELECTING_PREVIOUS))
			    {//creates the runnable that will fire the proper player event later
			     final Runnable toSelectStateRunnable=new TransitionTriggerAction<PlayerState,PlayerEvent>(stateMachine,event,null);
				 //creates the condition satisfied when the "put back" is complete
			     final ScheduledTaskCondition<PlayerState> putBackCompleteCondition=new PutBackCompleteCondition(playerData);
			     //creates the scheduled task using the condition and the runnable above
			     final ScheduledTask<PlayerState> putBackToSelectTask=new ScheduledTask<PlayerState>(putBackCompleteCondition,1,toSelectStateRunnable,0);
			     //adds this task into the scheduler
			     scheduler.addScheduledTask(putBackToSelectTask);
			    }*/
		}
    }

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerEvent.class,PlayerState.NOT_YET_AVAILABLE);
        //adds the states and their actions to the state machine
        //uses an exit action to update the data
        final TransitionTriggerAction<PlayerState,PlayerEvent> toIdleAction=new TransitionTriggerAction<PlayerState,PlayerEvent>(internalStateMachine,PlayerEvent.IDLE,null);
        final AttackAction attackAction=new AttackAction(playerData);
        addState(PlayerState.ATTACK,toIdleAction,attackAction);
        final ReloadAction reloadAction=new ReloadAction(playerData);
        addState(PlayerState.RELOAD,toIdleAction,reloadAction);        
        final SelectionAction selectionAction=new SelectionAction(playerData);
        final PutBackToSelectionTransitionAction selectionTransitionAction=new PutBackToSelectionTransitionAction(playerData,scheduler);
        addState(PlayerState.PUT_BACK,selectionTransitionAction,null);
        addState(PlayerState.SELECT_NEXT,toIdleAction,selectionAction);
        addState(PlayerState.SELECT_PREVIOUS,toIdleAction,selectionAction);        
        //adds all transitions between states to the transition model
        transitionModel.addTransition(PlayerState.NOT_YET_AVAILABLE,PlayerState.IDLE,PlayerEvent.AVAILABLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //no condition is required but an attack may fail (because of a lack of ammo).
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.ATTACK,PlayerEvent.ATTACKING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates a condition satisfied when the player can reload his weapon(s)
        final ReloadPossibleCondition reloadPossibleCondition=new ReloadPossibleCondition(playerData);
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.RELOAD,PlayerEvent.RELOADING,reloadPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates condition satisfied when the player can select another weapon
        final SelectionPossibleCondition nextSelectionPossibleCondition=new SelectionPossibleCondition(playerData,true);
        final SelectionPossibleCondition previousSelectionPossibleCondition=new SelectionPossibleCondition(playerData,false); 
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PUT_BACK,PlayerEvent.SELECTING_NEXT,nextSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PUT_BACK,PlayerEvent.SELECTING_PREVIOUS,previousSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_NEXT,PlayerEvent.SELECTING_NEXT,nextSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_PREVIOUS,PlayerEvent.SELECTING_PREVIOUS,previousSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates transitions to the idle state
        transitionModel.addTransition(PlayerState.ATTACK,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.RELOAD,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_NEXT,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_PREVIOUS,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //drives the player available
        internalStateMachine.fireEvent(PlayerEvent.AVAILABLE);
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
