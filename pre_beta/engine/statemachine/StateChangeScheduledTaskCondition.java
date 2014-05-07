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

public class StateChangeScheduledTaskCondition<S> extends ScheduledTaskCondition<S>{

	private final S state;

    private final StateChangeType stateChangeType;
	
	public StateChangeScheduledTaskCondition(final S state,final StateChangeType stateChangeType){
		super();
		this.state=state;
        this.stateChangeType=stateChangeType;
	}

	@Override
	public boolean isSatisfied(final S previousState,final S currentState){
		return(previousState!=currentState&&
				((stateChangeType.equals(StateChangeType.ENTRY)&&state.equals(currentState))||
				 (stateChangeType.equals(StateChangeType.EXIT)&&state.equals(previousState))));
	}	
}
