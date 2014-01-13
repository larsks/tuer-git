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

/**
 * Data model of the 3D model converter, it contains some information
 * about the output file and the input file
 * 
 * @author Julien Gouesse
 *
 */
public class ModelConverter extends JFPSMToolUserObject{

	private static final long serialVersionUID=1L;
	
	private String convertibleModelFilePath;
	
	private String convertedModelDirectoryPath;
	
	private String convertedModelFilename;
	
	private ModelFileFormat convertedModelFileFormat;

	public ModelConverter(){
		this("");
	}
	
	public ModelConverter(final String name){
		super(name);
	}
	
	@Override
    public Viewer createViewer(final ToolManager toolManager){
    	return(new ModelConverterViewer(this,toolManager));
    }

	public String getConvertibleModelFilePath(){
		return(convertibleModelFilePath);
	}

	public void setConvertibleModelFilePath(String convertibleModelFilePath){
		this.convertibleModelFilePath=convertibleModelFilePath;
	}

	public String getConvertedModelDirectoryPath(){
		return(convertedModelDirectoryPath);
	}

	public void setConvertedModelDirectoryPath(String convertedModelDirectoryPath){
		this.convertedModelDirectoryPath=convertedModelDirectoryPath;
	}

	public String getConvertedModelFilename(){
		return(convertedModelFilename);
	}

	public void setConvertedModelFilename(String convertedModelFilename){
		this.convertedModelFilename=convertedModelFilename;
	}

	public ModelFileFormat getConvertedModelFileFormat(){
		return(convertedModelFileFormat);
	}

	public void setConvertedModelFileFormat(ModelFileFormat convertedModelFileFormat){
		this.convertedModelFileFormat=convertedModelFileFormat;
	}

	@Override
	public boolean isDirty(){
		return(false);
	}

	@Override
	public void markDirty(){}

	@Override
	public void unmarkDirty(){}

	@Override
	boolean isRemovable(){
		return(true);
	}

	@Override
	boolean isOpenable(){
		return(true);
	}

	@Override
	boolean canInstantiateChildren(){
		return(false);
	}
}
