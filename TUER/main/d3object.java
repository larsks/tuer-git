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
   private short  speed;    // if moving, otherwise alive indicator
   private int    shape;    // or the type of object
   private short  face;     // if multi-image object: which face is shown
   private short  faceskip; // delay counter for animation
   private int    ianim;    // animation counter, for bots
   private int    sleep;    // in frames
   private int    sleep2;   // suspend walking this long
   private int    idamage;  // if >0, bot was hit once
   private boolean seenPlayer;
   
   double getX(){return(x);}        
   double getZ(){return(z);}        
   double getDir(){return(dir);}      
   short  getSpeed(){return(speed);}    
   int    getShape(){return(shape);}    
   short  getFace(){return(face);}     
   short  getFaceskip(){return(faceskip);} 
   int    getIanim(){return(ianim);}
   int    getSleep(){return(sleep);}    
   int    getSleep2(){return(sleep2);}   
   int    getIdamage(){return(idamage);}  
   boolean getSeenPlayer(){return(seenPlayer);}
   
   void setX(double x){this.x=x;}        
   void setZ(double z){this.z=z;}       
   void setDir(double dir){this.dir=dir;}      
   void setSpeed(short speed){this.speed=speed;} 
   void setShape(int shape){this.shape=shape;}    
   void setFace(short face){this.face=face;}     
   void setFaceskip(short faceskip){this.faceskip=faceskip;} 
   void setIanim(int ianim){this.ianim=ianim;}
   void setSleep(int sleep){this.sleep=sleep;}    
   void setSleep2(int sleep2){this.sleep2=sleep2;}   
   void setIdamage(int idamage){this.idamage=idamage;}  
   void setSeenPlayer(boolean seenPlayer){this.seenPlayer=seenPlayer;}
}
