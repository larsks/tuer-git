/**
 * Copyright (c) 2006-2014 Julien Gouesse
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

import java.util.List;
import engine.data.Objective;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;

/**
 * 
 * @author Julien Gouesse
 *
 */
public class PauseMenuStateEntryAction extends ScenegraphStateEntryAction{

	public static final String NO_PRESELECTED_MENU_ITEM = "(No Preselected Menu Item)";
	
	public static final String EXIT_CONFIRM_TAG = "(For Exit Confirm)";
	
	@Override
    public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args,StateMachine<ScenegraphState,String> stateMachine){
		final PauseMenuState pauseMenuState=(PauseMenuState)to;
		final ScenegraphTransitionTriggerActionArguments sttaArgs=(ScenegraphTransitionTriggerActionArguments)args;
		final int latestPlayedLevelIndex=sttaArgs.getPreviousLevelIndex();
		final int latestNextPlayableLevelIndex=sttaArgs.getNextLevelIndex();
		pauseMenuState.setLatestPlayedLevelIndex(latestPlayedLevelIndex);
		pauseMenuState.setLatestNextPlayableLevelIndex(latestNextPlayableLevelIndex);
		final boolean openedForExitConfirm=EXIT_CONFIRM_TAG.equals(sttaArgs.getTag());
		pauseMenuState.setOpenedForExitConfirm(openedForExitConfirm);
		final GameStatistics gameStats=sttaArgs.getGameStatistics();
		final List<Objective> objectives=sttaArgs.getObjectives();
		pauseMenuState.setGameStatistics(gameStats);
		pauseMenuState.setObjectives(objectives);
		super.onTransition(from,to,cause,args,stateMachine);
    }
}
