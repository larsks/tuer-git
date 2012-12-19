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

public class AmmunitionContainer{

	private int ammunitionCount;
	
	private int ammunitionMaxCount;
	
	public AmmunitionContainer(final int ammunitionMaxCount){
		this.ammunitionCount=0;
		this.ammunitionMaxCount=ammunitionMaxCount;
	}
	
	public final void empty(){
		this.ammunitionCount=0;
	}
	
	public final int getAmmunitionCount(){
		return(ammunitionCount);
	}
	
	public final int getAmmunitionMaxCount(){
		return(ammunitionMaxCount);
	}
	
	public final int add(final int ammunitionCountToAdd){
		final int previousAmmunitionCount=ammunitionCount;
		if(ammunitionCountToAdd>0)
			ammunitionCount=Math.min(ammunitionMaxCount,ammunitionCount+ammunitionCountToAdd);
		return(ammunitionCount-previousAmmunitionCount);
	}
	
	public final int remove(final int ammunitionCountToRemove){
		final int previousAmmunitionCount=ammunitionCount;
		if(ammunitionCountToRemove>0)
			ammunitionCount=Math.max(0,ammunitionCount-ammunitionCountToRemove);
		return(previousAmmunitionCount-ammunitionCount);
	}
}
