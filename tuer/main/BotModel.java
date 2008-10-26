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

import java.io.Serializable;
//JAVABEAN OK
class BotModel implements Serializable{
    
    
    private static final long serialVersionUID=1L;

    static final int startingHealth=40;
    
    private int health;
    
    private boolean invincible;
    
    private boolean winner;
    
    private double x;
    
    private double y;
    
    private double z;
    
    private double respawnX;
    
    private double respawnY;
    
    private double respawnZ;
    
    private double direction;
    
    private short face;
    
    private int areaIndex;
    
    private boolean running;
    
    private long time;
    
    static final int NO_AREA=-1;
    
    private static final int frameLength = 50;//in milliseconds
    
    private static final int frameCount = 11;
    
    
    BotModel(){
        this.health=startingHealth;
        this.invincible=false;
        this.winner=false;
        this.x=0;
        this.y=0;
        this.z=0;
        this.respawnX=x;
        this.respawnY=y;
        this.respawnZ=z;
        this.direction=0;
        this.face=0;
        this.areaIndex=NO_AREA;
        this.running=false;
        this.time=System.currentTimeMillis();
    }
    
    BotModel(double x,double y,double z){
        this.health=startingHealth;
        this.invincible=false;
        this.winner=false;
        this.x=x;
        this.y=y;
        this.z=z;
        this.respawnX=x;
        this.respawnY=y;
        this.respawnZ=z;
        this.direction=0;
        this.face=0;
        this.areaIndex=NO_AREA;
        this.running=false;
        this.time=System.currentTimeMillis();
    }
    
    
    public void setAreaIndex(int areaIndex){
        this.areaIndex=areaIndex;
    }
    
    public int getAreaIndex(){
        return(areaIndex);
    }
    
    void decreaseHealth(int damage){
        if(!invincible)
            {this.health-=damage;
             if(this.health<0)
                 this.health=0;
            }
    }
    
    public short getFace(){
        updateFace();	
	    return(face);
    }
    
    boolean isAlive(){
        return(this.health>0);
    }
    
    public int getHealth(){
        return(health);
    }
    
    void respawn(){
        this.direction=0;
        this.face=0;
        this.invincible=false;
        this.winner=false;
        this.x=this.respawnX;
        this.y=this.respawnY;
        this.z=this.respawnZ;
        this.health=startingHealth;
    }
    
    void setAsWinner(){
        this.winner=true;
    }
    
    void setAsLoser(){
        this.winner=false;
    }
    
    public double getX(){
        return(x);
    }
    
    public double getY(){
        return(y);
    }
    
    public double getZ(){
        return(z);
    }
    
    public double getRespawnX(){
        return(respawnX);
    }
    
    public double getRespawnY(){
        return(respawnY);
    }
    
    public double getRespawnZ(){
        return(respawnZ);
    }
    
    public double getDirection(){
        return(direction);
    }
    
    public void setX(double x){
        this.x=x;
    }
    
    public void setY(double y){
        this.y=y;
    }
    
    public void setZ(double z){
        this.z=z;
    }
    
    public void setDirection(double direction){
        this.direction=direction;
    }
    
    public void setRunning(boolean running){
        if(running!=this.running)
	        toggleRunning();
    }
    
    private void toggleRunning(){
        this.running=!this.running;
	    this.time=System.currentTimeMillis();
    }
    
    public void setFace(short face){
        this.face=face;	
    }
    
    private void updateFace(){
        if(!running)
            setFace((short)(((System.currentTimeMillis()-time)/frameLength)%2));
        else
            setFace((short)(((System.currentTimeMillis()-time)/frameLength)%frameCount));
    }

    public final boolean isInvincible() {
        return invincible;
    }

    public final void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public final boolean isWinner() {
        return(winner);
    }

    public final void setWinner(boolean winner) {
        this.winner = winner;
    }

    public final long getTime() {
        return time;
    }

    public final void setTime(long time) {
        this.time = time;
    }

    public final boolean isRunning() {
        return running;
    }

    public final void setHealth(int health) {
        this.health = health;
    }

    public final void setRespawnX(double respawnX) {
        this.respawnX = respawnX;
    }

    public final void setRespawnY(double respawnY) {
        this.respawnY = respawnY;
    }

    public final void setRespawnZ(double respawnZ) {
        this.respawnZ = respawnZ;
    }
}
