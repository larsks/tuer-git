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
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;
import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.BasicConditions;
import engine.input.ActionMap;
import engine.input.MouseAndKeyboardSettings;
import engine.misc.FontStore;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManager;

/**
 * state machine handling the main states of the scenegraph
 * 
 * @author Julien Gouesse
 *
 * TODO: add several states: GAME_OVER (display when the player loses), 
 *                           PAUSE_MENU,
 *                           LEVEL_END_DISPLAY (display at the end of a level with figures, etc...)
 *                           GAME_END_DISPLAY (final scene)
 *       
 *       add an accepting state to this machine to handle the cleanup 
 */
public class ScenegraphStateMachine extends StateMachineWithScheduler<ScenegraphState,String>{
	
    /**switch node used to show only the nodes of a single state*/
    private final StateMachineSwitchNode switchNode;
    
    private final TaskManager taskManager;
    
    private final FontStore fontStore;
    
    private final ActionMap defaultActionMap;
    
    private final ActionMap customActionMap;
    
    private final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings;
    
    private final MouseAndKeyboardSettings customMouseAndKeyboardSettings;
    
    /**
     * sound manager used to play sound samples and music
     * */    
    private final SoundManager soundManager;
    
    public ScenegraphStateMachine(final Node parent,final NativeCanvas canvas,
            final PhysicalLayer physicalLayer,final MouseManager mouseManager,
            final TriggerAction toggleScreenModeAction,final Runnable launchRunnable,
            final Runnable uninstallRunnable,final String creditsContent,
            final ActionMap defaultActionMap,final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings){
        super(ScenegraphState.class,String.class,new ScenegraphState());
        fontStore=new FontStore();
        taskManager=new TaskManager();
        soundManager=new SoundManager();
        if(defaultMouseAndKeyboardSettings==null)
            {this.defaultMouseAndKeyboardSettings=new MouseAndKeyboardSettings();
             this.defaultMouseAndKeyboardSettings.setKeyRotateSpeed(2.2);
             this.defaultMouseAndKeyboardSettings.setLookUpDownReversed(false);
             this.defaultMouseAndKeyboardSettings.setMousePointerNeverHidden(false);
             this.defaultMouseAndKeyboardSettings.setMouseRotateSpeed(0.005);
             this.defaultMouseAndKeyboardSettings.setMoveSpeed(5);
            }
        else
            this.defaultMouseAndKeyboardSettings=defaultMouseAndKeyboardSettings;
        customMouseAndKeyboardSettings=this.defaultMouseAndKeyboardSettings.clone();
        if(defaultActionMap==null)
            {this.defaultActionMap=new ActionMap();
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.MOVE_FORWARD,Key.W);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.MOVE_FORWARD,Key.Z);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.MOVE_FORWARD,Key.NUMPAD8);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.MOVE_BACKWARD,Key.S);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.MOVE_BACKWARD,Key.NUMPAD2);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.STRAFE_LEFT,Key.A);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.STRAFE_LEFT,Key.Q);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.STRAFE_LEFT,Key.NUMPAD4);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.STRAFE_RIGHT,Key.D);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.STRAFE_RIGHT,Key.NUMPAD6);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.TURN_LEFT,Key.LEFT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.TURN_RIGHT,Key.RIGHT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.LOOK_UP,Key.UP);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.LOOK_DOWN,Key.DOWN);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.CROUCH,Key.C);
             this.defaultActionMap.setMouseButtonActionBinding(engine.input.Action.CROUCH,MouseButton.MIDDLE);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.PAUSE,Key.P);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.RELOAD,Key.R);
             this.defaultActionMap.setMouseButtonActionBinding(engine.input.Action.RELOAD,MouseButton.RIGHT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.ACTIVATE,Key.RETURN);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.RUN,Key.LSHIFT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.RUN,Key.RSHIFT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.ATTACK,Key.SPACE);
             this.defaultActionMap.setMouseButtonActionBinding(engine.input.Action.ATTACK,MouseButton.LEFT);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.NEXT_WEAPON,Key.M);
             this.defaultActionMap.setMouseWheelMoveActionBinding(engine.input.Action.NEXT_WEAPON,Boolean.TRUE);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.PREVIOUS_WEAPON,Key.L);
             this.defaultActionMap.setMouseWheelMoveActionBinding(engine.input.Action.PREVIOUS_WEAPON,Boolean.FALSE);
             this.defaultActionMap.setKeyActionBinding(engine.input.Action.QUIT,Key.ESCAPE);
            }
        else
        	this.defaultActionMap=defaultActionMap;
        this.customActionMap=this.defaultActionMap.clone();
        // creates a condition only satisfied when the task manager has no pending task
        final NoPendingTaskCondition noPendingTaskCondition=new NoPendingTaskCondition(taskManager);
        //gets the render context used further to put some actions onto the rendering queue    
        final RenderContext renderContext=canvas.getCanvasRenderer().getRenderContext();
        //initializes the switch node
        switchNode=new StateMachineSwitchNode();
        parent.attachChild(switchNode);
        //creates events
        final String initialScenegraphStateToContentRatingSystemEvent=getTransitionEvent(ScenegraphState.class,ContentRatingSystemState.class);
        final String contentRatingSystemToInitializationEvent=getTransitionEvent(ContentRatingSystemState.class,InitializationState.class);
        final String initializationToIntroductionEvent=getTransitionEvent(InitializationState.class,IntroductionState.class);
        final String introductionToMainMenuEvent=getTransitionEvent(IntroductionState.class,MainMenuState.class);
        final String mainMenuToLoadingDisplayEvent=getTransitionEvent(MainMenuState.class,LoadingDisplayState.class);
        final String loadingDisplayToGameEvent=getTransitionEvent(LoadingDisplayState.class,GameState.class);
        final String gameToPauseMenuEvent=getTransitionEvent(GameState.class,PauseMenuState.class);
        final String gameToGameOverEvent=getTransitionEvent(GameState.class,GameOverState.class);
        final String pauseMenuToGameEvent=getTransitionEvent(PauseMenuState.class,GameState.class);
        final String pauseMenuToGameOverEvent=getTransitionEvent(PauseMenuState.class,GameOverState.class);
        final String pauseMenuToUnloadingDisplayEvent=getTransitionEvent(PauseMenuState.class,UnloadingDisplayState.class);
        final String unloadingDisplayToExitGameEvent=getTransitionEvent(UnloadingDisplayState.class,ExitGameState.class);
        final String unloadingDisplayToMainMenuEvent=getTransitionEvent(UnloadingDisplayState.class,MainMenuState.class);
        final String unloadingDisplayToLoadingDisplayEvent=getTransitionEvent(UnloadingDisplayState.class,LoadingDisplayState.class);
        final String initializationToExitGameEvent=getTransitionEvent(InitializationState.class,ExitGameState.class);
        final String introductionToExitGameEvent=getTransitionEvent(IntroductionState.class,ExitGameState.class);
        final String mainMenuToExitGameEvent=getTransitionEvent(MainMenuState.class,ExitGameState.class);
        final String loadingDisplayToUnloadingDisplayEvent=getTransitionEvent(LoadingDisplayState.class,UnloadingDisplayState.class);
        final String gameOverToUnloadingDisplayEvent=getTransitionEvent(GameOverState.class,UnloadingDisplayState.class);
        //creates actions allowing to go to the next state by pressing a key
        final TransitionTriggerAction<ScenegraphState,String> contentRatingSystemToInitializationTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,contentRatingSystemToInitializationEvent,renderContext);
        //the initialization state must be leaved only once there is no pending task
        final TransitionTriggerAction<ScenegraphState,String> initializationToIntroductionTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,initializationToIntroductionEvent,renderContext);
        final Runnable initializationToIntroductionRunnable=new Runnable(){
        	@Override
        	public void run(){
        		final ScheduledTask<ScenegraphState> goToIntroductionWhenNoPendingTask=new ScheduledTask<ScenegraphState>(noPendingTaskCondition,1,initializationToIntroductionTriggerAction,0);
        		scheduler.addScheduledTask(goToIntroductionWhenNoPendingTask);
        	}
        };
        final TransitionTriggerAction<ScenegraphState,String> introductionToMainMenuTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,introductionToMainMenuEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> mainMenuToLoadingDisplayTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,mainMenuToLoadingDisplayEvent,new Arguments(new int[]{-1}),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> loadingDisplayToGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,loadingDisplayToGameEvent,renderContext);      
        final TransitionTriggerAction<ScenegraphState,String> gameToPauseMenuTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameToPauseMenuEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> gameToPauseMenuTriggerActionForExitConfirm=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameToPauseMenuEvent,new Arguments(PauseMenuStateEntryAction.EXIT_CONFIRM_TAG),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> pauseMenuToGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,pauseMenuToGameEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> pauseMenuToGameOverTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,pauseMenuToGameOverEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> pauseMenuToUnloadingDisplayTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,pauseMenuToUnloadingDisplayEvent,new Arguments(UnloadingDisplayStateEntryAction.EXIT_TAG),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> unloadingDisplayToExitGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,unloadingDisplayToExitGameEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> unloadingDisplayToMainMenuTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,unloadingDisplayToMainMenuEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> unloadingDisplayToLoadingDisplayTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,unloadingDisplayToLoadingDisplayEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> initializationToExitGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,initializationToExitGameEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> introductionToExitGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,introductionToExitGameEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> mainMenuToExitGameTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,mainMenuToExitGameEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> loadingDisplayToUnloadingDisplayTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,loadingDisplayToUnloadingDisplayEvent,new Arguments(UnloadingDisplayStateEntryAction.EXIT_TAG),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> gameToGameOverTriggerAction=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameToGameOverEvent,renderContext);
        final TransitionTriggerAction<ScenegraphState,String> gameOverToUnloadingDisplayTriggerActionForExit=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameOverToUnloadingDisplayEvent,new Arguments(UnloadingDisplayStateEntryAction.EXIT_TAG),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> gameOverToUnloadingDisplayTriggerActionForMainMenu=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameOverToUnloadingDisplayEvent,new Arguments(UnloadingDisplayStateEntryAction.MAIN_MENU_TAG),renderContext);
        final TransitionTriggerAction<ScenegraphState,String> gameOverToUnloadingDisplayTriggerActionForLoadingDisplay=new TransitionTriggerAction<ScenegraphState,String>(internalStateMachine,gameOverToUnloadingDisplayEvent,new Arguments(UnloadingDisplayStateEntryAction.LEVEL_TAG,new int[]{-1}),renderContext);
        //creates states
        final ScenegraphState initialState=internalStateMachine.getCurrentState();
        final ContentRatingSystemState contentRatingSystemState=new ContentRatingSystemState(canvas,physicalLayer,mouseManager,soundManager,fontStore);
        final InitializationState initializationState=new InitializationState(canvas,physicalLayer,initializationToExitGameTriggerAction,initializationToIntroductionTriggerAction,soundManager,taskManager);
        final IntroductionState introductionState=new IntroductionState(canvas,physicalLayer,introductionToExitGameTriggerAction,introductionToMainMenuTriggerAction,soundManager,fontStore);
        final MainMenuState mainMenuState=new MainMenuState(canvas,physicalLayer,mouseManager,mainMenuToExitGameTriggerAction,mainMenuToLoadingDisplayTriggerAction,soundManager,launchRunnable,uninstallRunnable,creditsContent,fontStore,toggleScreenModeAction,this.defaultActionMap,this.customActionMap,this.defaultMouseAndKeyboardSettings,this.customMouseAndKeyboardSettings);
        final GameState gameState=new GameState(canvas,physicalLayer,gameToPauseMenuTriggerAction,gameToPauseMenuTriggerActionForExitConfirm,gameToGameOverTriggerAction,toggleScreenModeAction,soundManager,taskManager,mouseManager,this.defaultActionMap,this.customActionMap,this.defaultMouseAndKeyboardSettings,this.customMouseAndKeyboardSettings);
        final LoadingDisplayState loadingDisplayState=new LoadingDisplayState(canvas,physicalLayer,loadingDisplayToGameTriggerAction,loadingDisplayToUnloadingDisplayTriggerAction,soundManager,taskManager,new StateInitializationRunnable<GameState>(gameState),fontStore);
        final PauseMenuState pauseMenuState=new PauseMenuState(canvas,physicalLayer,mouseManager,pauseMenuToGameTriggerAction,pauseMenuToGameOverTriggerAction,pauseMenuToUnloadingDisplayTriggerAction,soundManager,fontStore);
        final GameOverState gameOverState=new GameOverState(canvas,physicalLayer,mouseManager,soundManager,fontStore,gameOverToUnloadingDisplayTriggerActionForExit,gameOverToUnloadingDisplayTriggerActionForMainMenu,gameOverToUnloadingDisplayTriggerActionForLoadingDisplay);
        final UnloadingDisplayState unloadingDisplayState=new UnloadingDisplayState(canvas,taskManager,soundManager);
        final ExitGameState exitGameState=new ExitGameState(canvas,soundManager);
        //adds the states and their actions to the state machine
        addState(contentRatingSystemState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        addState(initializationState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        addState(introductionState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        addState(mainMenuState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        //uses an entry action to get the level index and pass it to the game state
        addState(loadingDisplayState,new LoadingDisplayStateEntryAction(gameState),new ScenegraphStateExitAction());
        addState(gameState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        addState(pauseMenuState,new PauseMenuStateEntryAction(),new ScenegraphStateExitAction());
        addState(gameOverState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        addState(unloadingDisplayState,new UnloadingDisplayStateEntryAction(scheduler,noPendingTaskCondition,unloadingDisplayToExitGameTriggerAction,unloadingDisplayToMainMenuTriggerAction,unloadingDisplayToLoadingDisplayTriggerAction),new ScenegraphStateExitAction());
        addState(exitGameState,new ScenegraphStateEntryAction(),new ScenegraphStateExitAction());
        //adds all transitions between states to the transition model
        transitionModel.addTransition(initialState,contentRatingSystemState,initialScenegraphStateToContentRatingSystemEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(contentRatingSystemState,initializationState,contentRatingSystemToInitializationEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(initializationState,introductionState,initializationToIntroductionEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(introductionState,mainMenuState,introductionToMainMenuEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(mainMenuState,loadingDisplayState,mainMenuToLoadingDisplayEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(loadingDisplayState,gameState,loadingDisplayToGameEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(pauseMenuState,gameState,pauseMenuToGameEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(gameState,pauseMenuState,gameToPauseMenuEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(pauseMenuState,gameOverState,pauseMenuToGameOverEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(pauseMenuState,unloadingDisplayState,pauseMenuToUnloadingDisplayEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(unloadingDisplayState,exitGameState,unloadingDisplayToExitGameEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(unloadingDisplayState,mainMenuState,unloadingDisplayToMainMenuEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(unloadingDisplayState,loadingDisplayState,unloadingDisplayToLoadingDisplayEvent,noPendingTaskCondition,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(initializationState,exitGameState,initializationToExitGameEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(introductionState,exitGameState,introductionToExitGameEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(mainMenuState,exitGameState,mainMenuToExitGameEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        transitionModel.addTransition(loadingDisplayState,unloadingDisplayState,loadingDisplayToUnloadingDisplayEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        //the player wins or loses a game whatever the reason
        transitionModel.addTransition(gameState,gameOverState,gameToGameOverEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        //the player chooses to restart the current mission, to go to the next one, to go to the main menu or to quit the game
        transitionModel.addTransition(gameOverState,unloadingDisplayState,gameOverToUnloadingDisplayEvent,BasicConditions.ALWAYS,Collections.<Action<ScenegraphState,String>>emptyList());
        //enqueues other tasks except the first one and in-game tasks
        taskManager.enqueueTask(new StateInitializationRunnable<InitializationState>(initializationState));
        taskManager.enqueueTask(new StateInitializationRunnable<IntroductionState>(introductionState));
        taskManager.enqueueTask(new StateInitializationRunnable<MainMenuState>(mainMenuState));
        taskManager.enqueueTask(new StateInitializationRunnable<LoadingDisplayState>(loadingDisplayState));
        //creates the scheduled tasks
        final ScheduledTask<ScenegraphState> contentRatingSystemToInitializationTask=new StateChangeScheduledTask<ScenegraphState>(Integer.MAX_VALUE,contentRatingSystemToInitializationTriggerAction,2.0,contentRatingSystemState,StateChangeType.ENTRY);
        final ScheduledTask<ScenegraphState> initializationToIntroductionTask=new StateChangeScheduledTask<ScenegraphState>(Integer.MAX_VALUE,initializationToIntroductionRunnable,5.0,initializationState,StateChangeType.ENTRY);
        final ScheduledTask<ScenegraphState> introductionToMainMenuTask=new StateChangeScheduledTask<ScenegraphState>(Integer.MAX_VALUE,introductionToMainMenuTriggerAction,17.0,introductionState,StateChangeType.ENTRY);
        //adds the scheduled tasks to the scheduler
        scheduler.addScheduledTask(contentRatingSystemToInitializationTask);
        scheduler.addScheduledTask(initializationToIntroductionTask);
        scheduler.addScheduledTask(introductionToMainMenuTask);
        //goes to the content rating system state
        internalStateMachine.fireEvent(initialScenegraphStateToContentRatingSystemEvent);
    }
    
    @Override
    protected void addState(final ScenegraphState state,Action<ScenegraphState,String> entryAction,Action<ScenegraphState,String> exitAction){
        super.addState(state,entryAction,exitAction);
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
    
    @Override
    public void updateLogicalLayer(final ReadOnlyTimer timer){
        internalStateMachine.getCurrentState().getLogicalLayer().checkTriggers(timer.getTimePerFrame());
        super.updateLogicalLayer(timer);
    }
}
