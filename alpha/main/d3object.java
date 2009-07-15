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

final class d3object{


   private double x;        // multiplied with 65536.0, like a
   private double z;        // virtual 16-bit shift to the left.
   private double dir;      // in radians (in degrees in the past, 0..359)
   private float speed;    // if moving, otherwise alive indicator
   private int    shape;    // or the type of object
   private int    sleep2;   // suspend walking this long
   private boolean seenPlayer;
   
   double getX(){return(x);}        
   double getZ(){return(z);}        
   double getDir(){return(dir);}      
   float  getSpeed(){return(speed);}
   int    getShape(){return(shape);}
   int    getSleep2(){return(sleep2);} 
   boolean getSeenPlayer(){return(seenPlayer);}
   
   void setX(double x){this.x=x;}        
   void setZ(double z){this.z=z;}       
   void setDir(double dir){this.dir=dir;}      
   void setSpeed(float speed){this.speed=speed;} 
   void setShape(int shape){this.shape=shape;}   
   void setSleep2(int sleep2){this.sleep2=sleep2;}  
   void setSeenPlayer(boolean seenPlayer){this.seenPlayer=seenPlayer;}
}
