package engine.statemachine;

import java.util.Collections;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.ReadOnlyTimer;

import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.BasicConditions;
import se.hiflyer.fettle.BasicStateMachine;
import se.hiflyer.fettle.ModifiableStateMachine;
import engine.service.Ardor3DGameServiceProvider.Step;

public class AlternativeScenegraphStateMachine {
	
	private static final Action<ScenegraphState,String> defaultAction = new Action<ScenegraphState,String>(){
		@Override
		public void perform(ScenegraphState from,ScenegraphState to,String cause,Arguments args){
			from.setEnabled(false);
			to.setEnabled(true);
		}
	};

	private final ModifiableStateMachine<ScenegraphState,String> internalStateMachine;
	
	private final StateMachineSwitchNode switchNode;
	
	public AlternativeScenegraphStateMachine(final Node parent,final ScenegraphState initialState){
		switchNode=new StateMachineSwitchNode();
		parent.attachChild(switchNode);
		internalStateMachine=BasicStateMachine.createStateMachine(initialState);
		
		//internalStateMachine.addTransition(Step.CONTENT_RATING_SYSTEM,Step.INITIALIZATION,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.INITIALIZATION,Step.INTRODUCTION,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.INTRODUCTION,Step.MAIN_MENU,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.MAIN_MENU,Step.LEVEL_LOADING_DISPLAY,"to",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.LEVEL_LOADING_DISPLAY,Step.GAME,"pause",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.PAUSE_MENU,"pause",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.LEVEL_END_DISPLAY,"end",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
		//internalStateMachine.addTransition(Step.GAME,Step.GAME_OVER,"game over",BasicConditions.ALWAYS,Collections.<Action<Step,String>>emptyList());
	}
	
	public void addState(final ScenegraphState state){
		//internalStateMachine.addEntryAction(state, new DefaultEntryAction());
		switchNode.attachChild(((ScenegraphState)state).getRoot());
	}
	
	
	
	public final void updateLogicalLayer(final ReadOnlyTimer timer){
		internalStateMachine.getCurrentState().getLogicalLayer().checkTriggers(timer.getTimePerFrame());
	}
}
