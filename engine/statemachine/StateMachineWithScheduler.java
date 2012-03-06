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

import com.ardor3d.util.ReadOnlyTimer;
import se.hiflyer.fettle.Action;
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
    private S previousState;
    
    public StateMachineWithScheduler(Class<S> stateClass,Class<E> eventClass){
        //creates the transition model
        transitionModel=Fettle.newTransitionModel(stateClass,eventClass);
        //creates the state machine used internally, based on Fettle API
        internalStateMachine=transitionModel.newStateMachine(null);
        //creates the scheduler
        scheduler=new Scheduler<S>();
    }
    
    protected void addState(S state,Action<S,E> entryAction,Action<S,E> exitAction){
        //adds an entry action to the transition model
        if(entryAction!=null)
            transitionModel.addEntryAction(state,entryAction);
        //adds an exit action to the transition model
        if(exitAction!=null)
            transitionModel.addExitAction(state,exitAction);
    }
    
    public void updateLogicalLayer(final ReadOnlyTimer timer){
        final S currentState=internalStateMachine.getCurrentState();
        scheduler.update(previousState,currentState,timer.getTimePerFrame());
        previousState=currentState;
    }  
}
