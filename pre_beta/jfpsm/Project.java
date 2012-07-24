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
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import misc.SerializationHelper;

/**
 * Instance of a game, it contains a container of floors and a container of tiles.
 * It is saved as a ZIP archive that contains an XML file for most of the data and the image files.
 * @author Julien Gouesse
 *
 */
public final class Project extends JFPSMUserObject{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Project.class);}
	
    private static final long serialVersionUID = 1L;
    
    private static final String fileExtension = ".jfpsm.zip";
    
    private LevelSet levelSet;

    private TileSet tileSet;
    
    public Project(){
    	this("");
    }
    
    public Project(String name){
        super(name);
        levelSet=new LevelSet("Level Set");
        tileSet=new TileSet("Tile Set");
        markDirty();
    }
    
    
    @Override
    public final boolean equals(Object o){
        boolean result;
        if(o==null||!(o instanceof Project))
            result=false;
        else
            {String name=getName();
             String otherName=((Project)o).getName();
             if(name==null)
                 result=otherName==null;
             else
                 result=name.equals(otherName);
            }
        return(result);
    }
    
    @Override
    public final boolean isDirty(){
        return(levelSet.isDirty()||tileSet.isDirty());
    }
    
    @Override
    public final void unmarkDirty(){}
    
    @Override
    public final void markDirty(){}
    
    public final LevelSet getLevelSet(){
		return(levelSet);
	}

	public final void setLevelSet(LevelSet levelSet){
		this.levelSet=levelSet;
	}

    public final TileSet getTileSet(){
        return(tileSet);
    }

    public final void setTileSet(TileSet tileSet){
        this.tileSet=tileSet;
    }
    
    public static final String getFileExtension(){
    	return(fileExtension);
    }
    
    /**
     * Gets the name of the project in this file if any
     * 
     * @param projectFile project file
     * @return project name from this file if it is a project file, otherwise <code>null</code>
     */
    public static final String getProjectNameFromFile(File projectFile){
    	/*String fullname=projectFile.getName();
    	String projectName=fullname.substring(0,fullname.length()-Project.getFileExtension().length());
    	return(projectName);*/
    	String projectName=null;
    	ZipFile zipFile=null;
    	try{zipFile=new ZipFile(projectFile);
    		ZipEntry entry=zipFile.getEntry("project.xml");
    		if(entry!=null)
    		    {CustomXMLDecoder decoder=new CustomXMLDecoder(zipFile.getInputStream(entry));
                 Object decodedObject=decoder.readObject();
                 decoder.close();
                 if(decodedObject!=null&&decodedObject instanceof Project)
                     {Project project=(Project)decodedObject;
                      projectName=project.getName();
                     }
    		    }
    	}
    	catch(ZipException ze)
    	{}
    	catch(IOException ioe)
    	{}
    	finally
        {if(zipFile!=null)
		     try{zipFile.close();}
             catch (IOException ioe) 
			 {//ignores this exception
			 }
        }
        return(projectName);
    }
    
    @Override
    final boolean canInstantiateChildren(){
        return(false);
    }

    @Override
    final boolean isOpenable(){
        return(true);
    }

    @Override
    final boolean isRemovable(){
        return(true);
    }
}