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

import java.util.Map.Entry;

import engine.data.PlayerData;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.Condition;

public class SelectionPossibleCondition implements Condition{

	private final PlayerData playerData;
	
	private final boolean next;
	
	public SelectionPossibleCondition(final PlayerData playerData,final boolean next){
		this.next=next;
		this.playerData=playerData;
	}
	
	@Override
    public boolean isSatisfied(Arguments args){
		Entry<Integer,Boolean> selectableWeaponIndexAndDualHandEnabledFlag=playerData.getSelectableWeaponIndexAndDualHandEnabledFlag(next);
		return(selectableWeaponIndexAndDualHandEnabledFlag!=null);
	}
}
