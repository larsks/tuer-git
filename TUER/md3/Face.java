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

// This is our face structure.  This is is used for indexing into the vertex 
// and texture coordinate arrays.  From this information we know which vertices
// from our vertex array go to which face, along with the correct texture coordinates.
class Face{
   
   
    private int[] vertIndex;// indicies for the verts that make up this triangle
    
    private int[] coordIndex;// indicies for the tex coords to texture this face
   
    
    Face(){
        vertIndex = new int[3];
	coordIndex = new int[3];
    }
    
    
    int[] getVertIndex(){
        return(vertIndex);
    }
    
    int[] getCoordIndex(){
        return(coordIndex);
    }
    
    void setVertIndex(int index,int value){
        this.vertIndex[index]=value;
    }
    
    void setCoordIndex(int index,int value){
        this.coordIndex[index]=value;
    }
}
