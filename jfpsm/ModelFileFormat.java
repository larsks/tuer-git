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
package jfpsm;

import java.io.File;

/**
 * model file format supported by JFPSM
 * 
 * @author Julien Gouesse
 *
 */
public enum ModelFileFormat{
	ARDOR3D_BINARY("Ardor3D Binary",".abin"),
	ARDOR3D_XML("Ardor3D XML",".axml"),
	COLLADA("Collada",".dae"),
	MD2("MD2",".md2"),
	MD3("MD3",".md3"),
	WAVEFRONT_OBJ("WaveFront OBJ",".obj");
	
	private final String description;
	
	private final String extension;
	
    private ModelFileFormat(final String description,final String extension){
		this.description=description;
		this.extension=extension;
	}
    
    public final String getDescription(){
    	return(description);
    }
    
    public final String getExtension(){
    	return(extension);
    }
    
    @Override
    public final String toString(){
    	return(getDescription());
    }
    
    public static ModelFileFormat get(final String filePath){
		ModelFileFormat modelFileFormat=null;
		if(filePath!=null)
		    {final String modelFileExtension=filePath.substring(filePath.lastIndexOf('.'));
		     for(ModelFileFormat currentModelFileFormat:ModelFileFormat.values())
	    	     if(modelFileExtension.equals(currentModelFileFormat.getExtension()))
	                 {modelFileFormat=currentModelFileFormat;
	                  break;
	                 }
		    }
		return(modelFileFormat);
	}
}