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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

/**
 * Container of projects, it is a kind of workspace.
 * @author Julien Gouesse
 *
 */
public final class ProjectSet extends JFPSMUserObject{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(ProjectSet.class);}
	
    private static final ProjectFileFilter projectFileFilter=new ProjectFileFilter();
	
	private static final long serialVersionUID = 1L;
    
    private ArrayList<Project> projectsList;
    
    private transient boolean dirty;
    
    private transient File workspaceDirectory;
    
    
    public ProjectSet(){
    	this("");
    }
    
    public ProjectSet(String name){
        super(name);
        projectsList=new ArrayList<Project>();
        dirty=true;
        initializeWorkspaceDirectory();
    }
    
    
    final boolean addProject(Project project){
        final boolean success;
        if(success=!projectsList.contains(project))
            {projectsList.add(project);
             dirty=true;
            }
        return(success);
    }
    
    final String createProjectPath(String projectName){
        return(workspaceDirectory.getAbsolutePath()+System.getProperty("file.separator")+projectName+Project.getFileExtension());
    }
    
    final void removeProject(Project project){
        if(projectsList.remove(project))
            {dirty=true;
             final File projectFile=new File(createProjectPath(project.getName()));
             //delete the corresponding file if any
             if(projectFile.exists())
           	     projectFile.delete();
            }
    }
    
    final void saveProject(Project project){
    	if(projectsList.contains(project))
    	    {//this should be tested rather in the GUI
    		 if(project.isDirty())
    	         {final String projectPath=createProjectPath(project.getName());
          	      final File projectFile=new File(projectPath);
          	      //save the project (project.xml and image files have to be put into a ZIP file)
          	      try{if(!projectFile.exists())
          	              {if(!projectFile.createNewFile())
          	                   throw new IOException("cannot create file "+projectPath);
          	              }
          	          //create a temporary file used as a buffer before putting the data into the archive
          	          File tmpFile=File.createTempFile("JFPSM",".tmp");
          	          ZipOutputStream zoStream=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(projectFile)));
          	          zoStream.setMethod(ZipOutputStream.DEFLATED);
          	          //create a ZipEntry for the XML file
          	          ZipEntry projectXMLEntry=new ZipEntry("project.xml");
          	          projectXMLEntry.setMethod(ZipEntry.DEFLATED);
          	          //put it into the ZipOutputStream
          	          zoStream.putNextEntry(projectXMLEntry);
          	          //create, use and close an XMLEncoder
          	          CustomXMLEncoder encoder=new CustomXMLEncoder(new BufferedOutputStream(new FileOutputStream(tmpFile)));
          	          encoder.writeObject(project);
          	          encoder.close();
          	          //copy the temporary file into the zip entry         	          
          	          int bytesIn;
          	          byte[] readBuffer=new byte[1024];
          	          FileInputStream fis=new FileInputStream(tmpFile);
          	          while((bytesIn=fis.read(readBuffer))!=-1) 
          	              zoStream.write(readBuffer,0,bytesIn);         	          
          	          fis.close();    
          	          ZipEntry floorEntry;
          	          String floorDirectory;
          	          String floorSetName=project.getFloorSet().getName();
          	          for(Floor floor:project.getFloorSet().getFloorsList())
          	              {floorDirectory="levelset/"+floorSetName+"/"+floor.getName()+"/";
          	               //save each map
          	               for(MapType type:MapType.values())
          	                   {floorEntry=new ZipEntry(floorDirectory+type.getFilename());
              	                floorEntry.setMethod(ZipEntry.DEFLATED);
              	                zoStream.putNextEntry(floorEntry);
              	                ImageIO.write(floor.getMap(type).getImage(),"png",zoStream);          	            	    
          	                   }
          	              }         	          
          	          //close the ZipOutputStream
          	          zoStream.close();
          	          //delete the temporary file
          	          tmpFile.delete();
          	         }
          	      catch(IOException ioe)
          	      {throw new RuntimeException("The project "+project.getName()+" cannot be saved!",ioe);}
    	         }   		 
    	    }
    	else
    		throw new IllegalArgumentException("The project "+project.getName()+" is not handled by this project set!");
    }

    @Override
    public final boolean isDirty(){
        boolean dirty=this.dirty;
        if(!dirty)
            for(Project project:projectsList)
                if(project.isDirty())
                    {dirty=true;
                     break;
                    }
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

    public final ArrayList<Project> getProjectsList(){
        return(projectsList);
    }

    public final void setProjectsList(ArrayList<Project> projectsList){
        this.projectsList=projectsList;
    }
    
    private final void initializeWorkspaceDirectory(){
    	//ensure the workspace directory exists and is writable
        workspaceDirectory=new File(System.getProperty("user.home")+System.getProperty("file.separator")+"jfpsm");       
        if(!workspaceDirectory.exists()||!workspaceDirectory.isDirectory())
            workspaceDirectory.mkdir();
        if(!workspaceDirectory.exists()||!workspaceDirectory.isDirectory()||!workspaceDirectory.canRead()||!workspaceDirectory.canWrite())
        	throw new RuntimeException("The workspace directory "+workspaceDirectory.getAbsolutePath()+" cannot be used!");
    }
    
    @Override
    public final void resolve(){
        initializeWorkspaceDirectory();
    }
    
    /**
     * 
     * @return files of the projects in the file system
     */
    final File[] getProjectFiles(){
        File[] files=workspaceDirectory.listFiles(projectFileFilter);
        return(files==null?new File[0]:files);
    }
    
    /**
     * 
     * @return names of the projects in the file system
     */
    final String[] getProjectNames(){
        File[] files=getProjectFiles();
        String[] names=new String[files.length];
        String fullname;
        for(int i=0;i<names.length;i++)
            {fullname=files[i].getName();
             names[i]=fullname.substring(0,fullname.length()-Project.getFileExtension().length());
            }
        return(names);
    }
    
    /**
     * load a project from a file
     * @param projectFile project file
     * @return the newly loaded project or the previous one if it had been already loaded
     */
    final Project loadProject(File projectFile){
        String fullname=projectFile.getName();
        Project project=null;
        if(projectFile.getName().endsWith(Project.getFileExtension()))
            {int nameLength=fullname.length();
             String projectName=fullname.substring(0,nameLength-Project.getFileExtension().length());     
             try{ZipFile zipFile=new ZipFile(projectFile);
                 Enumeration<? extends ZipEntry> entries = zipFile.entries();
                 ZipEntry entry;
                 BufferedImage imageMap;
                 String[] path;
                 while(entries.hasMoreElements())
                     {entry=entries.nextElement();
                      if(entry.getName().equals("project.xml"))
                          {CustomXMLDecoder decoder=new CustomXMLDecoder(zipFile.getInputStream(entry));
                           project=(Project)decoder.readObject();
                           decoder.close();
                          }
                      else
                          {if(!entry.isDirectory())
                               {path=entry.getName().split("/");
                                if(path.length==4&&path[0].equals("levelset"))
                                    {//find the floor that should contain this map
                                     String floorSetName=project.getFloorSet().getName();
                                     for(Floor floor:project.getFloorSet().getFloorsList())
                                         if(path[1].equals(floorSetName)&&path[2].equals(floor.getName()))
                                             {imageMap=ImageIO.read(zipFile.getInputStream(entry));
                                              for(MapType type:MapType.values())
                                                  if(path[3].equals(type.getFilename()))
                                                      {floor.getMap(type).setImage(imageMap);
                                                       break;
                                                      }                                              
                                              break;
                                             }
                                    }
                                else
                                    if(path.length>=3&&path[0].equals("tileset"))
                                        {//find the tile that should contain this file
                                         for(Tile tile:project.getTileSet().getTilesList())
                                             if(path[1].equals(tile.getName()))
                                                 {
                                                  
                                                  //load the tile
                                                 }                      
                                        }
                               }
                          }
                     }
                 if(project!=null)
                     {//if the project was already loaded, return 
                      //the project previously created with this name
                      if(!addProject(project))
                          {for(Project existingProject:projectsList)
                               if(existingProject.getName().equals(projectName))
                                   {//FIXME: handle conflicts
                                    project=existingProject;
                                   }
                          }
                     }
                 else
                     throw new IllegalArgumentException("The project file "+fullname+" does not contain any file named \"project.xml\"!");
                } 
             catch(Throwable throwable)
             {throw new RuntimeException("The project "+projectName+" cannot be loaded!",throwable);}            
            }
        else
            throw new IllegalArgumentException("The file "+fullname+" is not a JFPSM project file!");
        return(project);
    }
    
    @Override
    final boolean canInstantiateChildren(){
        return(true);
    }

    @Override
    final boolean isOpenable(){
        //it is always open and it cannot be closed
        return(false);
    }

    @Override
    final boolean isRemovable(){
        return(false);
    }
}