package engine.statemachine;

import java.util.Collections;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;

import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.BasicConditions;
import se.hiflyer.fettle.Fettle;
import se.hiflyer.fettle.StateMachine;
import se.hiflyer.fettle.impl.MutableTransitionModelImpl;
import engine.service.Ardor3DGameServiceProvider.Step;

public class AlternativeScenegraphStateMachine {
	
	private static final Action<ScenegraphState,String> defaultAction = new Action<ScenegraphState,String>(){
		@Override
		public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args, StateMachine<ScenegraphState,String> stateMachine){
			from.setEnabled(false);
			to.setEnabled(true);
		}
	};

	private final StateMachine<ScenegraphState,String> internalStateMachine;
	
	private final MutableTransitionModelImpl<ScenegraphState,String> transitionModel;
	
	private final StateMachineSwitchNode switchNode;
	
	public AlternativeScenegraphStateMachine(final Node parent){
		switchNode=new StateMachineSwitchNode();
		parent.attachChild(switchNode);
		//TODO each state must know which event to fire (see StateMachine.fireEvent(E))
		final ScenegraphState contentRatingSystemState=null;
		transitionModel=Fettle.newTransitionModel(ScenegraphState.class,String.class);
		internalStateMachine=transitionModel.newStateMachine(contentRatingSystemState);
		
		//internalStateMachine.addTransition(Step.CONTENT_RATING_SYSTEM,Step.INITIALIZATION,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.INITIALIZATION,Step.INTRODUCTION,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.INTRODUCTION,Step.MAIN_MENU,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.MAIN_MENU,Step.LEVEL_LOADING_DISPLAY,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.LEVEL_LOADING_DISPLAY,Step.GAME,"pause",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.PAUSE_MENU,"pause",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.LEVEL_END_DISPLAY,"end",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.GAME_OVER,"game over",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
	}
	
	private void addState(final ScenegraphState state,Action<ScenegraphState,String> entryAction,Action<ScenegraphState,String> exitAction){
	    //TODO add the state to the transitional model
		//TODO add an exit action (the dumb one, see defaultAction)
	    //TODO add an entry action only for the state that loads the levels
	    //internalStateMachine.addEntryAction(state, new DefaultEntryAction());
		switchNode.attachChild(state.getRoot());
	}
	
	
	
	public final void updateLogicalLayer(final ReadOnlyTimer timer){
		internalStateMachine.getCurrentState().getLogicalLayer().checkTriggers(timer.getTimePerFrame());
	}
}
