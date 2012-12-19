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
package engine.weaponry;

public class AmmunitionContainerContainer{
	
    private final AmmunitionContainer[] ammunitionContainers;
    
    private final AmmunitionFactory ammunitionFactory;
	
	public AmmunitionContainerContainer(final AmmunitionFactory ammunitionFactory){
		this.ammunitionFactory=ammunitionFactory;
		ammunitionContainers=new AmmunitionContainer[ammunitionFactory.getSize()];
		for(int i=0;i<ammunitionContainers.length;i++)
			//FIXME use the ammunition to set the maximum count of ammo
			ammunitionContainers[i]=new AmmunitionContainer(1000);
	}
	
	public final void empty(){
		for(AmmunitionContainer ammunitionContainer:ammunitionContainers)
			ammunitionContainer.empty();
	}
	
	public final int getMax(final Ammunition ammunition){
		final int ammunitionId=ammunitionFactory.getId(ammunition);
		return(ammunitionContainers[ammunitionId].getAmmunitionMaxCount());
	}
	
	public final int get(final Ammunition ammunition){
		final int ammunitionId=ammunitionFactory.getId(ammunition);
		return(ammunitionContainers[ammunitionId].getAmmunitionCount());
	}
	
	public final int add(final Ammunition ammunition,final int ammunitionCountToAdd){
		final int ammunitionId=ammunitionFactory.getId(ammunition);
		return(ammunitionContainers[ammunitionId].add(ammunitionCountToAdd));
	}
	
	public final int remove(final Ammunition ammunition,final int ammunitionCountToRemove){
		final int ammunitionId=ammunitionFactory.getId(ammunition);
		return(ammunitionContainers[ammunitionId].remove(ammunitionCountToRemove));
	}
}
