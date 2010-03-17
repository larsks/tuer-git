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
package engine;

import java.util.Arrays;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

final class PlayerData {
    
	
	private static final int maxHealth=100;
	
	private int health;
	
	private boolean invincible;
	
	private boolean[] weaponsAvailability;
	
	private transient Node[] weaponsList;
	
	private Weapon.Identifier weaponIDInUse;
	
	private SpatialController<Spatial> weaponController;
	
	private Node parent;
	
	
	PlayerData(Node parent){
		this.parent=parent;
		health=maxHealth;
		invincible=false;
		weaponIDInUse=null;
		weaponsAvailability=new boolean[Weapon.Identifier.values().length];
		weaponsList=new Node[Weapon.Identifier.values().length];
		Arrays.fill(weaponsAvailability,false);
		Arrays.fill(weaponsList,null);
	}
	
	
	boolean collect(Node collectible){
		//check if the collectible can be collected
		final boolean result=weaponsAvailability[((Weapon.Identifier)collectible.getUserData()).ordinal()];
		if(result)
		    {if(collectible.getParent()!=null)
			     {
		    	  collectible.getParent().detachChild(collectible);
			     }
		     final int weaponIndex=((Weapon.Identifier)collectible.getUserData()).ordinal();
		     weaponsAvailability[weaponIndex]=true;
		     weaponsList[weaponIndex]=collectible;
		    }
		return(result);
	}
	
	int decreaseHealth(int damage){
		int oldHealth=health;
		if(!invincible && damage>0 && health > 0)
			health=Math.max(0,health-damage);
		return(oldHealth-health);
	}
	
	final int increaseHealth(int amount){
	    int oldHealth=health;
	    if(amount>0 && health<maxHealth)
	        health=Math.min(maxHealth,health+amount);
	    return(health-oldHealth);
	}
	
	boolean isAlive(){
	    return(this.health>0);
	}
	
	int getHealth(){
	    return(health);
	}
	
	void respawn(){
		health=maxHealth;
		weaponIDInUse=null;
		Arrays.fill(weaponsAvailability,false);
		Arrays.fill(weaponsList,null);
	}
	
	void selectNextWeapon(){
		selectWeapon(true);
	}
	
    void selectPreviousWeapon(){
    	selectWeapon(false);
	}
    
    private void selectWeapon(boolean next){
    	Weapon.Identifier oldWeaponIDInUse=weaponIDInUse;
    	final int weaponCount=Weapon.Identifier.values().length;
		final int firstIndex=weaponIDInUse!=null?(weaponIDInUse.ordinal()-1)%weaponCount:0;
		int currentIndex;
		int multiplier=next?1:-1;
		for(int i=0;i<weaponCount-1;i++)
		    {currentIndex=(firstIndex+(i*multiplier))%weaponCount;
			 if(weaponsAvailability[currentIndex])
			     {weaponIDInUse=Weapon.Identifier.values()[currentIndex];
				  break;
			     }
		    }
		if(oldWeaponIDInUse!=weaponIDInUse)
	        {if(oldWeaponIDInUse!=null)
	             {Node oldWeapon=weaponsList[oldWeaponIDInUse.ordinal()];
	              oldWeapon.clearControllers();
	              parent.detachChild(oldWeapon);
	             }
	         Node newWeapon=weaponsList[weaponIDInUse.ordinal()];
	         newWeapon.addController(weaponController);
	         parent.attachChild(newWeapon);
	        }
	}
}
