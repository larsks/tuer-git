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
import se.hiflyer.fettle.StateMachine;

/**
 * 
 * @author Julien Gouesse
 *
 */
public class PauseMenuStateEntryAction extends ScenegraphStateEntryAction{

	public static final String EXIT_CONFIRM_TAG = "(For Exit Confirm)"; 
	
	@Override
    public void onTransition(ScenegraphState from,ScenegraphState to,String cause,Arguments args,StateMachine<ScenegraphState,String> stateMachine){
		final PauseMenuState pauseMenuState=(PauseMenuState)to;
		final int latestPlayedLevelIndex=((int[])args.getFirst())[0];
		final int latestNextPlayableLevelIndex=((int[])args.getFirst())[1];
		pauseMenuState.setLatestPlayedLevelIndex(latestPlayedLevelIndex);
		pauseMenuState.setLatestNextPlayableLevelIndex(latestNextPlayableLevelIndex);
		final boolean openedForExitConfirm=args!=null&&args.getNumberOfArguments()>=2&&args.getArgument(1)!=null&&args.getArgument(1) instanceof String&&(((String)args.getArgument(1)).equals(EXIT_CONFIRM_TAG));
		pauseMenuState.setOpenedForExitConfirm(openedForExitConfirm);
		super.onTransition(from,to,cause,args,stateMachine);
    }
}
