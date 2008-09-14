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

package md3;

// This is our 3D point class.  This will be used to store the vertices of our model.

class Vector3D{
   
   
   private float x;
   
   private float y;
   
   private float z;
   
   
   Vector3D(){
       this(0,0,0);
   }
   
   Vector3D(float x,float y,float z){
       this.x=x;		
       this.y=y;		
       this.z=z;
   }
   
   
   float getX(){
       return(x);
   }
   
   float getY(){
       return(y);
   }
   
   float getZ(){
       return(z);
   }
   
   void setX(float x){
       this.x=x;
   }
   
   void setY(float y){
       this.y=y;
   }
   
   void setZ(float z){
       this.z=z;
   }
}
