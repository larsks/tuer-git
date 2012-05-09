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

import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.Condition;

public abstract class ScheduledTaskCondition<S> implements Condition{

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSatisfied(Arguments args){
		final S previousState;
		final S currentState;
		if(args!=null&&args!=Arguments.NO_ARGS&&args.getNumberOfArguments()==2)
		    {previousState=(S)args.getFirst();
		     currentState=(S)args.getArgument(1);
		    }
		else
		    {previousState=null;
		     currentState=null;
		    }
		return isSatisfied(previousState,currentState);
	}

	public abstract boolean isSatisfied(final S previousState,final S currentState);
}
