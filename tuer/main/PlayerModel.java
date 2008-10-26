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

package main;

import java.awt.geom.Rectangle2D;

import weapon.AmmunitionRepository;
import weapon.RocketLauncherModel;

class PlayerModel implements Collector{
    
    
    private static final int startingHealth=100;
    
    private static final int maximumHealth=100;
    
    private static final int defaultBoundingSize=GameModel.factor/4;
    
    private int health;
    
    private boolean invincible;
    
    private boolean winner;
    
    private double x;
    
    private double y;
    
    private double z;
    
    private double direction;//in radians
    
    private int boundingSize;
    
    private Rectangle2D.Double voxel;
    
    private AmmunitionRepository ammoExternalRepository;
    //TODO: get a weapon only by collecting it
    private RocketLauncherModel rocketLauncherModel;
    
    
    PlayerModel(Clock clock){
        this.health=startingHealth;
        this.invincible=false;
        this.winner=false;
        this.x=0;
        this.y=0;
        this.z=0;
        this.direction=0;
        this.boundingSize=defaultBoundingSize;
        this.voxel=new Rectangle2D.Double();
        updateVoxel();
        this.ammoExternalRepository=new AmmunitionRepository();
        this.rocketLauncherModel=new RocketLauncherModel(ammoExternalRepository,clock);
    }
    
    private final void updateVoxel(){
        this.voxel.setRect(x,z,boundingSize,boundingSize);
    }
    
    void decreaseHealth(int damage){
        if(!invincible)
            {this.health-=damage;
             if(this.health<0)
                 this.health=0;
            }
    }
    
    @Override
    public final int increaseHealth(int amount){
        int oldHealth=health;
        if(amount>0 && health<maximumHealth)
            health=Math.min(maximumHealth,health+amount);
        return(health-oldHealth);
    }
    
    boolean isAlive(){
        return(this.health>0);
    }
    
    int getHealth(){
        return(health);
    }
    
    boolean isWinning(){
        return(winner);
    }
    void respawn(){
        this.health=startingHealth;
    }
    
    void setAsWinner(){
        this.winner=true;
    }
    
    void setAsLoser(){
        this.winner=false;
    }
    
    double getX(){
        return(x);
    }
    
    double getY(){
        return(y);
    }
    
    double getZ(){
        return(z);
    }
    
    double getDirection(){
        return(direction);
    }
    
    final int getBoundingSize(){
        return(boundingSize);
    }
    
    final Rectangle2D.Double getVoxel(){
        return(this.voxel);
    }
    
    void setX(double x){
        this.x=x;
        updateVoxel();
    }
    
    void setY(double y){
        this.y=y;
        //updateVoxel();
    }
    
    void setZ(double z){
        this.z=z;
        updateVoxel();
    }
    
    void setDirection(double direction){
        this.direction=direction;
    }
    
    final void setBoundingSize(int boundingSize){
        this.boundingSize=boundingSize;
        updateVoxel();
    }

    @Override
    public final boolean collects(Collectable collectable){       
        if(collectable.updatesAsBeingCollectedBy(this))
            {//TODO: transmit this event to the view through the controller
             //use the collectable name
             return(true);
            }
        else
            return(false);
    }
    
    //TODO: redesign this class to extend Collidable
    boolean intersectsWith(Collidable collidable){
        double radiusSumSquare,distanceSquare;
        radiusSumSquare=boundingSize+
        collidable.getBoundingSphereRadiusArray()[collidable.currentFrameIndex];
        radiusSumSquare*=radiusSumSquare;
        distanceSquare=(x-collidable.x)*(x-collidable.x)+
                       (y-collidable.y)*(y-collidable.y)+
                       (z-collidable.z)*(z-collidable.z);
        //use the bounding spheres at first
        if(distanceSquare<radiusSumSquare)
            {//TODO: perform finer tests, use bounding boxes with transforms
             //TODO: perform finest tests on the whole geometry
             return(true);
            }
        else
            return(false);
    }
}
