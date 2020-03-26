/**
 * Copyright (c) 2006-2020 Julien Gouesse
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Tool executing scheduled tasks by taking into account their conditions, their
 * delays and their execution count. It postpones the execution of a task when
 * it should not be executed immediately. This tool relies on a state machine
 * that feeds it with its state changes and on a timer that provides the time
 * elapsed since the last frame.
 * 
 * @author Julien Gouesse
 *
 */
public class Scheduler<S> {

    /**
     * Map associating scheduled tasks with their remaining execution counts
     * (the number of time they have to be executed). All tasks manipulated by
     * the scheduler are in this map
     */
    private final HashMap<ScheduledTask<S>, Integer> scheduledTasks;

    /**
     * Map associating queued tasks (tasks that are going to be executed after a
     * delay, see {@link StateChangeScheduledTask#getTimeOffsetInSeconds()})
     * with their remaining execution times before their executions. Only the
     * tasks that cannot be immediately run are in this map. They are removed
     * from this map each time they are executed and they may be put into it
     * again if a transition justifies their use
     */
    private final HashMap<ScheduledTask<S>, Double> queuedTasks;

    /**
     * list of tasks that are going to be added into the map of scheduled tasks
     * (used to avoid concurrent modification)
     */
    private final ArrayList<ScheduledTask<S>> unschedulableTasks;

    /**
     * list of tasks that are going to be removed from the map of scheduled
     * tasks (used to avoid concurrent modification)
     */
    private final ArrayList<ScheduledTask<S>> schedulableTasks;

    /**
     * Constructor
     */
    public Scheduler() {
        scheduledTasks = new HashMap<>();
        queuedTasks = new HashMap<>();
        unschedulableTasks = new ArrayList<>();
        schedulableTasks = new ArrayList<>();
    }

    /**
     * Adds a scheduled task into this scheduler
     * 
     * @param scheduledTask
     *            scheduled task
     */
    public void addScheduledTask(ScheduledTask<S> scheduledTask) {
        schedulableTasks.add(scheduledTask);
    }

    public void removeScheduledTask(ScheduledTask<S> scheduledTask) {
        unschedulableTasks.add(scheduledTask);
    }

    /**
     * Updates this scheduler by using the supplied states and the elapsed time
     * since the last frame
     * 
     * @param previousState
     *            previous state of a state machine
     * @param currentState
     *            current state of a state machine
     * @param timePerFrame
     *            elapsed time since the last frame
     */
    public void update(final S previousState, final S currentState, final double timePerFrame) {
        // adds all tasks that should be scheduled
        for (ScheduledTask<S> schedulableTask : schedulableTasks) {
            final Integer initialRemainingExecutionCount = Integer.valueOf(schedulableTask.getExecutionCount());
            scheduledTasks.put(schedulableTask, initialRemainingExecutionCount);
        }
        schedulableTasks.clear();
        // removes all tasks that should not be scheduled anymore
        for (ScheduledTask<S> unschedulableTask : unschedulableTasks) {
            scheduledTasks.remove(unschedulableTask);
            // removes it as a queued task too (it has no effect if the task was
            // not queued)
            queuedTasks.remove(unschedulableTask);
        }
        unschedulableTasks.clear();
        final ArrayList<ScheduledTask<S>> executedTasks = new ArrayList<>();
        // checks all scheduled tasks
        for (ScheduledTask<S> scheduledTask : scheduledTasks.keySet())
            // if it is not yet in the queue and if its condition is satisfied
            if (!queuedTasks.containsKey(scheduledTask)
                    && scheduledTask.isConditionSatisfied(previousState, currentState)) {// if
                                                                                         // it
                                                                                         // can
                                                                                         // be
                                                                                         // run
                                                                                         // immediately
                if (scheduledTask.getTimeOffsetInSeconds() == 0) {// runs it now
                    scheduledTask.getRunnable().run();
                    executedTasks.add(scheduledTask);
                } else {// runs it later
                    queuedTasks.put(scheduledTask, Double.valueOf(scheduledTask.getTimeOffsetInSeconds()));
                }
            }
        // tries to run tasks whose executions have been postponed
        final Iterator<Entry<ScheduledTask<S>, Double>> queuedEntriesIterator = queuedTasks.entrySet().iterator();
        while (queuedEntriesIterator.hasNext()) {
            Entry<ScheduledTask<S>, Double> queuedEntry = queuedEntriesIterator.next();
            // gets a task
            final ScheduledTask<S> queuedTask = queuedEntry.getKey();
            // gets the previous remaining time before triggering its execution
            final double previousRemainingTime = queuedEntry.getValue().doubleValue();
            // computes its new remaining time
            final double currentRemainingTime = previousRemainingTime - timePerFrame;
            // if there is no remaining time
            if (currentRemainingTime <= 0) {// runs it now
                queuedTask.getRunnable().run();
                executedTasks.add(queuedTask);
                // removes it from the queued tasks as it does not need to be
                // queued anymore
                queuedEntriesIterator.remove();
                // FIXME if currentRemainingTime is negative, the next execution
                // should be done earlier
            } else {// runs it later, keeps it in the queued tasks, updates its
                    // remaining time
                queuedEntry.setValue(Double.valueOf(currentRemainingTime));
            }
        }
        // updates the remaining execution counts of executed tasks if necessary
        // or removes the task(s) from the scheduler
        for (ScheduledTask<S> executedTask : executedTasks) {// gets the
                                                             // previous
                                                             // remaining
                                                             // execution count
            final int previousRemainingExecutionCount = scheduledTasks.get(executedTask).intValue();
            // decrements the remaining execution count as this task has just
            // been run earlier
            final int currentRemainingExecutionCount = previousRemainingExecutionCount - 1;
            if (currentRemainingExecutionCount == 0) {// removes the executed
                                                      // task as it will not be
                                                      // executed anymore
                scheduledTasks.remove(executedTask);
                // removes it as a queued task too (it has no effect if the task
                // was not queued)
                queuedTasks.remove(executedTask);
            } else {// keeps this task and updates its remaining execution count
                scheduledTasks.put(executedTask, Integer.valueOf(currentRemainingExecutionCount));
            }
        }
    }
}
