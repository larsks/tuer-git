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

//tag structure for the .MD3 file format, used to link other
//models to rotate and transate the child models of that model
class MD3Tag{


    private String strName;//name of the tag (I.E. "tag_torso") [64]
    
    private Vector3D vPosition;//translation that should be performed
    
    private float[] rotation;//rotation matrix for this frame


    MD3Tag(MD3Model md3Model){
	strName=md3Model.byte2string(64);	
	md3Model.increaseFilePointer(64);
	vPosition=new Vector3D();
	vPosition.setX(md3Model.byte2float());
	vPosition.setY(md3Model.byte2float());
	vPosition.setZ(md3Model.byte2float());	
	rotation=new float[9];
	for(int i=0;i<9;i++)
	    rotation[i]=md3Model.byte2float();
    }
    
    
    String getStrName(){
        return(strName);
    }
    
    Vector3D getVPosition(){
        return(vPosition);
    }
    
    float[] getRotation(){
        return(rotation);
    }
}
