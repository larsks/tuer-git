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

import engine.data.PlayerData;
import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;


public class ReloadAction implements Action<PlayerState,PlayerTransition>{

    private final PlayerData playerData;

    public ReloadAction(final PlayerData playerData){
        this.playerData=playerData;
    }

    @Override
    public void onTransition(PlayerState from,PlayerState to,PlayerTransition event,Arguments args,StateMachine<PlayerState,PlayerTransition> stateMachine){
        playerData.reload();
    }
}
