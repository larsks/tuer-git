package main;

import java.nio.FloatBuffer;
import java.util.List;

class EnemyModel extends Collidable {

    
    private int health;
    
    private int maximumHealth;
    
    private int scopeAngle;
    
    private int scopeDepth;
    
    private DeathDescriptor deathDescriptor;
    
    
    EnemyModel(){}

    EnemyModel(float x, float y, float z,
            List<FloatBuffer> coordinatesBuffersList,
            List<AnimationInfo> animationList, float horizontalDirection,
            float verticalDirection, Clock internalClock,int health,
            int maximumHealth,int scopeAngle,int scopeDepth){
        super(x, y, z, coordinatesBuffersList, animationList,
                horizontalDirection, verticalDirection, internalClock);     
        this.health=health;
        this.maximumHealth=maximumHealth;
        this.scopeAngle=scopeAngle;
        this.scopeDepth=scopeDepth;
        //the clock should be correctly set
        if(this.health<=0)
            {this.health=0;
             //TODO: precise the cause when the player can use several weapons
             deathDescriptor=new DeathDescriptor(getElapsedTime(),null);
            }
        else
            deathDescriptor=null;
    }
    

    final int getHealth(){
        return(health);
    }

    final void setHealth(int health){
        this.health=health;
    }
    
    void decreaseHealth(int damage){
        int previousHealth=health;
        this.health-=damage;
        if(this.health<0)
            this.health=0;
        if(previousHealth>0 && health==0)
            deathDescriptor=new DeathDescriptor(getElapsedTime(),null);       
    }
    
    //indicates the time spent since the death
    private final long getElapsedTimeSinceDeath(){
        return(deathDescriptor!=null?getElapsedTime()-deathDescriptor.getInstant():0);
    }
    
    //indicates if the enemy has been exploded
    private final boolean hasBeenExploded(){
        return(deathDescriptor!=null?deathDescriptor.isCausedByExplosion():false);
    }
    
    final boolean isAlive(){
        return(this.health>0);
    }

    final int getMaximumHealth(){
        return(maximumHealth);
    }

    final void setMaximumHealth(int maximumHealth){
        this.maximumHealth=maximumHealth;
    }

    protected boolean isOpponentInScope(PlayerModel player){
        //TODO: use the scope angle and the scope depth
        //to determine if the player is in the scope
        return(false);
    }
    
    protected boolean isOpponentInTarget(PlayerModel player){
        //TODO: determine if the player can be shot, right on sight
        return(false);
    }
    
    protected boolean isColleagueInTarget(EnemyModel enemy){
        //TODO: determine if a colleague can be shot, right on sight
        //(mainly to avoid friendly fire)
        return(false);
    }
    
    //move inside the level with or without a path
    protected void patrol(){}
    
    //ask for someone's help
    protected void askForHelp(){}
    
    //flee from the current location
    protected void getAway(){}
    
    //attack the player
    protected void attack(){}
    
    //find a nice place to get protected when reloading or when waiting for help
    protected void getCovered(){}

    //choose an action among the available ones after analyzing the situation
    protected void decide(){}
    
    final int getScopeAngle(){
        return(scopeAngle);
    }

    final void setScopeAngle(int scopeAngle){
        this.scopeAngle=scopeAngle;
    }

    final int getScopeDepth(){
        return(scopeDepth);
    }

    final void setScopeDepth(int scopeDepth){
        this.scopeDepth=scopeDepth;
    }
    
    /**
     * no need to call it directly
     */
    @Override
    void updateAnimationIndex(){
        //TODO: update the animation index by using hasBeenExploded(), 
        //getElapsedTimeSinceDeath() and isAlive()
        
    }
}
