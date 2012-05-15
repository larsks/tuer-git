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
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Tool executing scheduled tasks by taking into account their conditions, their delays and their execution count. It postpones the 
 * execution of a task when it should not be executed immediately. This tool relies on a state machine that feeds it with its state 
 * changes and on a timer that provides the time elapsed since the last frame.
 * 
 * @author Julien Gouesse
 *
 */
public class Scheduler<S>{

    /**
     * Map associating scheduled tasks with their remaining execution counts (the number of time 
     * they have to be executed). All tasks manipulated by the scheduler are in this map
     */
    private final HashMap<ScheduledTask<S>,Integer> scheduledTasks;
    
    /**
     * Map associating queued tasks (tasks that are going to be executed after a delay, see 
     * {@link StateChangeScheduledTask#getTimeOffsetInSeconds()}) with their remaining execution 
     * times before their executions. Only the tasks that cannot be immediately run are in this 
     * map. They are removed from this map each time they are executed and they may be put into it
     * again if a transition justifies their use
     * */
    private final HashMap<ScheduledTask<S>,Double> queuedTasks;

    /**
     * Constructor
     */
    public Scheduler(){
        scheduledTasks=new HashMap<ScheduledTask<S>,Integer>();
        queuedTasks=new HashMap<ScheduledTask<S>,Double>();
    }
    
    /**
     * Adds a scheduled task into this scheduler
     * 
     * @param scheduledTask scheduled task
     */
    public void addScheduledTask(ScheduledTask<S> scheduledTask){
        final Integer initialRemainingExecutionCount=Integer.valueOf(scheduledTask.getExecutionCount());
        scheduledTasks.put(scheduledTask,initialRemainingExecutionCount);
    }
    
    public void removeScheduledTask(ScheduledTask<S> scheduledTask){
    	scheduledTasks.remove(scheduledTask);
    }

    /**
     * Updates this scheduler by using the supplied states and the elapsed time since the last frame
     * 
     * @param previousState previous state of a state machine
     * @param currentState current state of a state machine
     * @param timePerFrame elapsed time since the last frame
     */
    public void update(final S previousState,final S currentState,final double timePerFrame){
        final ArrayList<ScheduledTask<S>> executedTasks=new ArrayList<ScheduledTask<S>>();
        final ArrayList<ScheduledTask<S>> postponedTasks=new ArrayList<ScheduledTask<S>>();
        //checks all scheduled tasks
        for(ScheduledTask<S> scheduledTask:scheduledTasks.keySet())
        	//if its condition is satisfied
        	if(scheduledTask.isConditionSatisfied(previousState,currentState))
        	    {//if it can be run immediately
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
        //tries to run tasks whose executions have been postponed
        final Iterator<Entry<ScheduledTask<S>, Double>> queuedEntriesIterator=queuedTasks.entrySet().iterator();
        while(queuedEntriesIterator.hasNext())
            {Entry<ScheduledTask<S>,Double> queuedEntry=queuedEntriesIterator.next();
             //gets a task
       	     ScheduledTask<S> queuedTask=queuedEntry.getKey();
             //gets the previous remaining time before triggering its execution
       	     final double previousRemainingTime=queuedEntry.getValue().doubleValue();
             //computes its new remaining time
       	     final double currentRemainingTime=previousRemainingTime-timePerFrame;
       	     //if there is no remaining time
             if(currentRemainingTime<=0)
                 {//runs it now
                  queuedTask.getRunnable().run();
                  executedTasks.add(queuedTask);
                  //removes it from the queued tasks as it does not need to be queued anymore
                  queuedEntriesIterator.remove();
                 }
             else
                 {//runs it later, keeps it in the queued tasks, updates its remaining time
                  queuedEntry.setValue(Double.valueOf(currentRemainingTime));
                 }
            }
        //updates the remaining execution counts of executed tasks if necessary or removes the task(s) from the scheduler
        for(ScheduledTask<S> executedTask:executedTasks)
            {//gets the previous remaining execution count
        	 final int previousRemainingExecutionCount=scheduledTasks.get(executedTask).intValue();
             //decrements the remaining execution count as this task has just been run earlier
             final int currentRemainingExecutionCount=previousRemainingExecutionCount-1;
             if(currentRemainingExecutionCount==0)
                 {//removes the executed task as it will not be executed anymore
                  scheduledTasks.remove(executedTask);
                  //removes it as a queued task too (it has no effect if the task was not queued)
                  queuedTasks.remove(executedTask);
                 }
             else
                 {//keeps this task and updates its remaining execution count
                  scheduledTasks.put(executedTask,Integer.valueOf(currentRemainingExecutionCount));
                 }
            }
        //queues postponed tasks here to avoid mixing them with already queued tasks
        for(ScheduledTask<S> postponedTask:postponedTasks)
            queuedTasks.put(postponedTask,Double.valueOf(postponedTask.getTimeOffsetInSeconds()));        
    }
}
