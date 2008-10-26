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

// This stores the normals and vertex indices 
class MD3Triangle{


    private short[] vertex;// The vertex for this face (scale down by 64.0f) (short [3])
    
    private int[] normal;// This stores some crazy normal values (not sure...) (char [2])


    MD3Triangle(MD3Model md3Model){
        vertex = new short[3];
	normal = new int[2];
	vertex[0]=md3Model.byte2short();
	vertex[1]=md3Model.byte2short();
	vertex[2]=md3Model.byte2short();
	normal[0]=md3Model.byte2byte();
	normal[1]=md3Model.byte2byte();
    }
    
    
    short[] getVertex(){
        return(vertex);
    }
    
    int[] getNormal(){
        return(normal);
    }
}
