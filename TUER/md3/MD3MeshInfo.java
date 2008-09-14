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

//structure used to read in the mesh data for the .md3 models

class MD3MeshInfo{


    private String meshID;	//mesh ID (We don't care) [4]
    
    private String strName;	//mesh name (We do care) [68]
    
    private int	numMeshFrames;	//mesh aniamtion frame count
    
    private int	numSkins;	//mesh skin count
    
    private int numVertices;	//mesh vertex count
    
    private int	numTriangles;	//mesh face count
    
    private int	triStart;	//starting offset for the triangles
    
    private int	headerSize;	//header size for the mesh
    
    private int uvStart;	//starting offset for the UV coordinates
    
    private int	vertexStart;	//starting offset for the vertex indices
    
    private int	meshSize;	//total mesh size


    MD3MeshInfo(MD3Model md3Model){
	meshID	= md3Model.byte2string(4);
	md3Model.increaseFilePointer(4);
	strName	= md3Model.byte2string(68);
	md3Model.increaseFilePointer(68);
	numMeshFrames= md3Model.byte2int();
	numSkins= md3Model.byte2int();
	numVertices= md3Model.byte2int();
	numTriangles= md3Model.byte2int();
	triStart= md3Model.byte2int();
	headerSize= md3Model.byte2int();
	uvStart	= md3Model.byte2int();
	vertexStart= md3Model.byte2int();
	meshSize= md3Model.byte2int();
    }
    
    
    String getMeshID(){
        return(meshID);
    }
    
    String getStrName(){
        return(strName);
    }
    
    int	getNumMeshFrames(){
        return(numMeshFrames);
    }
    
    int	getNumSkins(){
        return(numSkins);
    }
    
    int getNumVertices(){
        return(numVertices);
    }
    
    int	getNumTriangles(){
        return(numTriangles);
    }
    
    int	getTriStart(){
        return(triStart);
    }
    
    int	getHeaderSize(){
        return(headerSize);
    }
    
    int getUvStart(){
        return(uvStart);
    }
    
    int	getVertexStart(){
        return(vertexStart);
    }
    
    int	getMeshSize(){
        return(meshSize);
    }
}
