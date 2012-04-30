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

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import com.ardor3d.math.Matrix3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.util.ReadOnlyTimer;

import engine.statemachine.PlayerEvent;
import engine.statemachine.PlayerStateMachine;
import engine.weaponry.Ammunition;
import engine.weaponry.AmmunitionContainerContainer;
import engine.weaponry.AmmunitionFactory;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponContainer;
import engine.weaponry.WeaponFactory;

/**
 * 
 * 
 * @author Julien Gouesse
 * 
 * FIXME move all dependencies on the state machine into a separate class
 */
public final class PlayerData {
    
	/**maximum health*/
	private static final int maxHealth=100;
	/**automatic increment used to compute the identifiers*/
	private static final AtomicInteger autoIncrementalIndex=new AtomicInteger(0);
	/**unique identifier*/
	private final int uid;
	/**default invalid identifier used to mark objects with no owner*/
	public static final int NO_UID=-1;
	/**current health*/
	private int health;
	/**flag indicating whether the player can be damaged*/
	private boolean invincible;
	/**flag indicating whether the player is using 2 identical weapons simultaneously*/
	private boolean dualWeaponUseEnabled;
	/**flag indicating whether the player is right-handed*/
	private final boolean rightHanded;
	/**weapon container of the right hand (right-handed) or in the left hand (left-handed)*/
	private final WeaponContainer primaryHandWeaponContainer;
	/**weapon container of the left hand (right-handed) or in the right hand (left-handed)*/
	private final WeaponContainer secondaryHandWeaponContainer;
	/**weapon currently in use*/
	private Weapon weaponInUse;
	/**node representing the camera*/
	private CameraNode cameraNode;
	/**factory that creates weapons*/
	private final WeaponFactory weaponFactory;
	/**container of ammunition container*/
	private final AmmunitionContainerContainer ammoContainerContainer;
	//FIXME use a state machine
	private final PlayerStateMachine stateMachine;
	//TODO define a duration to select another weapon
	
	
	public PlayerData(final CameraNode cameraNode,final AmmunitionFactory ammunitionFactory,final WeaponFactory weaponFactory,final boolean rightHanded){
		this.uid=autoIncrementalIndex.getAndIncrement();
		this.cameraNode=cameraNode;
		this.weaponFactory=weaponFactory;
		health=maxHealth;
		invincible=false;
		weaponInUse=null;
		dualWeaponUseEnabled=false;
		primaryHandWeaponContainer=new WeaponContainer(weaponFactory);
		secondaryHandWeaponContainer=new WeaponContainer(weaponFactory);
		ammoContainerContainer=new AmmunitionContainerContainer(ammunitionFactory);
		stateMachine=new PlayerStateMachine(this);
		this.rightHanded=rightHanded;
	}
	
	/**
	 * collects a collectible object if possible
	 * @param collectible
	 * @return count of collected sub elements
	 */
	public int collect(final Node collectible){
		//check if the collectible can be collected
		final int result;
		final Object userData=collectible.getUserData();
		if(userData!=null&&userData instanceof CollectibleUserData)
		    {if(userData instanceof WeaponUserData)
		         result=collectWeapon(collectible,(WeaponUserData)userData);
		     else
		    	 if(userData instanceof MedikitUserData)
		    		 result=collectMedikit(collectible,(MedikitUserData)userData);
		    	 else
		    		 if(userData instanceof AmmunitionUserData)
		    			 result=collectAmmunition(collectible,(AmmunitionUserData)userData);
		    	         //TODO: handle here the other kinds of collectible objects
		    	     else
		    		     result=0;
		    }
		else
			result=0;
		return(result);
	}
	
	protected int collectWeapon(final Node collectible,final WeaponUserData weaponUserData){
		final boolean result;
		final Weapon weapon=weaponUserData.getWeapon();
		final int ownerUid=weaponUserData.getOwnerUid();
		final boolean digitalWatermarkEnabled=weaponUserData.isDigitalWatermarkEnabled();
	    final boolean canChangeOfOwner=ownerUid!=uid&&!digitalWatermarkEnabled;
		if((ownerUid==uid||canChangeOfOwner))
            {if(weaponUserData.isPrimary())
                 result=primaryHandWeaponContainer.add(collectible,weapon);
             else
                 //the weapon must be already in the primary container to be added into the second one
                 result=primaryHandWeaponContainer.isAvailable(weapon)&&secondaryHandWeaponContainer.add(collectible,weapon);
             if(result&&canChangeOfOwner)
            	 weaponUserData.setOwnerUid(uid);           	              	 
            }
        else
	        result=false;
		return(result?1:0);
	}
	
	protected int collectAmmunition(final Node collectible,final AmmunitionUserData ammoUserData){
		final int result;
		result=ammoContainerContainer.add(ammoUserData.getAmmunition(),ammoUserData.getAmmunitionCount());
		return(result);
	}
	
	protected int collectMedikit(Node collectible,final MedikitUserData medikitUserData){
		final int result;
		result=increaseHealth(medikitUserData.getHealth());
		return(result);
	}
	
	public int decreaseHealth(int damage){
		int oldHealth=health;
		if(!invincible && damage>0)
			health=Math.max(0,health-damage);
		return(oldHealth-health);
	}
	
	/**
	 * increases the health
	 * @param amount the suggested increase of health
	 * @return the real increase of health
	 */
	public int increaseHealth(int amount){
	    final int oldHealth=health;
	    if(amount>0)
	        health=Math.min(maxHealth,health+amount);
	    return(health-oldHealth);
	}
	
	public boolean isAlive(){
	    return(this.health>0);
	}
	
	public int getHealth(){
	    return(health);
	}
	
	public void die(){
		if(!isAlive())
		    {//TODO: change the owner uid (if there is no digital watermark) and detach them from the player
			 //TODO: empty the container of ammunition container (use WeaponUserData)
		    }
	}
	
	public void respawn(){
		health=maxHealth;
		weaponInUse=null;
		ammoContainerContainer.empty();
		dualWeaponUseEnabled=false;
		primaryHandWeaponContainer.empty();
		secondaryHandWeaponContainer.empty();
	}
	
	/**
     * Gets the amount of ammo that can be used during a reload of the weapon in the primary hand
     * 
     * @return amount of ammo that can be used during a reload of the weapon in the primary hand
     */
	public int getReloadableAmmoCountForPrimaryHandWeapon() {
	    final int reloadableAmmoCount;
	    if(weaponInUse!=null&&!weaponInUse.isForMelee())
	        {//gets the size of the magazine of the weapon currently in use
	         final int magazineSize=weaponInUse.getMagazineSize();
	         //gets the data about the weapon currently in use
             final WeaponUserData primaryHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
             //computes the remaining room for ammo in the magazine
             final int remainingRoomForAmmoInMagazineForPrimaryHandWeapon=magazineSize-primaryHandWeaponUserData.getAmmunitionCountInMagazine();
             //gets the amount of available ammo in the container
	         final int availableAmmoInContainerBeforeReload=getAmmunitionCountInContainer();
	         //computes the amount of available ammo in the container if the player reloads the weapon currently in use
	         final int availableAmmoInContainerAfterReload=Math.max(0,availableAmmoInContainerBeforeReload-remainingRoomForAmmoInMagazineForPrimaryHandWeapon);
	         //computes the amount of available ammo for a single reload
	         reloadableAmmoCount=availableAmmoInContainerBeforeReload-availableAmmoInContainerAfterReload;
	        }
	    else
	        reloadableAmmoCount=0;
	    return(reloadableAmmoCount);
	}
	
	/**
	 * Gets the amount of ammo that can be used during a reload of the weapon in the secondary hand
	 * 
	 * @return amount of ammo that can be used during a reload of the weapon in the secondary hand
	 */
	public int getReloadableAmmoCountForSecondaryHandWeapon() {
        final int reloadableAmmoCount;
        if(weaponInUse!=null&&!weaponInUse.isForMelee()&&dualWeaponUseEnabled)
            {//gets the size of the magazine of the weapon currently in use
             final int magazineSize=weaponInUse.getMagazineSize();
             //gets the data about the weapon currently in use
             final WeaponUserData secondaryHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
             //computes the remaining room for ammo in the magazine
             final int remainingRoomForAmmoInMagazineForSecondaryHandWeapon=magazineSize-secondaryHandWeaponUserData.getAmmunitionCountInMagazine();
             //gets the amount of available ammo in the container
             final int availableAmmoInContainerBeforeReload=getAmmunitionCountInContainer();
             //computes the amount of available ammo in the container if the player reloads the weapon currently in use
             final int availableAmmoInContainerAfterReload=Math.max(0,availableAmmoInContainerBeforeReload-remainingRoomForAmmoInMagazineForSecondaryHandWeapon);
             //computes the amount of available ammo for a single reload
             reloadableAmmoCount=availableAmmoInContainerBeforeReload-availableAmmoInContainerAfterReload;
            }
        else
            reloadableAmmoCount=0;
        return(reloadableAmmoCount);
    }
	
	public void tryReload(){
		stateMachine.fireEvent(PlayerEvent.RELOADING);
	}
	
	/**
	 * Performs a reload of weapon(s)
	 * 
	 * @return amount of ammo used during the reload
	 */
	public int reload(){
		int reloadedAmmoCount=0;
		if(weaponInUse!=null&&!weaponInUse.isForMelee())
		    {//gets the ammo type
		     final Ammunition ammo=weaponInUse.getAmmunition();
		     //gets the data about the weapon currently in use
			 final WeaponUserData primaryHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
			 //gets the amount of ammo that can really be used during a reload (depending on the container and the magazine)
			 final int reloadableAmmoCountForPrimaryHandWeapon=getReloadableAmmoCountForPrimaryHandWeapon();
			 //removes ammo from the container
			 final int availableAmmoForPrimaryHandWeaponReload=ammoContainerContainer.remove(ammo,reloadableAmmoCountForPrimaryHandWeapon);
			 //adds it into the magazine
			 primaryHandWeaponUserData.addAmmunitionIntoMagazine(availableAmmoForPrimaryHandWeaponReload);
			 //increases reloaded ammunition count
			 reloadedAmmoCount+=availableAmmoForPrimaryHandWeaponReload;
			 if(dualWeaponUseEnabled)
			     {final WeaponUserData secondaryHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
			      final int reloadableAmmoCountForSecondaryHandWeapon=getReloadableAmmoCountForSecondaryHandWeapon();
			      //removes ammo from the container
			      final int availableAmmoForSecondaryHandWeaponReload=ammoContainerContainer.remove(ammo,reloadableAmmoCountForSecondaryHandWeapon);
			      //adds it into the magazine
			      secondaryHandWeaponUserData.addAmmunitionIntoMagazine(availableAmmoForSecondaryHandWeaponReload);
			      //increases reloaded ammunition count
			      reloadedAmmoCount+=availableAmmoForSecondaryHandWeaponReload;
			     }
		    }		    		
		return(reloadedAmmoCount);
	}
	
	public int getAmmunitionCountInContainer(){
		final int ammunitionCountInContainer;
		if(weaponInUse!=null&&!weaponInUse.isForMelee())
		    ammunitionCountInContainer=ammoContainerContainer.get(weaponInUse.getAmmunition());
		else
			ammunitionCountInContainer=0;
		return(ammunitionCountInContainer);
	}
	
	public boolean isCurrentWeaponAmmunitionCountDisplayable(){
		return(weaponInUse!=null&&!weaponInUse.isForMelee());
	}
	
	public int getAmmunitionCountInSecondaryHandedWeapon(){
		final int ammunitionCountInLeftHandedWeapon;
		if(weaponInUse!=null&&!weaponInUse.isForMelee()&&dualWeaponUseEnabled)
		    {final WeaponUserData leftHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		     ammunitionCountInLeftHandedWeapon=leftHandWeaponUserData.getAmmunitionCountInMagazine();
		    }
		else
			ammunitionCountInLeftHandedWeapon=0;
		return(ammunitionCountInLeftHandedWeapon);
	}
	
	public int getAmmunitionCountInPrimaryHandedWeapon(){
		final int ammunitionCountInRightHandedWeapon;
		if(weaponInUse!=null&&!weaponInUse.isForMelee())
		    {final WeaponUserData rightHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		     ammunitionCountInRightHandedWeapon=rightHandWeaponUserData.getAmmunitionCountInMagazine();
		    }
		else
			ammunitionCountInRightHandedWeapon=0;
		return(ammunitionCountInRightHandedWeapon);
	}
	
	/**
	 * Launches an attack
	 * 
	 * @return consumed ammunition if the weapon is not a melee weapon, knock count otherwise
	 */
	public int attack(){
		int consumedAmmunitionOrKnockCount=0;
		if(weaponInUse!=null)
		    {if(weaponInUse.isForMelee())
		         {consumedAmmunitionOrKnockCount=dualWeaponUseEnabled?2:1;
		    	  //melee weapon
		         }
		     else
		         {final int ammoPerShot=weaponInUse.getAmmunitionPerShot();
		          final WeaponUserData rightHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		          consumedAmmunitionOrKnockCount+=rightHandWeaponUserData.removeAmmunitionFromMagazine(ammoPerShot);
		          if(dualWeaponUseEnabled)
		              {final WeaponUserData leftHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		    	       consumedAmmunitionOrKnockCount+=leftHandWeaponUserData.removeAmmunitionFromMagazine(ammoPerShot);
		    	      }
		         }
		    }
		else
		    {consumedAmmunitionOrKnockCount=1;
			 //punch & kick
		    }
		return(consumedAmmunitionOrKnockCount);
	}
	
	public void updateLogicalLayer(final ReadOnlyTimer timer){
		stateMachine.updateLogicalLayer(timer);
	}
	
	public void trySelectNextWeapon(){
		stateMachine.fireEvent(PlayerEvent.SELECTING_NEXT);
	}
	
    public void trySelectPreviousWeapon(){
    	stateMachine.fireEvent(PlayerEvent.SELECTING_PREVIOUS);
	}
	
	public boolean selectNextWeapon(){
		return(selectWeapon(true));
	}
	
	public boolean selectPreviousWeapon(){
    	return(selectWeapon(false));
	}
	
	public boolean selectWeapon(final int index,final boolean dualWeaponUseWished){
		final Weapon chosenWeapon=weaponFactory.getWeapon(index);
		/**
		 * checks if:
		 * - the index is valid (i.e in [0;weaponCount[)
		 * - the weapon is available in the primary hand
		 * - the weapon is available in the secondary hand if the player wants to use one weapon per hand
		 * - the player does not want to use one weapon per hand
		 */
		final boolean success=chosenWeapon!=null&&primaryHandWeaponContainer.isAvailable(chosenWeapon)&&(!dualWeaponUseWished||secondaryHandWeaponContainer.isAvailable(chosenWeapon));		
		if(success)
			{final Weapon oldWeaponIDInUse=weaponInUse;
	    	 final boolean oldDualWeaponUse=dualWeaponUseEnabled;
			 weaponInUse=chosenWeapon;
			 dualWeaponUseEnabled=dualWeaponUseWished;
			 if(oldWeaponIDInUse!=weaponInUse||oldDualWeaponUse!=dualWeaponUseEnabled)
	    	     {Node oldWeapon,newWeapon;
    		      if(oldWeaponIDInUse!=weaponInUse)
    	              {//if at least one weapon was used previously
    		    	   if(oldWeaponIDInUse!=null)
    		    	       {//drop the right hand weapon
    		    		    oldWeapon=primaryHandWeaponContainer.getNode(oldWeaponIDInUse);
    		    		    oldWeapon.clearControllers();
    		    		    cameraNode.detachChild(oldWeapon);
    		    		    if(oldDualWeaponUse)
    		    		        {//drop the left hand weapon
    		    			     oldWeapon=secondaryHandWeaponContainer.getNode(oldWeaponIDInUse);
    		    			     oldWeapon.clearControllers();
    		    			     cameraNode.detachChild(oldWeapon);
    		    		        }
    		    	       }
    		    	   //add the right hand weapon
    		    	   newWeapon=primaryHandWeaponContainer.getNode(weaponInUse);
    		    	   initializeWeaponLocalTransform(newWeapon,true);
    		    	   cameraNode.attachChild(newWeapon);
    		    	   if(dualWeaponUseEnabled)
    		    	       {//add the left hand weapon
    		    		    newWeapon=secondaryHandWeaponContainer.getNode(weaponInUse);
    		    		    initializeWeaponLocalTransform(newWeapon,false);
    		    		    cameraNode.attachChild(newWeapon);
    		    	       }
    	              }
    		      else
    		    	  //only the dual use has changed
    		          {if(dualWeaponUseEnabled)
    		               {//add the left hand weapon
    		    	        newWeapon=secondaryHandWeaponContainer.getNode(weaponInUse);
    		    	        initializeWeaponLocalTransform(newWeapon,false);
    		    	        cameraNode.attachChild(newWeapon);
    		               }
    		           else
    		    	       if(oldWeaponIDInUse!=null)
    		    	           {//drop the left hand weapon
    		    		        oldWeapon=secondaryHandWeaponContainer.getNode(oldWeaponIDInUse);
    		    		        oldWeapon.clearControllers();
    		    		        cameraNode.detachChild(oldWeapon);
    		    	           }
    		          }
	    	     }
			}
		return(success);
	}
    
    protected boolean selectWeapon(final boolean next){
    	final boolean success;
    	Entry<Integer,Boolean> selectableWeaponIndexAndDualHandEnabledFlag=getSelectableWeaponIndexAndDualHandEnabledFlag(next);
    	if(selectableWeaponIndexAndDualHandEnabledFlag!=null)
    	    {final int selectableWeaponIndex=selectableWeaponIndexAndDualHandEnabledFlag.getKey().intValue();
    		 final boolean dualHandEnabledFlag=selectableWeaponIndexAndDualHandEnabledFlag.getValue().booleanValue();
    		 success=selectWeapon(selectableWeaponIndex, dualHandEnabledFlag);
    	    }
    	else
    		success=false;
    	return(success);
	}
    
    
    /**
     * Returns the index of the weapon that can be selected if any and a flag indicating whether it must be used in both hands, 
     * otherwise null
     * 
     * @param next flag indicating whether the returned weapon is the next one
     * @return the index of the weapon that can be selected if any and a flag indicating whether it must be used in both hands, 
     * otherwise null
     */
    public Entry<Integer,Boolean> getSelectableWeaponIndexAndDualHandEnabledFlag(final boolean next){
    	Entry<Integer,Boolean> result=null;
    	final int weaponCount=weaponFactory.getWeaponCount();
    	//checks whether there is at least one weapon in the factory
    	if(weaponCount>=1)
    	    {final int weaponIndexMultiplier;
    	     //if the player chooses the next weapon, we have to increase the weapon index, otherwise we have to decrease it
    	     if(next)
    	    	 weaponIndexMultiplier=1;
    	     else
    	    	 weaponIndexMultiplier=-1;
    	     final int firstWeaponIndex;
    	     if(weaponInUse!=null)
    	    	 {final int firstInitialFactor;
    	          if(next==dualWeaponUseEnabled)
    	        	  //tries to use another weapon
    	    	      firstInitialFactor=1;
    	          else
    	        	  //tries to use the same weapon, only changes the number of weapons in use
    	    	      firstInitialFactor=0;
    	    	  firstWeaponIndex=((weaponInUse.getUid()+weaponCount)+(weaponIndexMultiplier*firstInitialFactor))%weaponCount;
    	    	 }
    	     else
    	    	 //if the player doesn't use any weapon yet
    	    	 if(next)
    	    		 //tries to choose the first one when he wants the next one
    	    		 firstWeaponIndex=0;
    	    	 else
    	    		 //tries to choose the last one when he wants the previous one
    	    		 firstWeaponIndex=weaponCount-1;
    	     final boolean firstDualWeaponUseEnabledTested;
    	     if(weaponInUse!=null)
    	    	 //tries to use a different number of weapon
    	    	 firstDualWeaponUseEnabledTested=!dualWeaponUseEnabled;
    	     else
    	    	 //if the player doesn't use any weapon yet
    	    	 //tries to use 2 identical weapons only if he wants the previous one
    	    	 firstDualWeaponUseEnabledTested=!next;
    	     //there are 2 iterations per weapon (one for single handed, one for dual handed)
    	     int iterationIndex=firstWeaponIndex*2;
    	     //if we start with dual handed weapons, increases the iteration index
    	     if(firstDualWeaponUseEnabledTested)
    	    	 iterationIndex++;
    	     boolean dualWeaponUseEnabledTested;		     
		     final int maxIterationCount=weaponCount*2;
		     int iterationCount=0,currentWeaponIndex;
		     Weapon chosenWeapon;
		     while(iterationCount<maxIterationCount&&result==null)
		         {currentWeaponIndex=iterationIndex/2;
		          //odd -> dual handed, even -> single handed
		          dualWeaponUseEnabledTested=iterationIndex%2==1;
		          //gets the weapon from the factory
		          chosenWeapon=weaponFactory.getWeapon(currentWeaponIndex);
		          /**
		  		   * checks if:
		  		   * - the index is valid (i.e in [0;weaponCount[)
		  		   * - the weapon is available in the primary hand
		  		   * - the player does not want to use one weapon per hand or the weapon is available in the secondary hand
		  		   */
		          if(chosenWeapon!=null&&primaryHandWeaponContainer.isAvailable(chosenWeapon)&&(!dualWeaponUseEnabledTested||secondaryHandWeaponContainer.isAvailable(chosenWeapon)))
		        	  result=new AbstractMap.SimpleEntry<Integer,Boolean>(Integer.valueOf(currentWeaponIndex),Boolean.valueOf(dualWeaponUseEnabledTested));		        	  
		          else 
		              {//prepares the next iteration
		               //updates the iteration index by using the multiplier
		               iterationIndex=(iterationIndex+weaponIndexMultiplier+maxIterationCount)%maxIterationCount;
		               //increases the iteration count
		               iterationCount++;		        	   
		              }		          
		         }
    	    }
    	return(result);
    }
    
    public boolean isDualWeaponUseEnabled(){
    	return(dualWeaponUseEnabled);
    }
    
    protected void initializeWeaponLocalTransform(Node newWeapon,boolean localizedInThePrimaryHand){
    	Matrix3 correctWeaponRotation=new Matrix3();
    	//FIXME: move this half rotation into the user data of the weapon
    	correctWeaponRotation.fromAngles(0, Math.PI, 0).multiplyLocal(((WeaponUserData)newWeapon.getUserData()).getRotation());
    	newWeapon.setRotation(correctWeaponRotation);
    	if(localizedInThePrimaryHand==rightHanded)
    		newWeapon.setTranslation(-0.17870682064350812,-0.01787068206435081,0.35741364128701625);
    	else
    		newWeapon.setTranslation(0.17870682064350812,-0.01787068206435081,0.35741364128701625);
    }
}
