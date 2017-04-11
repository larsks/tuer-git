/**
 * Copyright (c) 2006-2017 Julien Gouesse
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

/**
 * Task scheduled to be executed at least one time immediately or after an
 * optional delay when a condition gets satisfied
 * 
 * @author Julien Gouesse
 *
 */
public class ScheduledTask<S> {
    /** condition that triggers the execution of this task */
    protected final ScheduledTaskCondition<S> condition;
    /** number of times this task has to be executed */
    protected final int executionCount;
    /** operation run by this task */
    protected final Runnable runnable;
    /**
     * delay between the satisfaction of the condition and the execution of this
     * task
     */
    protected final double timeOffsetInSeconds;

    /**
     * 
     * @param timeOffsetInSeconds
     * @param runnable
     * @param executionCount
     * @param condition
     */
    public ScheduledTask(final ScheduledTaskCondition<S> condition, final int executionCount, final Runnable runnable,
            final double timeOffsetInSeconds) {
        if (condition == null)
            throw new IllegalArgumentException("The condition must not be null");
        if (executionCount < 0)
            throw new IllegalArgumentException("The execution count must be strictly positive");
        if (runnable == null)
            throw new IllegalArgumentException("The runnable must not be null");
        if (timeOffsetInSeconds < 0)
            throw new IllegalArgumentException("The time offset must be positive");
        this.condition = condition;
        this.executionCount = executionCount;
        this.runnable = runnable;
        this.timeOffsetInSeconds = timeOffsetInSeconds;
    }

    public int getExecutionCount() {
        return (executionCount);
    }

    public Runnable getRunnable() {
        return (runnable);
    }

    public double getTimeOffsetInSeconds() {
        return (timeOffsetInSeconds);
    }

    /**
     * Tells whether the condition is satisfied
     * 
     * @param previousState
     *            previous state of the machine
     * @param currentState
     *            current state of the machine
     * @return <code>true</code> if the condition is satisfied, otherwise
     *         <code>false</code>
     */
    public boolean isConditionSatisfied(S previousState, S currentState) {
        return (condition.isSatisfied(previousState, currentState));
    }
}
