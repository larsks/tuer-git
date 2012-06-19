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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import com.ardor3d.math.Matrix3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.CameraNode;

import engine.data.common.userdata.AmmunitionUserData;
import engine.data.common.userdata.CollectibleUserData;
import engine.data.common.userdata.MedikitUserData;
import engine.data.common.userdata.WeaponUserData;
import engine.weaponry.Ammunition;
import engine.weaponry.AmmunitionContainerContainer;
import engine.weaponry.AmmunitionFactory;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponContainer;
import engine.weaponry.WeaponFactory;

/**
 * Data model of the player. It contains some operations for these data too.
 * 
 * @author Julien Gouesse
 * 
 */
public class PlayerData {
    
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
	protected Weapon weaponInUse;
	/**node representing the camera*/
	protected CameraNode cameraNode;
	/**factory that creates weapons*/
	private final WeaponFactory weaponFactory;
	/**container of ammunition container*/
	private final AmmunitionContainerContainer ammoContainerContainer;
	/**duration of a "put back" operation in seconds*/
	private static final double PUT_BACK_DURATION_IN_SECONDS=0.2;
	/**duration of a "wait for attack end" operation in seconds*/
	private static final double ATTACK_END_DURATION_IN_SECONDS=0.1;
	/**duration of a "pull out" operation in seconds*/
	private static final double PULL_OUT_DURATION_IN_SECONDS=0.2;
	/**ordinate of the weapon when it is ready to be used*/
	private static final double PULLED_OUT_WEAPON_ORDINATE=-0.01787068206435081;
	/**ordinate of the weapon when it has been put away (not ready to be used)*/
	private static final double PUT_BACK_WEAPON_ORDINATE=10*PULLED_OUT_WEAPON_ORDINATE;
	
	private static final double ATTACK_WEAPON_MAXIMUM_ORDINATE=0;
	
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
		this.rightHanded=rightHanded;
	}
	
	/**
	 * Collects a collectible object if possible
	 * 
	 * @param collectible
	 * @return count of collected sub elements
	 */
	public int collect(final Node collectible){
		//checks if the collectible can be collected
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
		    	         //handles here the other kinds of collectible objects
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
		    {/**
			  * TODO change the owner uid (if there is no digital watermark) and detach them from the player
			  *      empty the container of ammunition container (use WeaponUserData)
			  */
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
	 * Returns the progress of the "put back"
	 * 
	 * @return a value in the interval [0;1] representing the progress of the "put back" according to the current ordinate of the weapon
	 */
	public double computePutBackProgress(){
		final double putBackProgress;
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
		     final double y=primaryWeaponNode.getTranslation().getY();
			 final double putBackYStart=PULLED_OUT_WEAPON_ORDINATE;
		     final double putBackYEnd=PUT_BACK_WEAPON_ORDINATE;
			 if(y==putBackYStart)
				 putBackProgress=0;
			 else
				 if(y==putBackYEnd)
					 putBackProgress=1;
				 else
				     putBackProgress=Math.max(0,Math.min(1.0d,(y-putBackYStart)/(putBackYEnd-putBackYStart)));
		    }
		else
			putBackProgress=1.0;		
		return(putBackProgress);
	}
	
	/**
	 * Returns the progress of the attack ending
	 * 
	 * @return a value in the interval [0;1] representing the progress of the attack ending according to the current ordinate of the weapon
	 */
	public double computeEndAttackProgress(){
		final double endAttackProgress;
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
		     final double y=primaryWeaponNode.getTranslation().getY();
			 final double endAttackYStart=ATTACK_WEAPON_MAXIMUM_ORDINATE;
		     final double endAttackYEnd=PULLED_OUT_WEAPON_ORDINATE;
			 if(y==endAttackYStart)
				 endAttackProgress=0;
			 else
				 if(y==endAttackYEnd)
					 endAttackProgress=1;
				 else
					 endAttackProgress=Math.max(0,Math.min(1.0d,(y-endAttackYStart)/(endAttackYEnd-endAttackYStart)));
		    }
		else
			endAttackProgress=1.0;		
		return(endAttackProgress);
	}
	
	/**
	 * Puts back the current weapon(s) if any
	 * 
	 * @param elapsedTimeSincePutBackStartInSeconds elapsed time since the start of the "put back" 
	 * step expressed in seconds
	 * @param initialPutBackProgress initial progress of the "put back"
	 */
	public void putBack(final double elapsedTimeSincePutBackStartInSeconds,final double initialPutBackProgress){
		if(isCurrentWeaponAmmunitionCountDisplayable())
            {//computes the progress of the "put back" step (in the interval [0;1])
			 final double putBackStepProgress=Math.max(0,Math.min(1.0d,initialPutBackProgress+(elapsedTimeSincePutBackStartInSeconds/PUT_BACK_DURATION_IN_SECONDS)));
			 final double putBackYStart=PULLED_OUT_WEAPON_ORDINATE;
			 final double putBackYEnd=PUT_BACK_WEAPON_ORDINATE;
			 //computes the ordinate with the progress
			 final double putBackYCurrent;
			 if(putBackStepProgress==0)
				 putBackYCurrent=putBackYStart;
			 else
				 if(putBackStepProgress==1)
					 putBackYCurrent=putBackYEnd;
				 else
					 putBackYCurrent=putBackYStart+((putBackYEnd-putBackYStart)*putBackStepProgress);
			 final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
	         //modifies the ordinate of the primary weapon
			 primaryWeaponNode.setTranslation(primaryWeaponNode.getTranslation().getX(),putBackYCurrent,primaryWeaponNode.getTranslation().getZ());
		     if(isDualWeaponUseEnabled())
		         {final Node secondaryWeaponNode=secondaryHandWeaponContainer.getNode(weaponInUse);
		          //modifies the ordinate of the secondary weapon
		          secondaryWeaponNode.setTranslation(secondaryWeaponNode.getTranslation().getX(),putBackYCurrent,secondaryWeaponNode.getTranslation().getZ());
		         }
            }
	}
	
	/**
	 * Pulls out the current weapon(s) if any
	 * 
	 * @param elapsedTimeSincePullOutStartInSeconds elapsed time since the start of the "pull out" 
	 * step expressed in seconds
	 */
	public void pullOut(final double elapsedTimeSincePullOutStartInSeconds){
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {//computes the progress of the "pull out" step (in the interval [0;1])
			 final double pullOutStepProgress=Math.max(0,Math.min(1.0d,elapsedTimeSincePullOutStartInSeconds/PULL_OUT_DURATION_IN_SECONDS));
			 final double pullOutYEnd=PULLED_OUT_WEAPON_ORDINATE;
			 final double pullOutYStart=PUT_BACK_WEAPON_ORDINATE;
			 //computes the ordinate with the progress
			 final double pullOutYCurrent;
			 if(pullOutStepProgress==0)
				 pullOutYCurrent=pullOutYStart;
			 else
				 if(pullOutStepProgress==1)
					 pullOutYCurrent=pullOutYEnd;
				 else
					 pullOutYCurrent=pullOutYStart+((pullOutYEnd-pullOutYStart)*pullOutStepProgress);
			 final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
	         //modifies the ordinate of the primary weapon
			 primaryWeaponNode.setTranslation(primaryWeaponNode.getTranslation().getX(),pullOutYCurrent,primaryWeaponNode.getTranslation().getZ());
		     if(isDualWeaponUseEnabled())
		         {final Node secondaryWeaponNode=secondaryHandWeaponContainer.getNode(weaponInUse);
		          //modifies the ordinate of the secondary weapon
		          secondaryWeaponNode.setTranslation(secondaryWeaponNode.getTranslation().getX(),pullOutYCurrent,secondaryWeaponNode.getTranslation().getZ());
		         }
		    }
	}
	
	/**
	 * Returns whether the "put back" is complete
	 * 
	 * @return <code>true</code> if the "put back" is complete, otherwise <code>false</code>
	 */
	public boolean isPutBackComplete(){
		final boolean isPutBackComplete;
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
			 final double putBackYEnd=PUT_BACK_WEAPON_ORDINATE;
			 //checks if the operation has completed
			 isPutBackComplete=primaryWeaponNode.getTranslation().getY()==putBackYEnd;
		    }
		else
			isPutBackComplete=true;
		return(isPutBackComplete);
	}
	
	/**
	 * Returns whether the "pull out" is complete
	 * 
	 * @return <code>true</code> if the "pull out" is complete, otherwise <code>false</code>
	 */
	public boolean isPullOutComplete(){
		final boolean isPullOutComplete;
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
		     final double pullOutYEnd=PULLED_OUT_WEAPON_ORDINATE;
		     //checks if the operation has completed
			 isPullOutComplete=primaryWeaponNode.getTranslation().getY()==pullOutYEnd;
		    }
		else
			isPullOutComplete=true;
		return(isPullOutComplete);
	}
	
	public boolean isPressTriggerComplete(){
		//TODO
		return(true);
	}
	
	public boolean isReleaseTriggerComplete(){
		//TODO
		return(true);
	}
	
	public boolean isAttackComplete(){
		final boolean isAttackComplete;
		if(isCurrentWeaponAmmunitionCountDisplayable())
		    {final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
		     final double attackYEnd=PULLED_OUT_WEAPON_ORDINATE;
		     //checks if the operation has completed
		     isAttackComplete=primaryWeaponNode.getTranslation().getY()==attackYEnd;
		    }
		else
			isAttackComplete=true;
		return(isAttackComplete);
	}
	
	public boolean isWaitForTriggerReleaseComplete(){
		//TODO
		return(true);
	}
	
	public void pressTrigger(final double elapsedTimeSincePressTriggerStartInSeconds){
		//TODO
	}
	
	public void attack(final double elapsedTimeSinceAttackStartInSeconds){
		final double currentWeaponBlowOrShotDurationInSeconds=getCurrentWeaponBlowOrShotDurationInMillis()/1000.0;
		final double attackStepProgress=Math.max(0,Math.min(1.0,(elapsedTimeSinceAttackStartInSeconds/currentWeaponBlowOrShotDurationInSeconds)%1.0));
		if(isCurrentWeaponAmmunitionCountDisplayable())
			{//TODO sparks?
			 /*if(attackStepProgress<1.0)
		         {
			      
		         }
		     else
		         {
			      
		         }*/
			 final double attackVariationPercentage=Math.sin(attackStepProgress*Math.PI);
			 final double attackYEnd=ATTACK_WEAPON_MAXIMUM_ORDINATE;
			 final double attackYStart=PULLED_OUT_WEAPON_ORDINATE;
			  //computes the ordinate with the progress
			 final double attackYCurrent;
			 if(attackStepProgress==0)
				 attackYCurrent=attackYStart;
			 else
				 if(attackStepProgress==1)
					 attackYCurrent=attackYStart;
				 else
					 attackYCurrent=attackYStart+((attackYEnd-attackYStart)*attackVariationPercentage);
			 if(attackYCurrent<PULLED_OUT_WEAPON_ORDINATE)
				 System.out.println("pb: " + attackYCurrent + " attackVariationPercentage: " + attackVariationPercentage);
			 final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
	         //modifies the ordinate of the primary weapon
			 primaryWeaponNode.setTranslation(primaryWeaponNode.getTranslation().getX(),attackYCurrent,primaryWeaponNode.getTranslation().getZ());
		     if(isDualWeaponUseEnabled())
		         {final Node secondaryWeaponNode=secondaryHandWeaponContainer.getNode(weaponInUse);
		          //modifies the ordinate of the secondary weapon
		          secondaryWeaponNode.setTranslation(secondaryWeaponNode.getTranslation().getX(),attackYCurrent,secondaryWeaponNode.getTranslation().getZ());
		         }
			}
		else
		    {
			 
		    }
	}
	
	public void releaseTrigger(final double elapsedTimeSinceReleaseTriggerStartInSeconds){
		//TODO
	}
	
	public void waitForTriggerRelease(final double elapsedTimeSinceWaitForTriggerReleaseStartInSeconds){
		//TODO
	}
	
	public void waitForAttackEnd(final double elapsedTimeSinceWaitForAttackEndStartInSeconds,final double initialEndAttackProgress){
		if(isCurrentWeaponAmmunitionCountDisplayable())
            {//computes the progress of the "put back" step (in the interval [0;1])
		     final double endAttackStepProgress=Math.max(0,Math.min(1.0d,initialEndAttackProgress+(elapsedTimeSinceWaitForAttackEndStartInSeconds/ATTACK_END_DURATION_IN_SECONDS)));
		     final double endAttackYStart=ATTACK_WEAPON_MAXIMUM_ORDINATE;
		     final double endAttackYEnd=PULLED_OUT_WEAPON_ORDINATE;
		     //computes the ordinate with the progress
		     final double endAttackYCurrent;
		     if(endAttackStepProgress==0)
		    	 endAttackYCurrent=endAttackYStart;
		     else
			     if(endAttackStepProgress==1)
			    	 endAttackYCurrent=endAttackYEnd;
			     else
			    	 endAttackYCurrent=endAttackYStart+((endAttackYEnd-endAttackYStart)*endAttackStepProgress);
		     final Node primaryWeaponNode=primaryHandWeaponContainer.getNode(weaponInUse);
             //modifies the ordinate of the primary weapon
		     primaryWeaponNode.setTranslation(primaryWeaponNode.getTranslation().getX(),endAttackYCurrent,primaryWeaponNode.getTranslation().getZ());
	         if(isDualWeaponUseEnabled())
	             {final Node secondaryWeaponNode=secondaryHandWeaponContainer.getNode(weaponInUse);
	              //modifies the ordinate of the secondary weapon
	              secondaryWeaponNode.setTranslation(secondaryWeaponNode.getTranslation().getX(),endAttackYCurrent,secondaryWeaponNode.getTranslation().getZ());
	             }
        }
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
	
	public boolean isCurrentWeaponFullyAutomatic(){
		return(weaponInUse!=null&&!weaponInUse.isForMelee()&&weaponInUse.isFullyAutomatic());
	}
	
	public int getCurrentWeaponBlowOrShotDurationInMillis(){
		final int currentWeaponBlowOrShotDurationInMillis;
		if(weaponInUse!=null)
		    {//uses the real duration
			 currentWeaponBlowOrShotDurationInMillis=weaponInUse.getBlowOrShotDurationInMillis();
		    }
		else
		    {//uses the duration of a punch
			 currentWeaponBlowOrShotDurationInMillis=1000;
		    }
		return(currentWeaponBlowOrShotDurationInMillis);
	}
	
	public String getCurrentWeaponBlowOrShotSoundSampleIdentifier(){
		final String blowOrShotIdentifier;
		if(weaponInUse!=null)
		    blowOrShotIdentifier=weaponInUse.getBlowOrShotSoundSampleIdentifier();
		else
			blowOrShotIdentifier=null;
		return(blowOrShotIdentifier);
	}
	
	public String getCurrentWeaponReloadSoundSampleIdentifier(){
		final String reloadIdentifier;
		if(weaponInUse!=null)
			reloadIdentifier=weaponInUse.getReloadSoundSampleIdentifier();
		else
			reloadIdentifier=null;
		return(reloadIdentifier);
	}
	
	public int getAmmunitionCountInSecondaryHandedWeapon(){
		final int ammunitionCountInSecondaryHandedWeapon;
		if(weaponInUse!=null&&!weaponInUse.isForMelee()&&dualWeaponUseEnabled)
		    {final WeaponUserData secondaryHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		     ammunitionCountInSecondaryHandedWeapon=secondaryHandWeaponUserData.getAmmunitionCountInMagazine();
		    }
		else
			ammunitionCountInSecondaryHandedWeapon=0;
		return(ammunitionCountInSecondaryHandedWeapon);
	}
	
	public int getAmmunitionCountInPrimaryHandedWeapon(){
		final int ammunitionCountInPrimaryHandedWeapon;
		if(weaponInUse!=null&&!weaponInUse.isForMelee())
		    {final WeaponUserData primaryHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		     ammunitionCountInPrimaryHandedWeapon=primaryHandWeaponUserData.getAmmunitionCountInMagazine();
		    }
		else
			ammunitionCountInPrimaryHandedWeapon=0;
		return(ammunitionCountInPrimaryHandedWeapon);
	}
	
	/**
	 * Launches an attack
	 * 
	 * @return consumed ammunition if the weapon is not a melee weapon, knock count otherwise
	 */
	public Map.Entry<Integer,Integer> attack(){
		final int consumedAmmunitionOrKnockCountWithPrimaryHand,consumedAmmunitionOrKnockCountWithSecondaryHand;
		if(weaponInUse!=null&&!weaponInUse.isForMelee())
		    {final int ammoPerShot=weaponInUse.getAmmunitionPerShot();
		     final WeaponUserData primaryHandWeaponUserData=(WeaponUserData)primaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		     consumedAmmunitionOrKnockCountWithPrimaryHand=primaryHandWeaponUserData.removeAmmunitionFromMagazine(ammoPerShot);
		     if(dualWeaponUseEnabled)
		         {final WeaponUserData secondaryHandWeaponUserData=(WeaponUserData)secondaryHandWeaponContainer.getNode(weaponInUse).getUserData();
		          consumedAmmunitionOrKnockCountWithSecondaryHand=secondaryHandWeaponUserData.removeAmmunitionFromMagazine(ammoPerShot);
		    	 }
		     else
		    	 consumedAmmunitionOrKnockCountWithSecondaryHand=0;
		    }
		else
		    {consumedAmmunitionOrKnockCountWithPrimaryHand=1;
		     consumedAmmunitionOrKnockCountWithSecondaryHand=dualWeaponUseEnabled?1:0;
			 //punch & kick or melee weapon(s)
		    }
		final Map.Entry<Integer,Integer> consumedAmmunitionOrKnockCounts=new AbstractMap.SimpleEntry<Integer,Integer>(Integer.valueOf(consumedAmmunitionOrKnockCountWithPrimaryHand),Integer.valueOf(consumedAmmunitionOrKnockCountWithSecondaryHand));
		return(consumedAmmunitionOrKnockCounts);
	}
	
	/**
	 * Tells whether this player can attack
	 * 
	 * @return <code>true</code> if the player can attack with his current weapon, otherwise <code>false</code>
	 */
	public boolean canAttack(){
		final boolean canAttack;
		if(weaponInUse!=null)
		    {if(weaponInUse.isForMelee())
		    	 canAttack=true;
		     else
		    	 canAttack=getAmmunitionCountInPrimaryHandedWeapon()>0||getAmmunitionCountInSecondaryHandedWeapon()>0;
		    }
		else
			canAttack=true;
		return(canAttack);
	}
	
	/**
	 * Tells whether this player can reload
	 * 
	 * @return <code>true</code> if the player can reload his current weapon, otherwise <code>false</code>
	 */
	public boolean canReload(){
		final boolean canReload=getReloadableAmmoCountForPrimaryHandWeapon()>0||getReloadableAmmoCountForSecondaryHandWeapon()>0;
        return(canReload);
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
    		    	       {//drops the right hand weapon
    		    		    oldWeapon=primaryHandWeaponContainer.getNode(oldWeaponIDInUse);
    		    		    oldWeapon.clearControllers();
    		    		    cameraNode.detachChild(oldWeapon);
    		    		    if(oldDualWeaponUse)
    		    		        {//drops the left hand weapon
    		    			     oldWeapon=secondaryHandWeaponContainer.getNode(oldWeaponIDInUse);
    		    			     oldWeapon.clearControllers();
    		    			     cameraNode.detachChild(oldWeapon);
    		    		        }
    		    	       }
    		    	   //adds the right hand weapon
    		    	   newWeapon=primaryHandWeaponContainer.getNode(weaponInUse);
    		    	   initializeWeaponLocalTransform(newWeapon,true);
    		    	   cameraNode.attachChild(newWeapon);
    		    	   if(dualWeaponUseEnabled)
    		    	       {//adds the left hand weapon
    		    		    newWeapon=secondaryHandWeaponContainer.getNode(weaponInUse);
    		    		    initializeWeaponLocalTransform(newWeapon,false);
    		    		    cameraNode.attachChild(newWeapon);
    		    	       }
    	              }
    		      else
    		    	  //only the dual use has changed
    		          {//FIXME dirty kludge, forces the ordinate of the right weapon
    		           newWeapon=primaryHandWeaponContainer.getNode(weaponInUse);
       		    	   initializeWeaponLocalTransform(newWeapon,true);
    		    	   if(dualWeaponUseEnabled)
    		               {//adds the left hand weapon
    		    	        newWeapon=secondaryHandWeaponContainer.getNode(weaponInUse);
    		    	        initializeWeaponLocalTransform(newWeapon,false);
    		    	        cameraNode.attachChild(newWeapon);
    		               }
    		           else
    		    	       if(oldWeaponIDInUse!=null)
    		    	           {//drops the left hand weapon
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
    	final int weaponCount=weaponFactory.getSize();
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
    		newWeapon.setTranslation(-0.17870682064350812,PUT_BACK_WEAPON_ORDINATE,0.35741364128701625);
    	else
    		newWeapon.setTranslation(0.17870682064350812,PUT_BACK_WEAPON_ORDINATE,0.35741364128701625);
    }
}
