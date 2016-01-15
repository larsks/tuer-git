/**
 * Copyright (c) 2006-2016 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.statemachine;

import com.ardor3d.util.ReadOnlyTimer;
import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.Fettle;
import se.hiflyer.fettle.StateMachine;
import se.hiflyer.fettle.impl.MutableTransitionModelImpl;

/**
 * General state machine that relies on a scheduler and based on Fettle API
 * 
 * @author Julien Gouesse
 *
 * @param <S> state class
 * @param <E> event class
 */
public class StateMachineWithScheduler<S,E>{

    /**internal state machine based on Fettle API*/
    protected final StateMachine<S,E> internalStateMachine;
    /**transition model based on Fettle API*/
    protected final MutableTransitionModelImpl<S,E> transitionModel;
    /**tool used to postpone state changes*/
    protected final Scheduler<S> scheduler;
    /**state before the latest logical update*/
    protected S previousState;
    
    /**
     * Constructor
     * 
     * @param stateClass state class
     * @param eventClass event class
     * @param initialState initial state (can be null but a NullPointerException 
     * will be thrown if StateMachine.forceSetState(S) is called any further)
     */
    public StateMachineWithScheduler(Class<S> stateClass,Class<E> eventClass,S initialState){
        //creates the transition model
        transitionModel=Fettle.newTransitionModel(stateClass,eventClass);
        /**
         * creates the state machine used internally, based on Fettle API. If null is passed, 
         * any further call of StateMachine.forceSetState(S) will throw a NullPointerException
         */
        internalStateMachine=transitionModel.newStateMachine(initialState);
        //creates the scheduler
        scheduler=new Scheduler<>();
    }
    
    /**
     * Adds a state and its actions triggered on transitions into the state machine
     * @param state added state
     * @param entryAction action used when the state machine enter the supplied state (can be null)
     * @param exitAction action used when the state machine exits the supplied state (can be null)
     */
    protected void addState(S state,Action<S,E> entryAction,Action<S,E> exitAction){
        //adds an entry action to the transition model
        if(entryAction!=null)
            transitionModel.addEntryAction(state,entryAction);
        //adds an exit action to the transition model
        if(exitAction!=null)
            transitionModel.addExitAction(state,exitAction);
    }
    
    /**
     * Updates the logical layer by running the scheduler
     * 
     * @param timer general timer
     */
    public void updateLogicalLayer(final ReadOnlyTimer timer){
        final S currentStateBeforeUpdate=internalStateMachine.getCurrentState();
        scheduler.update(previousState,currentStateBeforeUpdate,timer.getTimePerFrame());
        //obviously the current state may change during the update
        final S currentStateAfterUpdate=internalStateMachine.getCurrentState();
        previousState=currentStateAfterUpdate;
    }
    
    /**
     * Fires an event that may cause a transition
     * 
     * @param event
     */
    public void fireEvent(E event){
    	internalStateMachine.fireEvent(event);
    }
    
    /**
     * Fires an event that may cause a transition and supplies some transitional arguments
     * 
     * @param event event that may cause a transition
     * @param args arguments used in the transition if any
     */
    public void fireEvent(E event,Arguments args){
    	internalStateMachine.fireEvent(event,args);
    }
}
