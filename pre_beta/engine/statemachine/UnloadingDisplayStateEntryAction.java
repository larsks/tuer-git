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

import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;

/**
 * 
 * 
 * @author Julien Gouesse
 *
 */
public class UnloadingDisplayStateEntryAction extends ScenegraphStateEntryAction{
	
	private final Scheduler<ScenegraphState> scheduler;
	
	private final NoPendingTaskCondition noPendingTaskCondition;
	
	private final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction;
	
	public static final String EXIT_TAG = "EXIT";

	public UnloadingDisplayStateEntryAction(final Scheduler<ScenegraphState> scheduler,final NoPendingTaskCondition noPendingTaskCondition,
			final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction){
		this.scheduler=scheduler;
		this.noPendingTaskCondition=noPendingTaskCondition;
		this.toExitGameTriggerAction=toExitGameTriggerAction;
	}
	
	@Override
    public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args,StateMachine<ScenegraphState,String> stateMachine){
        super.onTransition(from,to,cause,args,stateMachine);
        //adds a (one shot) scheduled task that exits this state when there is no pending task. The arguments are used to determine the destination
        if(args!=null&&args.getFirst()!=null&&args.getFirst() instanceof String&&((String)args.getFirst()).equals(EXIT_TAG))
            scheduler.addScheduledTask(new ScheduledTask<ScenegraphState>(noPendingTaskCondition,1,toExitGameTriggerAction,0));
    }
}
