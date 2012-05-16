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
import se.hiflyer.fettle.Condition;
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
	public static final class SelectionAndPullOutAction implements Action<PlayerState,PlayerEvent>{
    	
        private final PlayerData playerData;
    	
		public SelectionAndPullOutAction(final PlayerData playerData){
			this.playerData=playerData;
		}
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		if(event.equals(PlayerEvent.SELECTING_NEXT))
    		    playerData.selectNextWeapon();
    		else
    		    if(event.equals(PlayerEvent.SELECTING_PREVIOUS))
    			    playerData.selectPreviousWeapon();
    		stateMachine.fireEvent(PlayerEvent.PULLING_OUT);
    	}
    }
    
    private static final class ReloadAndPullOutAction implements Action<PlayerState,PlayerEvent>{

    	private final PlayerData playerData;
    	
		public ReloadAndPullOutAction(final PlayerData playerData){
			this.playerData=playerData;
		}
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		playerData.reload();
    		stateMachine.fireEvent(PlayerEvent.PULLING_OUT);
    	}
    }
    
    /**
     * exit action that cancels a conditional transition if its condition cannot be satisfied anymore because of an interruption caused by an event
     * 
     * @author Julien Gouesse
     *
     */
    public static class ConditionalTransitionCancellerExitAction implements Action<PlayerState,PlayerEvent>{
    	
    	protected final Scheduler<PlayerState> scheduler;
    	
    	protected final ConditionalCancellableTransitionEntryAction conditionalTransitionEntryAction;
    	
    	public ConditionalTransitionCancellerExitAction(final Scheduler<PlayerState> scheduler,final ConditionalCancellableTransitionEntryAction conditionalTransitionEntryAction){
    		this.scheduler=scheduler;
    		this.conditionalTransitionEntryAction=conditionalTransitionEntryAction;
    	}
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		//gets the event expected when the operation has been completed without any interruption
    		final PlayerEvent nextExpectedEvent=conditionalTransitionEntryAction.getNextEvent();
    		//if another event has caused the exit from this state (because of an interruption)
    		if(!event.equals(nextExpectedEvent))
    		    {//tries to get the transition task
    			 final ScheduledTask<PlayerState> transitionTask=conditionalTransitionEntryAction.getTransitionScheduledTask();
    			 //if there is a transition task
    			 if(transitionTask!=null)
    				 //removes it from the scheduler as this transition must not be done
    				 scheduler.removeScheduledTask(transitionTask);
    		    }
    	}
    }
    
    /**
     * entry action that adds a conditional transition task into the scheduler but still allows a conditional transition "canceller" to cancel this transition
     * later if an interruption occurs
     * 
     * @author Julien Gouesse
     *
     */
    public static abstract class ConditionalCancellableTransitionEntryAction implements Action<PlayerState,PlayerEvent>{
    	
        protected final Scheduler<PlayerState> scheduler;
        
        protected final ScheduledTaskCondition<PlayerState> condition;
        
        protected ScheduledTask<PlayerState> transitionScheduledTask;
        
        protected PlayerEvent nextEvent;
        
        public ConditionalCancellableTransitionEntryAction(final Scheduler<PlayerState> scheduler,final ScheduledTaskCondition<PlayerState> condition){
			this.scheduler=scheduler;
			this.condition=condition;
			transitionScheduledTask=null;
			nextEvent=null;
        }
        
        protected abstract PlayerEvent getNextEvent(PlayerEvent event);
        
        public PlayerEvent getNextEvent(){
        	return(nextEvent);
        }
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		//gets the event used in the next transition if there is no interruption
    		nextEvent=getNextEvent(event);
    		//creates the runnable that will fire the proper player event later
    		final Runnable toIdleStateRunnable=new TransitionTriggerAction<PlayerState,PlayerEvent>(stateMachine,nextEvent,null);
    		//creates the scheduled task using the condition and the runnable above
    		transitionScheduledTask=new ScheduledTask<PlayerState>(condition,1,toIdleStateRunnable,0);
    		//adds this task into the scheduler
    		scheduler.addScheduledTask(transitionScheduledTask);
    	}
    	
    	public ScheduledTask<PlayerState> getTransitionScheduledTask(){
    		return(transitionScheduledTask);
    	}
    }
    
    public static final class FromPullOutToIdleAction extends ConditionalCancellableTransitionEntryAction{
    	
    	public FromPullOutToIdleAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
    		//creates the condition satisfied when the "pull out" is complete
    		super(scheduler,new PullOutCompleteCondition(playerData));
		}
    	
    	@Override
    	protected PlayerEvent getNextEvent(PlayerEvent event){
    		return(PlayerEvent.IDLE);
    	}
    }
    
    /**
     * Conditional transitional action that puts a (conditional) scheduled task into the scheduler and fires an event when the condition is satisfied
     * 
     * @author Julien Gouesse
     *
     */
	public static final class FromPutBackTransitionAction extends ConditionalCancellableTransitionEntryAction{
    	
		public FromPutBackTransitionAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			super(scheduler,new PutBackCompleteCondition(playerData));
		}
		
		@Override
		protected PlayerEvent getNextEvent(PlayerEvent event){
			/**
			 * uses a different event to go to "put back" and to go from "put back" to another state to avoid unwanted state changes (for example 
			 * starting another selection when reloading and vice versa)
			 */
			final PlayerEvent nextEvent;
			switch(event)
			    {case PUTTING_BACK_BEFORE_SELECTING_PREVIOUS:
			         {nextEvent=PlayerEvent.SELECTING_PREVIOUS;
			          break;
			         }
			     case PUTTING_BACK_BEFORE_SELECTING_NEXT:
			         {nextEvent=PlayerEvent.SELECTING_NEXT;
			          break;
			         }
			     case PUTTING_BACK_BEFORE_RELOADING:
			         {nextEvent=PlayerEvent.RELOADING;
			          break;
			         }
			     default:
			    	 nextEvent=null;
			    }
    		return(nextEvent);
    	}
    }

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerEvent.class,PlayerState.NOT_YET_AVAILABLE);
        //adds the states and their actions to the state machine        
        final TransitionTriggerAction<PlayerState,PlayerEvent> toIdleAction=new TransitionTriggerAction<PlayerState,PlayerEvent>(internalStateMachine,PlayerEvent.IDLE,null);
        //uses an exit action to update the data
        final AttackAction attackAction=new AttackAction(playerData);
        addState(PlayerState.ATTACK,toIdleAction,attackAction);
        final ReloadAndPullOutAction reloadAndPullOutAction=new ReloadAndPullOutAction(playerData);
        addState(PlayerState.RELOAD,reloadAndPullOutAction,null);
        final FromPutBackTransitionAction fromPutBackTransitionAction=new FromPutBackTransitionAction(playerData,scheduler);
        final ConditionalTransitionCancellerExitAction afterPutBackActionCancellerIfRequiredAction=new ConditionalTransitionCancellerExitAction(scheduler,fromPutBackTransitionAction);
        addState(PlayerState.PUT_BACK,fromPutBackTransitionAction,afterPutBackActionCancellerIfRequiredAction);
        //uses an entry action to select the weapon and to pull it out
        final SelectionAndPullOutAction selectionAndPullOutAction=new SelectionAndPullOutAction(playerData);
        addState(PlayerState.SELECT_NEXT,selectionAndPullOutAction,null);
        addState(PlayerState.SELECT_PREVIOUS,selectionAndPullOutAction,null);
        final FromPullOutToIdleAction fromPullOutToIdleAction=new FromPullOutToIdleAction(playerData,scheduler);
        final ConditionalTransitionCancellerExitAction toIdleActionCancellerIfRequiredAction=new ConditionalTransitionCancellerExitAction(scheduler,fromPullOutToIdleAction);
        addState(PlayerState.PULL_OUT,fromPullOutToIdleAction,toIdleActionCancellerIfRequiredAction);
        //adds all transitions between states to the transition model
        transitionModel.addTransition(PlayerState.NOT_YET_AVAILABLE,PlayerState.IDLE,PlayerEvent.AVAILABLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //no condition is required but an attack may fail (because of a lack of ammo).
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.ATTACK,PlayerEvent.ATTACKING,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final Condition putBackCompleteCondition=new PutBackCompleteCondition(playerData);
        //creates a condition satisfied when the player can reload his weapon(s)
        final ReloadPossibleCondition reloadPossibleCondition=new ReloadPossibleCondition(playerData);
        final Condition reloadPossibleAfterPutBackCondition=BasicConditions.and(reloadPossibleCondition,putBackCompleteCondition);
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_RELOADING,reloadPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.RELOAD,PlayerEvent.RELOADING,reloadPossibleAfterPutBackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates condition satisfied when the player can select another weapon
        final SelectionPossibleCondition nextSelectionPossibleCondition=new SelectionPossibleCondition(playerData,true);
        final SelectionPossibleCondition previousSelectionPossibleCondition=new SelectionPossibleCondition(playerData,false);
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_NEXT,nextSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_PREVIOUS,previousSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //allows to interrupt the "put back" (not yet used, maybe later when the player has just died)
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //allows the selection of another weapon only when the "put back" has ended        
        final Condition nextSelectionPossibleAfterPutBackCondition=BasicConditions.and(nextSelectionPossibleCondition,putBackCompleteCondition);
        final Condition previousSelectionPossibleAfterPutBackCondition=BasicConditions.and(previousSelectionPossibleCondition,putBackCompleteCondition);
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_NEXT,PlayerEvent.SELECTING_NEXT,nextSelectionPossibleAfterPutBackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_PREVIOUS,PlayerEvent.SELECTING_PREVIOUS,previousSelectionPossibleAfterPutBackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates transitions to the idle state
        transitionModel.addTransition(PlayerState.ATTACK,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.RELOAD,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_NEXT,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_PREVIOUS,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.PULL_OUT,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.PULL_OUT,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_NEXT,nextSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.PULL_OUT,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_PREVIOUS,previousSelectionPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.PULL_OUT,PlayerState.PUT_BACK,PlayerEvent.PUTTING_BACK_BEFORE_RELOADING,reloadPossibleCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
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
