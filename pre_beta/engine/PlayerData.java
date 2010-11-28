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
import com.ardor3d.math.Matrix3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.CameraNode;
import engine.GameState.WeaponUserData;

final class PlayerData {
    
	
	private static final int maxHealth=100;
	
	private int health;
	
	private boolean invincible;
	
	private boolean dualWeaponUse;
	
	private boolean[] rightHandWeaponsAvailability;
	
	private boolean[] leftHandWeaponsAvailability;
	
	private transient Node[] rightHandWeaponsList;
	
	private transient Node[] leftHandWeaponsList;
	
	private Weapon.Identifier weaponIDInUse;
	
	private CameraNode cameraNode;
	
	
	PlayerData(CameraNode cameraNode){
		this.cameraNode=cameraNode;
		health=maxHealth;
		invincible=false;
		weaponIDInUse=null;
		dualWeaponUse=false;
		final int weaponCount=Weapon.Identifier.values().length;
		rightHandWeaponsAvailability=new boolean[weaponCount];
		leftHandWeaponsAvailability=new boolean[weaponCount];
		rightHandWeaponsList=new Node[weaponCount];
		leftHandWeaponsList=new Node[weaponCount];
		Arrays.fill(rightHandWeaponsAvailability,false);
		Arrays.fill(leftHandWeaponsAvailability,false);
		Arrays.fill(rightHandWeaponsList,null);
		Arrays.fill(leftHandWeaponsList,null);
	}
	
	
	boolean collect(Node collectible){
		//check if the collectible can be collected
		final boolean result;
		final Object userData=collectible.getUserData();
		if(userData!=null&&userData instanceof GameState.CollectibleUserData)
		    {if(userData instanceof GameState.WeaponUserData)
		         result=collectWeapon(collectible,(GameState.WeaponUserData)userData);
		     else
		    	 if(userData instanceof GameState.MedikitUserData)
		    		 result=collectMedikit(collectible,(GameState.MedikitUserData)userData);
		    	     //TODO: handle here the other kinds of collectible objects
		    	 else
		    		 result=false;
		    }
		else
			result=false;
		return(result);
	}
	
	private boolean collectWeapon(Node collectible,final GameState.WeaponUserData weaponUserData){
		final boolean result;
		final int weaponIndex=weaponUserData.getId().ordinal();
	    if(!rightHandWeaponsAvailability[weaponIndex]||!leftHandWeaponsAvailability[weaponIndex])
            {if(!rightHandWeaponsAvailability[weaponIndex])
                 {rightHandWeaponsAvailability[weaponIndex]=true;
                  rightHandWeaponsList[weaponIndex]=collectible;
                  result=true;
                 }
             else
  	             //check if this weapon can have a dual use
  	             if(weaponUserData.isTwoHanded())
                     {leftHandWeaponsAvailability[weaponIndex]=true;
  	                  leftHandWeaponsList[weaponIndex]=collectible;
  	                  result=true;
                     }
  	             else
  		             result=false;
            }
        else
	        result=false;
		return(result);
	}
	
	private boolean collectMedikit(Node collectible,final GameState.MedikitUserData medikitUserData){
		final boolean result;
		result=increaseHealth(medikitUserData.getHealth())>0;
		return(result);
	}
	
	int decreaseHealth(int damage){
		int oldHealth=health;
		if(!invincible && damage>0 && health > 0)
			health=Math.max(0,health-damage);
		return(oldHealth-health);
	}
	
	/**
	 * increases the health
	 * @param amount the suggested increase of health
	 * @return the real increase of health
	 */
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
		dualWeaponUse=false;
		Arrays.fill(rightHandWeaponsAvailability,false);
		Arrays.fill(leftHandWeaponsAvailability,false);
		Arrays.fill(rightHandWeaponsList,null);
		Arrays.fill(leftHandWeaponsList,null);
	}
	
	void selectNextWeapon(){
		selectWeapon(true);
	}
	
    void selectPreviousWeapon(){
    	selectWeapon(false);
	}
    
    private void selectWeapon(boolean next){
    	final Weapon.Identifier oldWeaponIDInUse=weaponIDInUse;
    	final boolean oldDualWeaponUse=dualWeaponUse;
    	//if the player wants to use a single weapon instead of 2
    	if(!next&&dualWeaponUse)
    		dualWeaponUse=false;
    	else
    		//if the player wants to use 2 identical weapons instead of 1
    		if(next&&!dualWeaponUse&&weaponIDInUse!=null&&leftHandWeaponsAvailability[weaponIDInUse.ordinal()]&&rightHandWeaponsAvailability[weaponIDInUse.ordinal()])
    			dualWeaponUse=true;
    		else
    		    {final int weaponCount=Weapon.Identifier.values().length;
    	         int multiplier=next?1:-1;
    	         final int firstIndex=weaponIDInUse!=null?((weaponIDInUse.ordinal()+weaponCount)+multiplier)%weaponCount:0;
		         int currentIndex;
		         for(int i=0;i<weaponCount*2;i++)
		             {currentIndex=(firstIndex+((i/2)*multiplier)+weaponCount)%weaponCount;
		              if(i%2==0)
		                  {if(next)
		                       {if(rightHandWeaponsAvailability[currentIndex])
				                    {weaponIDInUse=Weapon.Identifier.values()[currentIndex];
				                     dualWeaponUse=false;
					                 break;
				                    }
		                       }
		                   else
		                       {if(leftHandWeaponsAvailability[currentIndex]&&rightHandWeaponsAvailability[currentIndex])
			                        {weaponIDInUse=Weapon.Identifier.values()[currentIndex];
			                         dualWeaponUse=true;
				                     break;
			                        }
		                       }
		                  }
		              else
		                  {if(next)
	                           {if(leftHandWeaponsAvailability[currentIndex]&&rightHandWeaponsAvailability[currentIndex])
		                            {weaponIDInUse=Weapon.Identifier.values()[currentIndex];
		                             dualWeaponUse=true;
			                         break;
		                            }
	                           }
		                   else
		                       {if(rightHandWeaponsAvailability[currentIndex])
			                        {weaponIDInUse=Weapon.Identifier.values()[currentIndex];
			                         dualWeaponUse=false;
				                     break;
			                        }
		                       }
		                  }
		             }
    	        }
    	if(oldWeaponIDInUse!=weaponIDInUse||oldDualWeaponUse!=dualWeaponUse)
    	    {Node oldWeapon,newWeapon;
    		 if(oldWeaponIDInUse!=weaponIDInUse)
    	         {//if at least one weapon was used previously
    	    	  if(oldWeaponIDInUse!=null)
	                  {//drop the right hand weapon
    	    		   oldWeapon=rightHandWeaponsList[oldWeaponIDInUse.ordinal()];
	                   oldWeapon.clearControllers();
	                   cameraNode.detachChild(oldWeapon);
	                   if(oldDualWeaponUse)
	    	    	      {//drop the left hand weapon
	                	   oldWeapon=leftHandWeaponsList[oldWeaponIDInUse.ordinal()];
	    	               oldWeapon.clearControllers();
	    	               cameraNode.detachChild(oldWeapon);
	    	    	      }
	                  }
    	    	  //add the right hand weapon
    	    	  newWeapon=rightHandWeaponsList[weaponIDInUse.ordinal()];
    	    	  initializeWeaponLocalTransform(newWeapon,true);
		          cameraNode.attachChild(newWeapon);
    	    	  if(dualWeaponUse)
    	    	      {//add the left hand weapon
    	    		   newWeapon=leftHandWeaponsList[weaponIDInUse.ordinal()];
    	    		   initializeWeaponLocalTransform(newWeapon,false);
   		               cameraNode.attachChild(newWeapon);
    	    	      }
    	         }
    	     else
    	    	 //only the dual use has changed
    	         {if(dualWeaponUse)
    	              {//add the left hand weapon
    	        	   newWeapon=leftHandWeaponsList[weaponIDInUse.ordinal()];
    	        	   initializeWeaponLocalTransform(newWeapon,false);
    		           cameraNode.attachChild(newWeapon);
    	              }
    	          else
    	        	  if(oldWeaponIDInUse!=null)
    	                  {//drop the left hand weapon
    	        	       oldWeapon=leftHandWeaponsList[oldWeaponIDInUse.ordinal()];
    	                   oldWeapon.clearControllers();
    	                   cameraNode.detachChild(oldWeapon);
    	                  }
    	         }
    	    }
	}
    
    private final void initializeWeaponLocalTransform(Node newWeapon,boolean rightHanded){
    	Matrix3 correctWeaponRotation=new Matrix3();
    	//FIXME: move this half rotation into the user data of the weapon
    	correctWeaponRotation.fromAngles(0, Math.PI, 0).multiplyLocal(((WeaponUserData)newWeapon.getUserData()).getRotation());
    	newWeapon.setRotation(correctWeaponRotation);
    	if(rightHanded)
    		newWeapon.setTranslation(-0.17870682064350812,-0.01787068206435081,0.35741364128701625);
    	else
    		newWeapon.setTranslation(0.17870682064350812,-0.01787068206435081,0.35741364128701625);
    }
}
