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

// This holds the information for a material.  It may be a texture map of a color.
// Some of these are not used, but I left them because you will want to eventually
// read in the UV tile ratio and the UV tile offset for some models.
class MaterialInfo{


    private String  strName;// The texture name

    private String  strFile;// The texture file name (If this is set it's a texture map)

    private byte[]  color;// The color of the object (R, G, B)

    private int   textureId;// the texture ID

    private float uTile;// u tiling of texture  (Currently not used)

    private float vTile;// v tiling of texture	(Currently not used)

    private float uOffset;// u offset of texture	(Currently not used)

    private float vOffset;// v offset of texture	(Currently not used)


    MaterialInfo(){
	color=new byte[3];
    }
   
   
    String  getStrName(){
        return(strName);
    }

    String  getStrFile(){
        return(strFile);
    }
    
    byte[]  getColor(){
        return(color);
    }

    int getTextureId(){
        return(textureId);
    }

    float getUTile(){
        return(uTile);
    }

    float getVTile(){
        return(vTile);
    }

    float getUOffset(){
        return(uOffset);
    }

    float getVOffset(){
        return(vOffset);
    }
    
    void setStrName(String strName){
        this.strName=strName;
    }
        
    void setStrFile(String strFile){
        this.strFile=strFile;
    }    
    
    void setColor(byte[] color){
        this.color=color;
    }

    void  setTextureId(int textureId){
        this.textureId=textureId;
    }

    void setUTile(float uTile){
        this.uTile=uTile;
    }

    void setVTile(float vTile){
        this.vTile=vTile;
    }

    void setUOffset(float uOffset){
        this.uOffset=uOffset;
    }

    void setVOffset(float vOffset){
        this.vOffset=vOffset;
    }
}
