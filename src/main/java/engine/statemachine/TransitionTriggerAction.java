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
    protected final StateMachine<S,E> stateMachine;
    
    /**event fired in this state machine*/
    protected final E event;
    
    /**arguments used when the event is fired*/
    protected final ScenegraphTransitionTriggerActionArguments arguments;
    
    /**render context if the event must be fired on the update queue, otherwise null*/
    protected final RenderContext renderContext;
    
    /**
     * Constructor
     * 
     * @param stateMachine state machine to which events are fired (must not be null)
     * @param event event fired in this state machine (must not be null)
     * @param renderContext render context if the event must be fired on the update queue, otherwise null
     */
    public TransitionTriggerAction(StateMachine<S,E> stateMachine,E event,RenderContext renderContext){
        this(stateMachine,event,null,renderContext);
    }
    
    /**
     * Constructor
     * 
     * @param stateMachine state machine to which events are fired (must not be null)
     * @param event event fired in this state machine (must not be null)
     * @param arguments arguments used when the event is fired (can be null)
     * @param renderContext render context if the event must be fired on the update queue, otherwise null
     */
    public TransitionTriggerAction(StateMachine<S,E> stateMachine,E event,ScenegraphTransitionTriggerActionArguments arguments,RenderContext renderContext){
        this.stateMachine=stateMachine;
        this.event=event;
        this.arguments=arguments;
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
    
    protected void doFireEvent(){
    	//fires the event in the state machine to cause the transition
        stateMachine.fireEvent(event,arguments);
    }
    
    @Override
    public void run(){
    	if(renderContext!=null)
            //this operation must be done on the update queue
            GameTaskQueueManager.getManager(renderContext).update(new Callable<Void>(){
                @Override
                public Void call() throws Exception{
                	doFireEvent();
                    return(null);
                }
            });
    	else
    		doFireEvent();
    }
}