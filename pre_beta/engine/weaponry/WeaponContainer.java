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

public final class WeaponContainer {
	
	private boolean[] weaponsAvailabilities;
	
	private Node[] weaponsNodes;
	
	private final WeaponFactory weaponFactory;
	
	
    public WeaponContainer(final WeaponFactory weaponFactory){
    	this.weaponFactory=weaponFactory;
    	final int weaponCount=weaponFactory.getWeaponCount();
    	this.weaponsAvailabilities=new boolean[weaponCount];
    	this.weaponsNodes=new Node[weaponCount];
    	Arrays.fill(weaponsAvailabilities,false);
    	Arrays.fill(weaponsNodes,null);
    }
    
    private final void ensureWeaponCountChangeDetection(){
    	final int previousWeaponCount=weaponsAvailabilities.length;
    	final int currentWeaponCount=weaponFactory.getWeaponCount();
    	if(currentWeaponCount>previousWeaponCount)
    	    {weaponsAvailabilities=Arrays.copyOf(weaponsAvailabilities,currentWeaponCount);
    	     weaponsNodes=Arrays.copyOf(weaponsNodes,currentWeaponCount);
    	    }
    }
    
    public final boolean add(final Node weaponNode,final Weapon weapon){
    	final boolean success;
    	if(weaponNode!=null)
    	    {ensureWeaponCountChangeDetection();
    	     final int index=weapon.getUid();
    	     if(!weaponsAvailabilities[index])
    	         {weaponsAvailabilities[index]=true;
    	          weaponsNodes[index]=weaponNode;
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
    	ensureWeaponCountChangeDetection();
    	return(weaponsAvailabilities[weapon.getUid()]);
    }
    
    public final Node getNode(final Weapon weapon){
    	ensureWeaponCountChangeDetection();
    	return(weaponsNodes[weapon.getUid()]);
    }
    
    public final void empty(){
    	Arrays.fill(weaponsAvailabilities,false);
    	Arrays.fill(weaponsNodes,null);
    	ensureWeaponCountChangeDetection();
    }
}
