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

	private int[] ammunitionsCount;
	
	private int[] ammunitionsMaxCount;
	
	private final AmmunitionFactory ammunitionFactory;
	
	public AmmunitionContainer(final AmmunitionFactory ammunitionFactory){
		this.ammunitionFactory=ammunitionFactory;
		ammunitionsCount=new int[ammunitionFactory.getAmmunitionCount()];
		ammunitionsMaxCount=new int[ammunitionFactory.getAmmunitionCount()];
		Arrays.fill(ammunitionsMaxCount,1000);
	}
	
	public final void empty(){
		Arrays.fill(ammunitionsCount,0);
		ensureAmmunitionCountChangeDetection();
	}
	
	private final void ensureAmmunitionCountChangeDetection(){
		final int previousAmmoCount=ammunitionsCount.length;
		final int currentAmmoCount=ammunitionFactory.getAmmunitionCount();
		//an ammunition cannot be removed from the factory
		if(currentAmmoCount>previousAmmoCount)
		    {ammunitionsCount=Arrays.copyOf(ammunitionsCount,currentAmmoCount);
		     ammunitionsMaxCount=Arrays.copyOf(ammunitionsMaxCount,currentAmmoCount);
		    }
	}
	
	public final int get(final Ammunition ammunition){
		ensureAmmunitionCountChangeDetection();
		return(ammunitionsCount[ammunition.getUid()]);
	}
	
	public final int add(final Ammunition ammunition,final int ammunitionCountToAdd){
		ensureAmmunitionCountChangeDetection();
		final int previousAmmunitionCount=ammunitionsCount[ammunition.getUid()];
		if(ammunitionCountToAdd>0)
			ammunitionsCount[ammunition.getUid()]=Math.min(ammunitionsMaxCount[ammunition.getUid()],ammunitionsCount[ammunition.getUid()]+ammunitionCountToAdd);
		return(ammunitionsCount[ammunition.getUid()]-previousAmmunitionCount);
	}
	
	public final int remove(final Ammunition ammunition,final int ammunitionCountToRemove){
		ensureAmmunitionCountChangeDetection();
		final int previousAmmunitionCount=ammunitionsCount[ammunition.getUid()];
		if(ammunitionCountToRemove>0)
			ammunitionsCount[ammunition.getUid()]=Math.max(0,ammunitionsCount[ammunition.getUid()]-ammunitionCountToRemove);
		return(previousAmmunitionCount-ammunitionsCount[ammunition.getUid()]);
	}
}
