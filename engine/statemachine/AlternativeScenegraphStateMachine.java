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

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;

import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.BasicConditions;
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
 * TODO: add several states: GAME_OVER (display when the player loses), 
 *                           PAUSE_MENU,
 *                           LEVEL_END_DISPLAY (display at the end of a level with figures, etc...)
 *                           GAME_END_DISPLAY (final scene)
 * TODO: move the general source code into a separate class, keep here only the code related on scenegraph states
 */
public class AlternativeScenegraphStateMachine {
	
    /**internal state machine based on Fettle API*/
	private final StateMachine<ScenegraphState,String> internalStateMachine;
	/**transition model based on Fettle API*/
	private final MutableTransitionModelImpl<ScenegraphState,String> transitionModel;
	/**switch node used to show only the nodes of a single state*/
	private final StateMachineSwitchNode switchNode;
    /**tool used to postpone state changes*/
    private final Scheduler scheduler;
    /**state before the latest logical update*/
    private State previousState;
	
	public AlternativeScenegraphStateMachine(final Node parent,final NativeCanvas canvas,
	        final PhysicalLayer physicalLayer,final MouseManager mouseManager,
	        final SoundManager soundManager,final TaskManager taskManager,
	        final TriggerAction exitAction){
	    // creates a condition only satisfied when the task manager has no pending task
	    final NoPendingTaskCondition noPendingTaskCondition=new NoPendingTaskCondition(taskManager);
	    //gets the render context used further to put some actions onto the rendering queue    
	    final RenderContext renderContext=canvas.getCanvasRenderer().getRenderContext();
	    scheduler=new Scheduler();
	    previousState=null;
	    //initializes the switch node
		switchNode=new StateMachineSwitchNode();
		parent.attachChild(switchNode);
		//creates the initial state
		final ScenegraphState contentRatingSystemState=new ContentRatingSystemState(canvas,physicalLayer,mouseManager,exitAction,null,soundManager);
		//creates the transition model
		transitionModel=Fettle.newTransitionModel(ScenegraphState.class,String.class);
		//creates the state machine used internally, based on Fettle API
		internalStateMachine=transitionModel.newStateMachine(contentRatingSystemState);
		//creates events
		final String contentRatingSystemToInitializationEvent=getTransitionEvent(ContentRatingSystemState.class,InitializationState.class);
		final String initializationToIntroductionEvent=getTransitionEvent(InitializationState.class,IntroductionState.class);
		final String introductionToMainMenuEvent=getTransitionEvent(IntroductionState.class,MainMenuState.class);
		final String mainMenuToLoadingDisplayEvent=getTransitionEvent(MainMenuState.class,LoadingDisplayState.class);
		final String loadingDisplayToGameEvent=getTransitionEvent(LoadingDisplayState.class,GameState.class);
		//creates actions allowing to go to the next state by pressing a key
		final TransitionTriggerAction contentRatingSystemToInitializationTriggerAction=new TransitionTriggerAction(internalStateMachine,contentRatingSystemToInitializationEvent,renderContext);
		final TransitionTriggerAction initializationToIntroductionTriggerAction=new TransitionTriggerAction(internalStateMachine,initializationToIntroductionEvent,renderContext);
		final TransitionTriggerAction introductionToMainMenuTriggerAction=new TransitionTriggerAction(internalStateMachine,introductionToMainMenuEvent,renderContext);
		final TransitionTriggerAction mainMenuToLoadingDisplayTriggerAction=new TransitionTriggerAction(internalStateMachine,mainMenuToLoadingDisplayEvent,renderContext);
		final TransitionTriggerAction loadingDisplayToGameTriggerAction=new TransitionTriggerAction(internalStateMachine,loadingDisplayToGameEvent,renderContext);		
		//creates other states
		final ScenegraphState initializationState=new InitializationState(canvas,physicalLayer,exitAction,initializationToIntroductionTriggerAction,soundManager,taskManager);
		final ScenegraphState introductionState=new IntroductionState(canvas,physicalLayer,exitAction,introductionToMainMenuTriggerAction,soundManager);
		final MainMenuState mainMenuState=new MainMenuState(canvas,physicalLayer,mouseManager,exitAction,mainMenuToLoadingDisplayTriggerAction,soundManager);
		final LoadingDisplayState loadingDisplayState=new LoadingDisplayState(canvas,physicalLayer,exitAction,loadingDisplayToGameTriggerAction,soundManager,taskManager);
		final GameState gameState=new GameState(canvas,physicalLayer,exitAction,soundManager,taskManager);
		//adds the state and its actions to the state machine
		addState(contentRatingSystemState,null,new TransitionAction());
		addState(initializationState,null,new TransitionAction());
		addState(introductionState,null,new TransitionAction());
		addState(mainMenuState,null,new TransitionAction());
		addState(loadingDisplayState,null,new TransitionAction());
		addState(gameState,null,new TransitionAction());		
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
        //creates the scheduled tasks
        final StateChangeScheduledTask contentRatingSystemToInitializationTask=new StateChangeScheduledTask(contentRatingSystemState,StateChangeType.ENTRY,2,contentRatingSystemToInitializationTriggerAction,Integer.MAX_VALUE);
        final StateChangeScheduledTask initializationToIntroductionTask=new StateChangeScheduledTask(initializationState,StateChangeType.ENTRY,5,initializationToIntroductionTriggerAction,Integer.MAX_VALUE);
        final StateChangeScheduledTask introductionToMainMenuTask=new StateChangeScheduledTask(introductionState,StateChangeType.ENTRY,17,introductionToMainMenuTriggerAction,Integer.MAX_VALUE);
        //adds the scheduled tasks to the scheduler
        scheduler.addScheduledTask(contentRatingSystemToInitializationTask);
        scheduler.addScheduledTask(initializationToIntroductionTask);
        scheduler.addScheduledTask(introductionToMainMenuTask);
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
		internalStateMachine.getCurrentState().getLogicalLayer().checkTriggers(timer.getTimePerFrame());
		final State currentState=internalStateMachine.getCurrentState();
		scheduler.update(previousState,currentState,timer.getTimePerFrame());
		previousState=currentState;
	}
}
