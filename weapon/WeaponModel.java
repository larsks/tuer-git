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

package weapon;

import main.Clock;

class WeaponModel {

    /**
     * name
     */
    private String name;
    
    /**
     * caliber
     */
    private AmmunitionType ammoType;
    
    /**
     * behavior of ammunition
     */
    private ProjectileModel shotProjectileBehaviour;
    
    /**
     * internal ammunition storage used by the weapon
     */
    private AmmunitionStorage magazine;  
    
    /**
     * external ammunition storage on the agent shared by several weapons
     */
    private AmmunitionStorage externalStorage;
    
    /**
     * count of projectile shot by
     */
    private int projectilePerShot;
    
    /**
     * required time to reload a weapon
     */
    private int reloadDuration;//in milliseconds
    
    /**
     * minimal duration between two successive shots.
     */
    private int minimalShotInterval;
    
    /**
     * minimal interval between two launches of projectile   
     */
    private int minimalProjectileInterval;
    
    /**
     * clock to synchronize the weapon
     */
    private Clock internalClock;
    
    /**
     * most recent time of reload
     */
    private long lastReloadTime;
    
    /**
     * most recent time of shot
     */
    private long lastShotTime;
    
    /**
     * 
     * @param name
     * @param ammoType
     * @param ammoCountInMagazine
     * @param magazineSize
     * @param projectilePerShot
     * @param reloadDuration
     * @param minimalShotInterval
     * @param minimalProjectileInterval
     * @param automatic
     * @param projectileModelFactory
     */
    WeaponModel(String name,AmmunitionType ammoType,int ammoCountInMagazine,
            int magazineSize,AmmunitionStorage externalStorage,int projectilePerShot,
            int reloadDuration,int minimalShotInterval,int minimalProjectileInterval,
            Clock internalClock){
        this.name=name;
        this.ammoType=ammoType;
        this.shotProjectileBehaviour=ProjectileModelFactory.getInstance(ammoType);
        this.magazine=new AmmunitionStorage(ammoType,ammoCountInMagazine,magazineSize);
        this.externalStorage=externalStorage;
        this.projectilePerShot=projectilePerShot;
        this.reloadDuration=reloadDuration;
        this.minimalShotInterval=minimalShotInterval;
        this.minimalProjectileInterval=minimalProjectileInterval;
        this.internalClock=internalClock;
        this.lastReloadTime=0;
        this.lastShotTime=0;
    }
    
    
    /**
     * 
     * @return
     */
    final boolean isProjectionShotSimultaneous(){
        return(minimalProjectileInterval==0 || projectilePerShot==1);
    }
    
    final String getName(){
        return(name);
    }
    
    final AmmunitionType getAmmoType(){
        return(ammoType);
    }
    
    final int reload(){
        if(!isBusy())
            {int amount=magazine.fill(externalStorage);
             if(amount>0)
                 lastReloadTime=internalClock.getElapsedTime();      
             return(amount);
            }
        else
            return(0);
    }
    
    final int shoot(){              
        if(!isBusy())
            {int amount=magazine.empty(projectilePerShot);
             if(amount>0)
                 lastShotTime=internalClock.getElapsedTime();      
             return(amount);
            }
        else
            return(0);
    }
    
    final private boolean isReloading(){
        long time=internalClock.getElapsedTime();
        return(lastReloadTime<=time && time<=lastReloadTime+reloadDuration);
    }
    
    final private boolean isShooting(){
        long time=internalClock.getElapsedTime();
        return(lastShotTime<=time && time<=lastShotTime+minimalShotInterval);
    }
    
    final private boolean isBusy(){
        return(isReloading() || isShooting());
    }
    
    /**
     * get the maximum damage caused by this weapon
     * (used by the model to update the health of the ennemies)
     * @return
     */
    /*final int getDamage(){
        return(shotProjectileBehaviour.getDamage());
    }*/
    
    /**
     * get the automatic behavior of the weapon
     * (used to know if the weapon is used in keyPressed() (when auto) 
     *  or after keyPressed() and keyReleased() (when semi-auto))
     * @return
     */
    final boolean isAutomatic(){
        return(minimalShotInterval==0);
    }
    
    /**
     * get the minimal interval between 2 successive launches of projectile
     * (used by the WeaponView to know how to draw a complete shot)
     * @return
     */
    final int getMinimalProjectileInterval(){
        return(minimalProjectileInterval);
    }
}
