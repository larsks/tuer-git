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

import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;

/**
 * Entry action used for the game over state to get some parameters about the latest played level
 * 
 * @author Julien Gouesse
 *
 */
public class GameOverStateEntryAction extends ScenegraphStateEntryAction{

	public GameOverStateEntryAction(){
		super();
	}

	@Override
    public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args,StateMachine<ScenegraphState,String> stateMachine){
		final int latestPlayedLevelIndex=((int[])args.getFirst())[0];
		final int latestNextPlayableLevelIndex=((int[])args.getFirst())[1];
		final GameStatistics gameStats=((GameStatistics[])args.getArgument(1))[0];
		final GameOverState gameOverState=(GameOverState)to;
		gameOverState.setLatestPlayedLevelIndex(latestPlayedLevelIndex);
		gameOverState.setLatestNextPlayableLevelIndex(latestNextPlayableLevelIndex);
		gameOverState.setGameStatistics(gameStats);
		super.onTransition(from,to,cause,args,stateMachine);
	}
}