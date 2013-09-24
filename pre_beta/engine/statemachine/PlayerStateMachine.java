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
    
	public static final class ReloadAndPullOutAction implements Action<PlayerState,PlayerEvent>{

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
    
	public static final class EndAttackEntryAction extends CancellableScheduledTaskEntryAction{
		
		public EndAttackEntryAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			super(scheduler,new AttackCompleteCondition(playerData));
		}
		
		@Override
		public boolean isCancellable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
			return(!to.equals(PlayerState.WAIT_FOR_ATTACK_END));
		}

		@Override
		protected Runnable createCancellableRunnable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState, PlayerEvent> stateMachine){
			final Runnable runnable=new TransitionTriggerAction<>(stateMachine,event,null);
			return(runnable);
		}

		@Override
		protected int getScheduledTaskExecutionCount(){
			return(1);
		}

		@Override
		protected double getScheduledTaskTimeOffsetInSeconds(){
			return(0);
		}
		
	}
	
	/**
	 * 
	 * 
	 * @author Julien Gouesse
	 */
	public static final class AttackAndWaitForTriggerReleaseAction extends CancellableScheduledTaskEntryAction{

    	private final PlayerData playerData;
    	
		public AttackAndWaitForTriggerReleaseAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			super(scheduler,BasicScheduledTaskConditions.<PlayerState>always());
			this.playerData=playerData;
		}
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		//performs the attack, it may consume some ammunition
    		playerData.attack();
    		//releases the trigger or prepares the next attack
    		super.onTransition(from,to,event,args,stateMachine);
    	}

		@Override
		public boolean isCancellable(PlayerState from,PlayerState to,PlayerEvent event, Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
			return(!to.equals(PlayerState.ATTACK)||!event.equals(PlayerEvent.ATTACKING));
		}
		
		@Override
		protected int getScheduledTaskExecutionCount(){
        	return(Integer.MAX_VALUE);
        }
		
		@Override
		protected double getScheduledTaskTimeOffsetInSeconds(){
        	return((double)playerData.getCurrentWeaponBlowOrShotDurationInMillis()/1000.0);
        }

		@Override
		protected Runnable createCancellableRunnable(PlayerState from,PlayerState to,PlayerEvent event, Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
			final Runnable runnableToWaitForTriggerRelease=new TransitionTriggerAction<>(stateMachine,PlayerEvent.WAITING_FOR_TRIGGER_RELEASE,null);
			final Runnable runnable=new AttackOrWaitForTriggerReleaseRunnable(playerData,runnableToWaitForTriggerRelease);
			return(runnable);
		}
    }
	
	public static class AttackOrWaitForTriggerReleaseRunnable implements Runnable{
		
		private final PlayerData playerData;
		
		private final Runnable runnableToWaitForTriggerRelease;
		
		public AttackOrWaitForTriggerReleaseRunnable(final PlayerData playerData,final Runnable runnableToWaitForTriggerRelease){
			this.playerData=playerData;
			this.runnableToWaitForTriggerRelease=runnableToWaitForTriggerRelease;
		}
		
		@Override
		public void run(){
			if(playerData.isCurrentWeaponFullyAutomatic()&&playerData.canAttack())
			    {//multiple consecutive attacks may be performed until the player explicitly releases the trigger of his current weapon
				 playerData.attack();
			    }
			else
				//waits for the trigger release as the current weapon doesn't allow multiple consecutive attacks or there is not enough ammunition
				runnableToWaitForTriggerRelease.run();
		}
	}
    
    /**
     * exit action that cancels a conditional transition if its condition cannot be satisfied anymore because of an interruption caused by an event
     * 
     * @author Julien Gouesse
     *
     */
    public static class CancellableScheduledTaskCancellerExitAction implements Action<PlayerState,PlayerEvent>{
    	
    	protected final Scheduler<PlayerState> scheduler;
    	
    	protected final CancellableScheduledTaskEntryAction cancellableEntryAction;
    	
    	public CancellableScheduledTaskCancellerExitAction(final Scheduler<PlayerState> scheduler,final CancellableScheduledTaskEntryAction conditionalTransitionEntryAction){
    		this.scheduler=scheduler;
    		this.cancellableEntryAction=conditionalTransitionEntryAction;
    	}
    	
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		if(cancellableEntryAction.isCancellable(from,to,event,args,stateMachine))
    		    {//tries to get the task
    			 final ScheduledTask<PlayerState> scheduledTask=cancellableEntryAction.getScheduledTask();
    			 //if there is a task
    			 if(scheduledTask!=null)
    				 //removes it from the scheduler as this task must not be executed
    				 scheduler.removeScheduledTask(scheduledTask);
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
    public static abstract class ConditionalCancellableTransitionScheduledTaskEntryAction extends CancellableScheduledTaskEntryAction{
        
        protected PlayerEvent nextEvent;
        
        public ConditionalCancellableTransitionScheduledTaskEntryAction(final Scheduler<PlayerState> scheduler,final ScheduledTaskCondition<PlayerState> condition){
        	super(scheduler,condition);
			nextEvent=null;
        }
        
        protected abstract PlayerEvent getNextEvent(PlayerEvent event);
        
        @Override
        public boolean isCancellable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
        	//if the supplied event isn't the expected next event, then the task can be cancelled
        	return(!event.equals(nextEvent));
        }
        
        @Override
        protected int getScheduledTaskExecutionCount(){
        	return(1);
        }
        
        @Override
        protected double getScheduledTaskTimeOffsetInSeconds(){
        	return(0);
        }
        
        @Override
        protected Runnable createCancellableRunnable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
        	//gets the event used in the next transition if there is no interruption
    		nextEvent=getNextEvent(event);
    		//creates the runnable that will fire the proper player event later
    		final Runnable transitionRunnable=new TransitionTriggerAction<>(stateMachine,nextEvent,null);
    		return(transitionRunnable);
        }
    }
    
    public static abstract class CancellableScheduledTaskEntryAction implements Action<PlayerState,PlayerEvent>{
    	
        protected final Scheduler<PlayerState> scheduler;
        
        protected final ScheduledTaskCondition<PlayerState> condition;
        
        protected ScheduledTask<PlayerState> scheduledTask;
        
        public CancellableScheduledTaskEntryAction(final Scheduler<PlayerState> scheduler,final ScheduledTaskCondition<PlayerState> condition){
			this.scheduler=scheduler;
			this.condition=condition;
			scheduledTask=null;
        }
        
        public abstract boolean isCancellable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine);
        
        protected abstract Runnable createCancellableRunnable(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine);
    	
        protected abstract int getScheduledTaskExecutionCount();
        
        protected abstract double getScheduledTaskTimeOffsetInSeconds();
        
    	@Override
	    public void onTransition(PlayerState from,PlayerState to,PlayerEvent event,Arguments args,StateMachine<PlayerState,PlayerEvent> stateMachine){
    		final Runnable runnable=createCancellableRunnable(from,to,event,args,stateMachine);
    		if(runnable!=null)
    		    {//creates the scheduled task using the condition and the runnable above
    		     scheduledTask=new ScheduledTask<>(condition,getScheduledTaskExecutionCount(),runnable,getScheduledTaskTimeOffsetInSeconds());
    		     //adds this task into the scheduler
    		     scheduler.addScheduledTask(scheduledTask);
    		    }
    		else
    			scheduledTask=null;
    	}
    	
    	public ScheduledTask<PlayerState> getScheduledTask(){
    		return(scheduledTask);
    	}
    }
    
    public static final class FromPullOutToIdleAction extends ConditionalCancellableTransitionScheduledTaskEntryAction{
    	
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
	public static final class FromPutBackTransitionAction extends ConditionalCancellableTransitionScheduledTaskEntryAction{
    	
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
	
	/**
     * Conditional transitional action that puts a (conditional) scheduled task into the scheduler and fires an event when the condition is satisfied
     * 
     * @author Julien Gouesse
     *
     */
	public static final class FromPressTriggerTransitionAction extends ConditionalCancellableTransitionScheduledTaskEntryAction{
    	
		public FromPressTriggerTransitionAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			super(scheduler,new PressTriggerCompleteCondition(playerData));
		}
		
		@Override
		protected PlayerEvent getNextEvent(PlayerEvent event){
    		return(PlayerEvent.ATTACKING);
    	}
    }
	
    public static final class FromReleaseTriggerTransitionAction extends ConditionalCancellableTransitionScheduledTaskEntryAction{
    	
		public FromReleaseTriggerTransitionAction(final PlayerData playerData,final Scheduler<PlayerState> scheduler){
			super(scheduler,new ReleaseTriggerCompleteCondition(playerData));
		}
		
		@Override
		protected PlayerEvent getNextEvent(PlayerEvent event){
    		return(PlayerEvent.IDLE);
    	}
    }

    public PlayerStateMachine(final PlayerData playerData){
        super(PlayerState.class,PlayerEvent.class,PlayerState.NOT_YET_AVAILABLE);
        //adds the states and their actions to the state machine
        final FromPressTriggerTransitionAction fromPressTriggerTransitionAction=new FromPressTriggerTransitionAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction afterPressTriggerActionCancellerIfRequiredAction=new CancellableScheduledTaskCancellerExitAction(scheduler,fromPressTriggerTransitionAction);
        addState(PlayerState.PRESS_TRIGGER,fromPressTriggerTransitionAction,afterPressTriggerActionCancellerIfRequiredAction);
        final AttackAndWaitForTriggerReleaseAction attackAndWaitForTriggerReleaseAction=new AttackAndWaitForTriggerReleaseAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction attackCanceller=new CancellableScheduledTaskCancellerExitAction(scheduler,attackAndWaitForTriggerReleaseAction);
        addState(PlayerState.ATTACK,attackAndWaitForTriggerReleaseAction,attackCanceller);
        final FromReleaseTriggerTransitionAction fromReleaseTriggerTransitionAction=new FromReleaseTriggerTransitionAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction afterReleaseTriggerActionCancellerIfRequiredAction=new CancellableScheduledTaskCancellerExitAction(scheduler,fromReleaseTriggerTransitionAction);
        addState(PlayerState.RELEASE_TRIGGER,fromReleaseTriggerTransitionAction,afterReleaseTriggerActionCancellerIfRequiredAction);
        final ReloadAndPullOutAction reloadAndPullOutAction=new ReloadAndPullOutAction(playerData);
        addState(PlayerState.RELOAD,reloadAndPullOutAction,null);
        final FromPutBackTransitionAction fromPutBackTransitionAction=new FromPutBackTransitionAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction afterPutBackActionCancellerIfRequiredAction=new CancellableScheduledTaskCancellerExitAction(scheduler,fromPutBackTransitionAction);
        addState(PlayerState.PUT_BACK,fromPutBackTransitionAction,afterPutBackActionCancellerIfRequiredAction);
        //uses an entry action to select the weapon and to pull it out
        final SelectionAndPullOutAction selectionAndPullOutAction=new SelectionAndPullOutAction(playerData);
        addState(PlayerState.SELECT_NEXT,selectionAndPullOutAction,null);
        addState(PlayerState.SELECT_PREVIOUS,selectionAndPullOutAction,null);
        final FromPullOutToIdleAction fromPullOutToIdleAction=new FromPullOutToIdleAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction toIdleActionCancellerIfRequiredAction=new CancellableScheduledTaskCancellerExitAction(scheduler,fromPullOutToIdleAction);
        addState(PlayerState.PULL_OUT,fromPullOutToIdleAction,toIdleActionCancellerIfRequiredAction);
        //adds all transitions between states to the transition model
        transitionModel.addTransition(PlayerState.NOT_YET_AVAILABLE,PlayerState.IDLE,PlayerEvent.AVAILABLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //no condition is required but an attack may fail (because of a lack of ammo).
        //transitionModel.addTransition(PlayerState.PRESS_TRIGGER,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final AttackPossibleCondition pressTriggerCondition=new AttackPossibleCondition(playerData);
        transitionModel.addTransition(PlayerState.IDLE,PlayerState.PRESS_TRIGGER,PlayerEvent.PRESSING_TRIGGER,pressTriggerCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final PressTriggerCompleteCondition pressTriggerCompleteCondition=new PressTriggerCompleteCondition(playerData);
        final Condition attackCondition=BasicConditions.and(pressTriggerCondition,pressTriggerCompleteCondition);
        transitionModel.addTransition(PlayerState.PRESS_TRIGGER,PlayerState.ATTACK,PlayerEvent.ATTACKING,attackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final EndAttackEntryAction endAttackEntryAction=new EndAttackEntryAction(playerData,scheduler);
        final CancellableScheduledTaskCancellerExitAction endAttackExitAction=new CancellableScheduledTaskCancellerExitAction(scheduler,endAttackEntryAction);
        addState(PlayerState.WAIT_FOR_ATTACK_END,endAttackEntryAction,endAttackExitAction);
        final AttackCompleteCondition attackCompleteCondition=new AttackCompleteCondition(playerData);
        transitionModel.addTransition(PlayerState.ATTACK,PlayerState.WAIT_FOR_ATTACK_END,PlayerEvent.RELEASING_TRIGGER,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.WAIT_FOR_ATTACK_END,PlayerState.RELEASE_TRIGGER,PlayerEvent.RELEASING_TRIGGER,attackCompleteCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final ReleaseTriggerCompleteCondition releaseTriggerCompleteCondition=new ReleaseTriggerCompleteCondition(playerData);
        transitionModel.addTransition(PlayerState.RELEASE_TRIGGER,PlayerState.IDLE,PlayerEvent.IDLE,releaseTriggerCompleteCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.ATTACK,PlayerState.WAIT_FOR_ATTACK_END,PlayerEvent.WAITING_FOR_TRIGGER_RELEASE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.WAIT_FOR_ATTACK_END,PlayerState.WAIT_FOR_TRIGGER_RELEASE,PlayerEvent.WAITING_FOR_TRIGGER_RELEASE,attackCompleteCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        final WaitForTriggerReleaseCompleteCondition waitForTriggerReleaseCompleteCondition=new WaitForTriggerReleaseCompleteCondition(playerData);
        transitionModel.addTransition(PlayerState.WAIT_FOR_TRIGGER_RELEASE,PlayerState.RELEASE_TRIGGER,PlayerEvent.RELEASING_TRIGGER,waitForTriggerReleaseCompleteCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
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
        //transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.IDLE,PlayerEvent.IDLE,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //allows the selection of another weapon only when the "put back" has ended        
        final Condition nextSelectionPossibleAfterPutBackCondition=BasicConditions.and(nextSelectionPossibleCondition,putBackCompleteCondition);
        final Condition previousSelectionPossibleAfterPutBackCondition=BasicConditions.and(previousSelectionPossibleCondition,putBackCompleteCondition);
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_NEXT,PlayerEvent.SELECTING_NEXT,nextSelectionPossibleAfterPutBackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        transitionModel.addTransition(PlayerState.PUT_BACK,PlayerState.SELECT_PREVIOUS,PlayerEvent.SELECTING_PREVIOUS,previousSelectionPossibleAfterPutBackCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        //creates transitions to the idle state
        transitionModel.addTransition(PlayerState.RELOAD,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_NEXT,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
        transitionModel.addTransition(PlayerState.SELECT_PREVIOUS,PlayerState.PULL_OUT,PlayerEvent.PULLING_OUT,BasicConditions.ALWAYS,Collections.<Action<PlayerState,PlayerEvent>>emptyList());        
        final PullOutCompleteCondition pullOutCompleteCondition=new PullOutCompleteCondition(playerData);
        transitionModel.addTransition(PlayerState.PULL_OUT,PlayerState.IDLE,PlayerEvent.IDLE,pullOutCompleteCondition,Collections.<Action<PlayerState,PlayerEvent>>emptyList());
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
