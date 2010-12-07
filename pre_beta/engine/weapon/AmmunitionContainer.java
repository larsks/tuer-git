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
package engine.weapon;

import java.util.Arrays;

public class AmmunitionContainer {

	private final int[] ammunitionsCount;
	
	private final int[] ammunitionsMaxCount;
	
	public AmmunitionContainer(){
		ammunitionsCount=new int[Ammunition.values().length];
		ammunitionsMaxCount=new int[Ammunition.values().length];
		Arrays.fill(ammunitionsMaxCount,1000);
	}
	
	public final void empty(){
		Arrays.fill(ammunitionsCount,0);
	}
	
	public final int get(final Ammunition ammunition){
		return(ammunitionsCount[ammunition.ordinal()]);
	}
	
	public final int add(final Ammunition ammunition,final int ammunitionCountToAdd){
		final int previousAmmunitionCount=ammunitionsCount[ammunition.ordinal()];
		if(ammunitionCountToAdd>0)
			ammunitionsCount[ammunition.ordinal()]=Math.min(ammunitionsMaxCount[ammunition.ordinal()],ammunitionsCount[ammunition.ordinal()]+ammunitionCountToAdd);
		return(ammunitionsCount[ammunition.ordinal()]-previousAmmunitionCount);
	}
	
	public final int remove(final Ammunition ammunition,final int ammunitionCountToRemove){
		final int previousAmmunitionCount=ammunitionsCount[ammunition.ordinal()];
		if(ammunitionCountToRemove>0)
			ammunitionsCount[ammunition.ordinal()]=Math.max(0,ammunitionsCount[ammunition.ordinal()]-ammunitionCountToRemove);
		return(previousAmmunitionCount-ammunitionsCount[ammunition.ordinal()]);
	}
}
