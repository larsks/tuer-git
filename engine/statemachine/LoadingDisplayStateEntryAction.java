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
 * Entry action that receives the level index and passes it to the game state
 * 
 * @author Julien Gouesse
 *
 */
public class LoadingDisplayStateEntryAction extends ScenegraphStateEntryAction{
	
	private final GameState gameState;

	public LoadingDisplayStateEntryAction(final GameState gameState){
		super();
		this.gameState=gameState;
	}
	
	@Override
    public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args,StateMachine<ScenegraphState,String> stateMachine){
		final ScenegraphTransitionTriggerActionArguments sttaArgs=(ScenegraphTransitionTriggerActionArguments)args;
		final int levelIndex=sttaArgs.getNextLevelIndex();
		gameState.setLevelIndex(levelIndex);
		super.onTransition(from,to,cause,args,stateMachine);
	}
}
