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
import se.hiflyer.fettle.StateMachine;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.GameTaskQueueManager;

/**
 * Trigger action causing a transition in the state machine
 * @author Julien Gouesse
 *
 */
public class TransitionTriggerAction<S,E> implements TriggerAction, Runnable{

    protected final StateMachine<S,E> stateMachine;
    
    protected final E transitionEvent;
    
    protected final RenderContext renderContext;
    
    public TransitionTriggerAction(StateMachine<S,E> stateMachine, 
            E transitionEvent, RenderContext renderContext) {
        this.stateMachine=stateMachine;
        this.transitionEvent=transitionEvent;
        this.renderContext=renderContext;
    }

    @Override
    public void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
        run();
    }
    
    @Override
    public void run() {
        //this operation must be done on the update queue
        GameTaskQueueManager.getManager(renderContext).update(new Callable<Void>(){
            @Override
            public Void call() throws Exception{
                //fires the transition event in the state machine to cause the transition
                stateMachine.fireEvent(transitionEvent);
                return(null);
            }
        });
    }
}