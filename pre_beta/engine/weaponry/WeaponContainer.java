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

import java.util.Arrays;

import com.ardor3d.scenegraph.Node;

public final class WeaponContainer{
	
	private final Node[] weaponsNodes;
	
	private final WeaponFactory weaponFactory;
	
    public WeaponContainer(final WeaponFactory weaponFactory){
    	this.weaponFactory=weaponFactory;
    	final int weaponCount=weaponFactory.getSize();
    	this.weaponsNodes=new Node[weaponCount];
    	Arrays.fill(weaponsNodes,null);
    }
    
    public final boolean add(final Node weaponNode,final Weapon weapon){
    	final boolean success;
    	if(weaponNode!=null)
    	    {final int index=weaponFactory.getId(weapon);
    	     if(weaponsNodes[index]==null)
    	         {weaponsNodes[index]=weaponNode;
    	    	  success=true;
    	         }
    	     else
    	    	 success=false;
    	    }
    	else
    		success=false;
    	return(success);
    }
    
    public final boolean isAvailable(final Weapon weapon){
    	final int weaponId=weaponFactory.getId(weapon);
    	return(weaponsNodes[weaponId]!=null);
    }
    
    public final Node getNode(final Weapon weapon){
    	final int weaponId=weaponFactory.getId(weapon);
    	return(weaponsNodes[weaponId]);
    }
    
    public final void empty(){
    	Arrays.fill(weaponsNodes,null);
    }
}
