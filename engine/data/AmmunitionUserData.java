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
package engine.data;

import engine.weaponry.Ammunition;

public final class AmmunitionUserData extends CollectibleUserData{
	
	
	private final Ammunition ammunition;
	
	private final int ammunitionCount;
	
	public AmmunitionUserData(final String sourcename,final Ammunition ammunition,final int ammunitionCount){
		super(sourcename,ammunition.getLabel());
		this.ammunition=ammunition;
		this.ammunitionCount=ammunitionCount;
	}
	
	public final Ammunition getAmmunition(){
		return(ammunition);
	}
	
	public final int getAmmunitionCount(){
		return(ammunitionCount);
	}
}