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

import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;

/**
 * 
 * 
 * @author Julien Gouesse
 *
 */
public class UnloadingDisplayStateEntryAction extends ScenegraphStateEntryAction {

    private final Scheduler<ScenegraphState> scheduler;

    private final NoPendingTaskCondition noPendingTaskCondition;

    private final TransitionTriggerAction<ScenegraphState, String> toExitGameTriggerAction;

    private final TransitionTriggerAction<ScenegraphState, String> toMainMenuTriggerAction;

    private final TransitionTriggerAction<ScenegraphState, String> toLoadingDisplayTriggerAction;

    public static final String EXIT_TAG = "EXIT";

    public static final String MAIN_MENU_TAG = "MAIN_MENU";

    public static final String LEVEL_TAG = "LEVEL";

    public UnloadingDisplayStateEntryAction(final Scheduler<ScenegraphState> scheduler,
            final NoPendingTaskCondition noPendingTaskCondition,
            final TransitionTriggerAction<ScenegraphState, String> toExitGameTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toMainMenuTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toLoadingDisplayTriggerAction) {
        this.scheduler = scheduler;
        this.noPendingTaskCondition = noPendingTaskCondition;
        this.toExitGameTriggerAction = toExitGameTriggerAction;
        this.toMainMenuTriggerAction = toMainMenuTriggerAction;
        this.toLoadingDisplayTriggerAction = toLoadingDisplayTriggerAction;
    }

    @Override
    public void onTransition(ScenegraphState from, ScenegraphState to, String cause, Arguments args,
            StateMachine<ScenegraphState, String> stateMachine) {
        super.onTransition(from, to, cause, args, stateMachine);
        /**
         * adds a (one shot) scheduled task that exits this state when there is
         * no pending task. The arguments are used to determine the destination.
         * The pending tasks are used to cleanup the game state
         */
        if (args != null && args instanceof ScenegraphTransitionTriggerActionArguments) {
            final ScenegraphTransitionTriggerActionArguments sttaArgs = (ScenegraphTransitionTriggerActionArguments) args;
            final String destinationTag = sttaArgs.getTag();
            if (destinationTag != null) {
                if (destinationTag.equals(EXIT_TAG))
                    scheduler.addScheduledTask(
                            new ScheduledTask<>(noPendingTaskCondition, 1, toExitGameTriggerAction, 0));
                else if (destinationTag.equals(MAIN_MENU_TAG))
                    scheduler.addScheduledTask(
                            new ScheduledTask<>(noPendingTaskCondition, 1, toMainMenuTriggerAction, 0));
                else if (destinationTag.equals(LEVEL_TAG)) {
                    final String levelIdentifier = sttaArgs.getNextLevelIdentifier();
                    // uses an argument to pass the level index
                    toLoadingDisplayTriggerAction.arguments.setNextLevelIdentifier(levelIdentifier);
                    scheduler.addScheduledTask(
                            new ScheduledTask<>(noPendingTaskCondition, 1, toLoadingDisplayTriggerAction, 0));
                }
            }
        }
    }
}
