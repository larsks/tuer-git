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

import engine.abstraction.AbstractFactory;

public final class AmmunitionFactory extends AbstractFactory<Ammunition>{
	
	public AmmunitionFactory(){
	}	
	
	public final boolean addNewAmmunition(final String pickingUpSoundSamplePath,final String identifier,final String label){
		boolean success=identifier!=null&&!componentMap.containsKey(identifier);
		if(success)
			{final Ammunition ammunition=new Ammunition(pickingUpSoundSamplePath,identifier,label);
			 success=add(identifier,ammunition);
			}
		return(success);
	}
}