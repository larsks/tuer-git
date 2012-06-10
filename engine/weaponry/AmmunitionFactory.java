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

import java.util.HashMap;

public final class AmmunitionFactory {

	
    private final HashMap<String,Ammunition> ammunitionsMap;
	
	private final HashMap<Integer,Ammunition> ammunitionsIndicesMap;
	
	
	public AmmunitionFactory(){
		ammunitionsMap=new HashMap<String,Ammunition>();
		ammunitionsIndicesMap=new HashMap<Integer,Ammunition>();
	}	
	
	public final boolean addNewAmmunition(final String pickingUpSoundSamplePath,final String identifier,final String label){
		final boolean success=identifier!=null&&!ammunitionsMap.containsKey(identifier);
		if(success)
			{final Ammunition ammunition=new Ammunition(pickingUpSoundSamplePath,identifier,label);
			 ammunitionsMap.put(identifier,ammunition);
			 ammunitionsIndicesMap.put(ammunition.getUid(),ammunition);
			}
		return(success);
	}
	
	public final Ammunition getAmmunition(final int index){
		return(ammunitionsIndicesMap.get(Integer.valueOf(index)));
	}
	
	public final Ammunition getAmmunition(final String identifier){
		return(ammunitionsMap.get(identifier));
	}
	
	public final int getSize(){
		return(ammunitionsMap.size());
	}
}
