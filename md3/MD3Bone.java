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

// This stores the bone information (useless as far as I can see...)
class MD3Bone{


    private float[] mins;//min (x, y, z) value for the bone
    
    private float[] maxs;//max (x, y, z) value for the bone
    
    private float[] position;//bone position???
    
    private float scale;//scale of the bone
    
    private String creator;//modeler used to create the model (I.E. "3DS Max") [16]


    MD3Bone(MD3Model md3Model){
        mins = new float[3];
	maxs = new float[3];
	position = new float[3];
	mins[0]     = md3Model.byte2float();
	mins[1]     = md3Model.byte2float();
	mins[2]     = md3Model.byte2float();
	maxs[0]     = md3Model.byte2float();
	maxs[1]     = md3Model.byte2float();
	maxs[2]     = md3Model.byte2float();
	position[0] = md3Model.byte2float();
	position[1] = md3Model.byte2float();
	position[2] = md3Model.byte2float();
	scale	    = md3Model.byte2float();
	creator     = md3Model.byte2string(16);
	md3Model.increaseFilePointer(16);
    }
    
    
    float[] getMins(){
        return(mins);
    }
    
    float[] getMaxs(){
        return(maxs);
    }
    
    float[] getPosition(){
        return(position);
    }
    
    float getScale(){
        return(scale);
    }
    
    String getCreator(){
        return(creator);
    }
}
