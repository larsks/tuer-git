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

// This holds all the information for our model/scene. 
// You should eventually turn into a robust class that 
// has loading/drawing/querying functions like:
// LoadModel(...); DrawObject(...); DrawModel(...); DestroyModel(...);
class Object3D{


    private int  numOfVerts;	// The number of verts in the model
    
    private int  numOfFaces;	// The number of faces in the model
    
    private int  numTexVertex;	// The number of texture coordinates
    
    private int  materialID;	// The texture ID to use, which is the index into our texture array
    
    private boolean bHasTexture;// This is TRUE if there is a texture map for this object
    
    private String strName;	// The name of the object
    
    private Vector3D[]  pVerts;	// The object's vertices
    
    private Vector3D[]  pNormals;// The object's normals
    
    private Vector2D[]  pTexVerts;// The texture's UV coordinates
    
    private Face[] pFaces;	  // The faces information of the object
    
    
    Object3D(){}
    
    
    int getNumOfVerts(){
        return(numOfVerts);
    }
    
    int getNumOfFaces(){
        return(numOfFaces);
    }
    
    int getNumTexVertex(){
        return(numTexVertex);
    }
    
    int getMaterialID(){
        return(materialID);
    }
    
    boolean getBHasTexture(){
        return(bHasTexture);
    }
    
    String getStrName(){
        return(strName);
    }
    
    Vector3D[] getPVerts(){
        return(pVerts);
    }
    
    Vector3D[] getPNormals(){
        return(pNormals);
    }
    
    Vector2D[] getPTexVerts(){
        return(pTexVerts);
    }
    
    Face[] getPFaces(){
        return(pFaces);
    }
    
    void setNumOfVerts(int numOfVerts){
        this.numOfVerts=numOfVerts;
    }
    
    void setNumOfFaces(int numOfFaces){
        this.numOfFaces=numOfFaces;
    }
    
    void setNumTexVertex(int numTexVertex){
        this.numTexVertex=numTexVertex;
    }
    
    void setMaterialID(int materialID){
        this.materialID=materialID;
    }
    
    void setBHasTexture(boolean bHasTexture){
        this.bHasTexture=bHasTexture;
    }
    
    void setStrName(String strName){
        this.strName=strName;
    }
    
    void setPVerts(Vector3D[] pVerts){
        this.pVerts=pVerts;
    }
    
    void setPVerts(int index,Vector3D pVerts){
        this.pVerts[index]=pVerts;
    }
    
    void setPNormals(Vector3D[] pNormals){
        this.pNormals=pNormals;
    }
    
    void setPTexVerts(Vector2D[] pTexVerts){
        this.pTexVerts=pTexVerts;
    }
    
    void setPTexVerts(int index,Vector2D pTexVerts){
        this.pTexVerts[index]=pTexVerts;
    }
    
    void setPFaces(Face[] pFaces){
        this.pFaces=pFaces;
    }
    
    void setPFaces(int index,Face pFaces){
        this.pFaces[index]=pFaces;
    }
}
