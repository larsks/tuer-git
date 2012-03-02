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
import java.util.concurrent.Callable;

import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;

import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.BasicConditions;
import se.hiflyer.fettle.Condition;
import se.hiflyer.fettle.Fettle;
import se.hiflyer.fettle.StateMachine;
import se.hiflyer.fettle.impl.MutableTransitionModelImpl;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManager;

/**
 * state machine handling the main states of the scenegraph, based on Fettle API
 * 
 * @author Julien Gouesse
 *
 */
public class AlternativeScenegraphStateMachine {
	
	private static final Action<ScenegraphState,String> defaultTransitionAction = new DefaultTransitionAction();
	
	private static final class DefaultTransitionAction implements Action<ScenegraphState,String>{
	
	    @Override
        public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args, StateMachine<ScenegraphState,String> stateMachine){
            from.setEnabled(false);
            to.setEnabled(true);
        }
	}
	
	private static final class DefaultTransitionTriggerAction implements TriggerAction{
	
	    private final StateMachine<ScenegraphState,String> stateMachine;
	    
	    private final String transitionEvent;
	    
	    private final RenderContext renderContext;
	    
	    private DefaultTransitionTriggerAction(StateMachine<ScenegraphState,String> stateMachine, 
	            String transitionEvent, RenderContext renderContext) {
	        this.stateMachine=stateMachine;
	        this.transitionEvent=transitionEvent;
	        this.renderContext=renderContext;
	    }
	
	    @Override
        public void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
            GameTaskQueueManager.getManager(renderContext).update(new Callable<Void>(){
                @Override
                public Void call() throws Exception{
                    stateMachine.fireEvent(transitionEvent);
                    return(null);
                }
            });
        }
	}
	
	private static final class NoPendingTaskCondition implements Condition {

	    private final TaskManager taskManager;
	
	    private NoPendingTaskCondition(TaskManager taskManager){
	        this.taskManager=taskManager;
	    }
	
        @Override
        public boolean isSatisfied(Arguments args){
            return taskManager.getTaskCount()==0;
        }
	    
	}

	private final StateMachine<ScenegraphState,String> internalStateMachine;
	
	private final MutableTransitionModelImpl<ScenegraphState,String> transitionModel;
	
	private final StateMachineSwitchNode switchNode;
	
	private final ScenegraphState contentRatingSystemState;
	
	private final ScenegraphState initializationState;
	
	private final ScenegraphState introductionState;
	
	private static final String contentRatingSystemToInitializationEvent=getTransitionEvent(ContentRatingSystemState.class,InitializationState.class);
	
	private static final String initializationToIntroductionEvent=getTransitionEvent(InitializationState.class,IntroductionState.class);
	
	private static final String introductionToMainMenuEvent=getTransitionEvent(IntroductionState.class,MainMenuState.class);
	
	private static final String mainMenuToLoadingDisplayEvent=getTransitionEvent(MainMenuState.class,LoadingDisplayState.class);
	
	private static final String loadingDisplayToGameEvent=getTransitionEvent(LoadingDisplayState.class,GameState.class);
	
	/**start time of the state "content system rating"*/
    private double contentSystemRatingStartTime;
    
    /**start time of the state "initialization"*/
    private double initializationStartTime;
    
    /**start time of the state "introduction"*/
    private double introductionStartTime;
	
	public AlternativeScenegraphStateMachine(final Node parent,final NativeCanvas canvas,
	        final PhysicalLayer physicalLayer,final MouseManager mouseManager,
	        final SoundManager soundManager,final TaskManager taskManager,
	        final TriggerAction exitAction){
	    // creates a condition only satisfied when the task manager has no pending task
	    final NoPendingTaskCondition noPendingTaskCondition=new NoPendingTaskCondition(taskManager);
	    //gets the render context used further to put some actions onto the rendering queue    
	    final RenderContext renderContext=canvas.getCanvasRenderer().getRenderContext();
	    //initializes start times
	    contentSystemRatingStartTime=Double.NaN;
	    initializationStartTime=Double.NaN;
	    introductionStartTime=Double.NaN;
	    //initializes the switch node
		switchNode=new StateMachineSwitchNode();
		parent.attachChild(switchNode);
		//creates the initial state
		contentRatingSystemState=new ContentRatingSystemState(canvas,physicalLayer,mouseManager,exitAction,null,soundManager);
		//creates the transition model
		transitionModel=Fettle.newTransitionModel(ScenegraphState.class,String.class);
		//creates the state machine used internally, based on Fettle API
		internalStateMachine=transitionModel.newStateMachine(contentRatingSystemState);
		//creates actions allowing to go to the next state by pressing a key
		final DefaultTransitionTriggerAction initializationToIntroductionTriggerAction=new DefaultTransitionTriggerAction(internalStateMachine,initializationToIntroductionEvent,renderContext);
		final DefaultTransitionTriggerAction introductionToMainMenuTriggerAction=new DefaultTransitionTriggerAction(internalStateMachine,introductionToMainMenuEvent,renderContext);
		final DefaultTransitionTriggerAction mainMenuToLoadingDisplayTriggerAction=new DefaultTransitionTriggerAction(internalStateMachine,mainMenuToLoadingDisplayEvent,renderContext);
		final DefaultTransitionTriggerAction loadingDisplayToGameTriggerAction=new DefaultTransitionTriggerAction(internalStateMachine,loadingDisplayToGameEvent,renderContext);		
		//creates other states
		initializationState=new InitializationState(canvas,physicalLayer,exitAction,initializationToIntroductionTriggerAction,soundManager,taskManager);
		introductionState=new IntroductionState(canvas,physicalLayer,exitAction,introductionToMainMenuTriggerAction,soundManager);
		final MainMenuState mainMenuState=new MainMenuState(canvas,physicalLayer,mouseManager,exitAction,mainMenuToLoadingDisplayTriggerAction,soundManager);
		final LoadingDisplayState loadingDisplayState=new LoadingDisplayState(canvas,physicalLayer,exitAction,loadingDisplayToGameTriggerAction,soundManager,taskManager);
		final GameState gameState=new GameState(canvas,physicalLayer,exitAction,soundManager,taskManager);
		//adds the state and its actions to the state machine
		addState(contentRatingSystemState,null,defaultTransitionAction);
		addState(initializationState,null,defaultTransitionAction);
		addState(introductionState,null,defaultTransitionAction);
		addState(mainMenuState,null,defaultTransitionAction);
		addState(loadingDisplayState,null,defaultTransitionAction);
		addState(gameState,null,defaultTransitionAction);
		//adds all transitions between states to the transition model
		transitionModel.addTransition(contentRatingSystemState,initializationState,contentRatingSystemToInitializationEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
		transitionModel.addTransition(initializationState,introductionState,initializationToIntroductionEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
		transitionModel.addTransition(introductionState,mainMenuState,introductionToMainMenuEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
		transitionModel.addTransition(mainMenuState,loadingDisplayState,mainMenuToLoadingDisplayEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
		transitionModel.addTransition(loadingDisplayState,gameState,loadingDisplayToGameEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
		//enqueues other tasks except the first one and in-game tasks
		taskManager.enqueueTask(new StateInitializationRunnable(initializationState));
		taskManager.enqueueTask(new StateInitializationRunnable(introductionState));
		taskManager.enqueueTask(new StateInitializationRunnable(mainMenuState));
		taskManager.enqueueTask(new StateInitializationRunnable(loadingDisplayState));
		//puts the task that loads a level into the level loading state
        loadingDisplayState.setLevelInitializationTask(new StateInitializationRunnable(gameState));		
		//the initial state never uses an entry action, we must enable it explicitly
		contentRatingSystemState.setEnabled(true);
	}
	
	private void addState(final ScenegraphState state,Action<ScenegraphState,String> entryAction,Action<ScenegraphState,String> exitAction){
	    //adds an entry action to the transition model (a separate one for the state that loads the levels)
	    if(entryAction!=null)
            transitionModel.addEntryAction(state, entryAction);
	    //adds an exit action (the dumb one, see defaultAction) to the transition model
        if(exitAction!=null)
            transitionModel.addExitAction(state, exitAction);
		switchNode.attachChild(state.getRoot());
	}
	
	private static final String getTransitionEvent(final Class<? extends ScenegraphState> from,
	        final Class<? extends ScenegraphState> to){
	    final StringBuilder builder=new StringBuilder();
	    builder.append(from.getSimpleName());
	    builder.append(" -> ");
	    builder.append(to.getSimpleName());
	    final String event=builder.toString();
	    return event;
	}
	
	public final void updateLogicalLayer(final ReadOnlyTimer timer){
	    //time constraints
	    //FIXME rather use time-based conditions
	    if(internalStateMachine.getCurrentState().equals(contentRatingSystemState))
	        {if(Double.isNaN(contentSystemRatingStartTime))
                 contentSystemRatingStartTime=timer.getTimeInSeconds();
             if(timer.getTimeInSeconds()-contentSystemRatingStartTime>2)
                 internalStateMachine.fireEvent(contentRatingSystemToInitializationEvent);
	        }
	    if(internalStateMachine.getCurrentState().equals(initializationState))
	        {if(Double.isNaN(initializationStartTime))
                initializationStartTime=timer.getTimeInSeconds();
             if(timer.getTimeInSeconds()-initializationStartTime>5)
                 internalStateMachine.fireEvent(initializationToIntroductionEvent);
	        }
	    if(internalStateMachine.getCurrentState().equals(introductionState))
	        {if(Double.isNaN(introductionStartTime))
                 introductionStartTime=timer.getTimeInSeconds();
             if(timer.getTimeInSeconds()-introductionStartTime>17)
                 internalStateMachine.fireEvent(introductionToMainMenuEvent);
	        }
		internalStateMachine.getCurrentState().getLogicalLayer().checkTriggers(timer.getTimePerFrame());
	}
	
	
	
	private static final class StateInitializationRunnable implements Runnable{
    
    
        private final State state;
    
    
        private StateInitializationRunnable(final State state){
            this.state=state;
        }
    
        @Override
        public final void run(){
            state.init();
        }  
    }
}
