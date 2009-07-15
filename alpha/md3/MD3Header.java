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

//header information read at the beginning of the file

class MD3Header{

 
    private String fileID;  //file ID - Must be "IDP3" [4]
    
    private int	version;    //file version - Must be 15
    
    private String strFile; //name of the file [68]
    
    private int	numFrames;  //number of animation frames
    
    private int	numTags;    //tag count
    
    private int	numMeshes;  //number of sub-objects in the mesh
    
    private int	numMaxSkins;//number of skins for the mesh
    
    private int	headerSize; //mesh header size
    
    private int	tagStart;   //offset into the file for tags
    
    private int	tagEnd;	    //end offset into the file for tags
    
    private int	fileSize;   //file size
    

    MD3Header(MD3Model md3Model){
	fileID	= md3Model.byte2string(4);
	md3Model.increaseFilePointer(4);
	version	= md3Model.byte2int();
	strFile	= md3Model.byte2string(68);
	md3Model.increaseFilePointer(68);
	numFrames= md3Model.byte2int();
	numTags	= md3Model.byte2int();
	numMeshes= md3Model.byte2int();
	numMaxSkins= md3Model.byte2int();
	headerSize= md3Model.byte2int();
	tagStart= md3Model.byte2int();
	tagEnd	= md3Model.byte2int();
	fileSize= md3Model.byte2int();
    } 
    
    
    String getFileID(){
        return(fileID);
    }  
    
    int	getVersion(){
        return(version);
    }
    
    String getStrFile(){
        return(strFile);
    }
    
    int	getNumFrames(){
        return(numFrames);
    }
    
    int	getNumTags(){
        return(numTags);
    }
    
    int	getNumMeshes(){
        return(numMeshes);
    }
    
    int	getNumMaxSkins(){
        return(numMaxSkins);
    }
    
    int	getHeaderSize(){
        return(headerSize);
    }
    
    int	getTagStart(){
        return(tagStart);
    }
    
    int	getTagEnd(){
        return(tagEnd);
    }
    
    int	getFileSize(){
        return(fileSize);
    }      
}
