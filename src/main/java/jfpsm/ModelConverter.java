/**
 * Copyright (c) 2006-2015 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jfpsm;

import common.ModelFileFormat;

/**
 * Data model of the 3D model converter, it contains some information
 * about the output file and the input file
 * 
 * @author Julien Gouesse
 *
 */
public class ModelConverter extends JFPSMToolUserObject{

	private static final long serialVersionUID=1L;
	
	private transient boolean dirty;
	
	private String convertibleModelFilePath;
	
	private String convertedModelDirectoryPath;
	
	private String convertedModelFilename;
	
	private ModelFileFormat convertedModelFileFormat;

	public ModelConverter(){
		this("");
	}
	
	public ModelConverter(final String name){
		super(name);
		dirty=true;
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
		dirty=true;
	}

	public String getConvertedModelDirectoryPath(){
		return(convertedModelDirectoryPath);
	}

	public void setConvertedModelDirectoryPath(String convertedModelDirectoryPath){
		this.convertedModelDirectoryPath=convertedModelDirectoryPath;
		dirty=true;
	}

	public String getConvertedModelFilename(){
		return(convertedModelFilename);
	}

	public void setConvertedModelFilename(String convertedModelFilename){
		this.convertedModelFilename=convertedModelFilename;
		dirty=true;
	}

	public ModelFileFormat getConvertedModelFileFormat(){
		return(convertedModelFileFormat);
	}

	public void setConvertedModelFileFormat(ModelFileFormat convertedModelFileFormat){
		this.convertedModelFileFormat=convertedModelFileFormat;
		dirty=true;
	}

	@Override
	public boolean isDirty(){
		return(dirty);
	}

	@Override
    public final void unmarkDirty(){
        dirty=false;
    }
    
    @Override
    public final void markDirty(){
        dirty=true;
    }

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
