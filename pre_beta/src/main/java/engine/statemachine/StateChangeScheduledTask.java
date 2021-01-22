/**
 * Copyright (c) 2006-2021 Julien Gouesse
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
 * Scheduled task whose condition is on a state change. It is not focused on a
 * given transition but rather on the entry or the exit from a state.
 * 
 * @author Julien Gouesse
 *
 * @param <S>
 *            state class
 */
public class StateChangeScheduledTask<S> extends ScheduledTask<S> {

    /**
     * Constructor
     * 
     * @param executionCount
     *            condition that triggers the execution of this task
     * @param runnable
     *            operation run by this task
     * @param timeOffsetInSeconds
     *            delay between the satisfaction of the condition and the
     *            execution of this task
     * @param state
     *            listened state
     * @param stateChangeType
     *            listened change type
     */
    public StateChangeScheduledTask(final int executionCount, final Runnable runnable, final double timeOffsetInSeconds,
            final S state, final StateChangeType stateChangeType) {
        super(new StateChangeScheduledTaskCondition<>(state, stateChangeType), executionCount, runnable,
                timeOffsetInSeconds);
    }
}
