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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Tool able to handle scheduled tasks by detecting state changes and executing these tasks by 
 * taking into account their time offsets.
 * 
 * @author Julien Gouesse
 *
 */
public class Scheduler{

    /**scheduled tasks*/
    private final HashMap<StateChangeScheduledTask,Integer> scheduledTasks;
    /**queued tasks: tasks that are going to be executed once soon*/
    private final HashMap<StateChangeScheduledTask,Double> queuedTasks;

    public Scheduler(){
        scheduledTasks=new HashMap<StateChangeScheduledTask,Integer>();
        queuedTasks=new HashMap<StateChangeScheduledTask,Double>();
    }
    
    public void addScheduledTask(StateChangeScheduledTask scheduledTask){
        final Integer initialRemainingExecutionCount=Integer.valueOf(scheduledTask.getExecutionCount());
        scheduledTasks.put(scheduledTask,initialRemainingExecutionCount);
    }

    public void update(final State previousState,final State currentState,final double timePerFrame){
        final ArrayList<StateChangeScheduledTask> executedTasks=new ArrayList<StateChangeScheduledTask>();
        final ArrayList<StateChangeScheduledTask> postponedTasks=new ArrayList<StateChangeScheduledTask>();
        final ArrayList<StateChangeScheduledTask> unqueuedTasks=new ArrayList<StateChangeScheduledTask>();
        if(previousState!=currentState)
            {if(previousState!=null)
                 {//looks for a scheduled task waiting for the exit of this state
                  for(StateChangeScheduledTask scheduledTask:scheduledTasks.keySet())
                      if(scheduledTask.getStateChangeType().equals(StateChangeType.EXIT))
                          {final State state=scheduledTask.getState();
                           if(state.equals(previousState))
                               if(scheduledTask.getTimeOffsetInSeconds()==0)
                                   {//runs it now
                                    scheduledTask.getRunnable().run();
                                    executedTasks.add(scheduledTask);
                                   }
                               else
                                   {//runs it later
                                    postponedTasks.add(scheduledTask);
                                   }
                          }                      
                 }
             if(currentState!=null)
                 {//looks for a scheduled task waiting for the entry of this state
                  for(StateChangeScheduledTask scheduledTask:scheduledTasks.keySet())
                      if(scheduledTask.getStateChangeType().equals(StateChangeType.ENTRY))
                          {final State state=scheduledTask.getState();
                           if(state.equals(currentState))
                               if(scheduledTask.getTimeOffsetInSeconds()==0)
                                   {//runs it now
                                    scheduledTask.getRunnable().run();
                                    executedTasks.add(scheduledTask);
                                   }
                               else
                                   {//runs it later
                                    postponedTasks.add(scheduledTask);                                    
                                   }
                          }
                 }
            }
        //removes tasks that cannot be queued anymore because of a state change
        for(StateChangeScheduledTask queuedTask:queuedTasks.keySet())
            {final State state=queuedTask.getState();
             switch(queuedTask.getStateChangeType()){
                 case ENTRY:
                     //if we have already switched to another state
                     if(!state.equals(currentState))
                         unqueuedTasks.add(queuedTask);
                     break;
                 case EXIT:
                     if(state.equals(currentState))
                         unqueuedTasks.add(queuedTask);
                     break;
             }
            }
        for(StateChangeScheduledTask unqueuedTask:unqueuedTasks)
            queuedTasks.remove(unqueuedTask);
        //tries to run tasks whose executions have been postponed
        final HashMap<StateChangeScheduledTask,Double> updatedQueuedTasks=new HashMap<StateChangeScheduledTask,Double>();
        for(Entry<StateChangeScheduledTask,Double> queuedEntry:queuedTasks.entrySet())
            {StateChangeScheduledTask queuedTask=queuedEntry.getKey();
             final double previousRemainingTime=queuedEntry.getValue().doubleValue();
             final double currentRemainingTime=previousRemainingTime-timePerFrame;
             if(currentRemainingTime<=0)
                 {//runs it now
                  queuedTask.getRunnable().run();
                  executedTasks.add(queuedTask);
                 }
             else
                 {//runs it later
                  updatedQueuedTasks.put(queuedTask,Double.valueOf(currentRemainingTime));
                 }
            }
        //updates the queued tasks here (to avoid performing concurrent modifications above)
        queuedTasks.putAll(updatedQueuedTasks);
        //updates the remaining execution counts of executed tasks if necessary or removes the task(s) from the scheduler
        for(StateChangeScheduledTask executedTask:executedTasks)
            {final int previousRemainingExecutionCount=scheduledTasks.get(executedTask).intValue();
             final int currentRemainingExecutionCount=previousRemainingExecutionCount-1;
             if(currentRemainingExecutionCount==0)
                 //removes the executed task as it will not be executed anymore
                 scheduledTasks.remove(executedTask);
             else
                 {//keeps this task and updates its remaining execution count
                  scheduledTasks.put(executedTask,Integer.valueOf(currentRemainingExecutionCount));
                 }
            }
        //queues postponed tasks here to avoid mixing them with already queued tasks
        for(StateChangeScheduledTask postponedTask:postponedTasks)
            queuedTasks.put(postponedTask,Double.valueOf(postponedTask.getTimeOffsetInSeconds()));        
    }
}
