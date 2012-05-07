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

import java.util.concurrent.Callable;

import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.GameTaskQueueManager;

/**
 * Trigger action firing an event to cause a transition in the state machine
 * 
 * @author Julien Gouesse
 *
 */
public class TransitionTriggerAction<S,E> implements TriggerAction, Runnable, Action<S,E>{

	/**state machine to which events are fired*/
    protected StateMachine<S,E> stateMachine;
    
    /**event fired in this state machine*/
    protected E event;
    
    /**render context if the event must be fired on the update queue, otherwise null*/
    protected final RenderContext renderContext;
    
    /**
     * Constructor
     * 
     * @param stateMachine state machine to which events are fired
     * @param event event fired in this state machine
     * @param renderContext render context if the event must be fired on the update queue, otherwise null
     */
    public TransitionTriggerAction(StateMachine<S,E> stateMachine,E event,
    		RenderContext renderContext) {
        this.stateMachine=stateMachine;
        this.event=event;
        this.renderContext=renderContext;
    }

    @Override
    public void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
        run();
    }
    
    @Override
    public void onTransition(S from,S to,E event,Arguments args,StateMachine<S,E> stateMachine){
    	run();
    }
    
    @Override
    public void run(){
    	if(event!=null&&stateMachine!=null)
    	    {if(renderContext!=null)
                 //this operation must be done on the update queue
                 GameTaskQueueManager.getManager(renderContext).update(new Callable<Void>(){
                 @Override
                     public Void call() throws Exception{
                         //fires the event in the state machine to cause the transition
                         stateMachine.fireEvent(event);
                         return(null);
                     }
                 });
    	     else
    		     stateMachine.fireEvent(event);
    	    }
    }
}