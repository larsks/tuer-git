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

public class AmmunitionContainerContainer {
	
    private AmmunitionContainer[] ammunitionContainers;
	
	private final AmmunitionFactory ammunitionFactory;
	
	public AmmunitionContainerContainer(final AmmunitionFactory ammunitionFactory){
		this.ammunitionFactory=ammunitionFactory;
		ammunitionContainers=new AmmunitionContainer[ammunitionFactory.getSize()];
		for(int i=0;i<ammunitionContainers.length;i++)
			ammunitionContainers[i]=new AmmunitionContainer(1000);
	}
	
	public final void empty(){
		for(AmmunitionContainer ammunitionContainer:ammunitionContainers)
			ammunitionContainer.empty();
		ensureAmmunitionCountChangeDetection();
	}
	
	private final void ensureAmmunitionCountChangeDetection(){
		final int previousAmmoCount=ammunitionContainers.length;
		final int currentAmmoCount=ammunitionFactory.getSize();
		//an ammunition cannot be removed from the factory
		if(currentAmmoCount>previousAmmoCount)
		    {ammunitionContainers=Arrays.copyOf(ammunitionContainers,currentAmmoCount);
		     for(int i=previousAmmoCount;i<ammunitionContainers.length;i++)
		    	 ammunitionContainers[i]=new AmmunitionContainer(1000);
		    }
	}
	
	public final int getMax(final Ammunition ammunition){
		ensureAmmunitionCountChangeDetection();
		return(ammunitionContainers[ammunition.getUid()].getAmmunitionMaxCount());
	}
	
	public final int get(final Ammunition ammunition){
		ensureAmmunitionCountChangeDetection();
		return(ammunitionContainers[ammunition.getUid()].getAmmunitionCount());
	}
	
	public final int add(final Ammunition ammunition,final int ammunitionCountToAdd){
		ensureAmmunitionCountChangeDetection();
		return(ammunitionContainers[ammunition.getUid()].add(ammunitionCountToAdd));
	}
	
	public final int remove(final Ammunition ammunition,final int ammunitionCountToRemove){
		ensureAmmunitionCountChangeDetection();		
		return(ammunitionContainers[ammunition.getUid()].remove(ammunitionCountToRemove));
	}
}
