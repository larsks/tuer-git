package main;

import weapon.AmmunitionType;

final class DeathDescriptor{
    
    
    private long instant;
    
    private AmmunitionType lethalAmmoType;
    
    
    DeathDescriptor(long instant,AmmunitionType lethalAmmoType){
        this.instant=instant;
        this.lethalAmmoType=lethalAmmoType;
    }


    final long getInstant() {
        return instant;
    }
    
    //TODO: treat more death causes
    final boolean isCausedByExplosion(){
        return(lethalAmmoType==null || 
               lethalAmmoType.equals(AmmunitionType.GRENADE) || 
               lethalAmmoType.equals(AmmunitionType.ROCKET));
    }
}
