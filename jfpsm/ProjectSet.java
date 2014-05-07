/**
 * Copyright (c) 2006-2014 Julien Gouesse
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
import misc.SerializationHelper;

/**
 * Container of projects, it is a kind of workspace.
 * @author Julien Gouesse
 *
 */
public final class ProjectSet extends JFPSMProjectUserObject{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(ProjectSet.class);}
	
    private static final ProjectFileFilter projectFileFilter=new ProjectFileFilter();
	
	private static final long serialVersionUID=1L;
    
    private ArrayList<Project> projectsList;
    
    private transient boolean dirty;
    
    private transient File workspaceDirectory;
    
    
    public ProjectSet(){
    	this("");
    }
    
    public ProjectSet(String name){
        super(name);
        projectsList=new ArrayList<>();
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
        return(createRawDataPath(projectName)+Project.getFileExtension());
    }
    
    final String createRawDataPath(String name){
        return(workspaceDirectory.getAbsolutePath()+System.getProperty("file.separator")+name);
    }
    
    /**
     * Gets the project file (in the workspace) whose name is the supplied name
     * 
     * @param projectName name of the project whose file is searched
     * @return project file
     */
    final File getProjectFileFromName(String projectName){
    	//File projectFile=new File(createProjectPath(projectName));
    	File projectFile=null;
    	File[] files=getProjectFiles();
    	if(projectName!=null)
    	    for(int i=0;i<files.length&&projectFile==null;i++)
                {final String currentProjectName=Project.getProjectNameFromFile(files[i]);
                 if(projectName.equals(currentProjectName))
            	     projectFile=files[i];
                }
    	return(projectFile);
    }
    
    final void removeProject(Project project){
    	if(project==null)
    		throw new IllegalArgumentException("A null project cannot be removed!");
    	else
    	    {if(projectsList.remove(project))
                 {dirty=true;
                  final File projectFile=getProjectFileFromName(project.getName());
                  //deletes the corresponding file if any
                  if(projectFile!=null&&projectFile.exists())
           	          projectFile.delete();
                 }
    	    }
    }
    
    final void saveProject(Project project){
    	final String projectName=project.getName();
    	File projectFile=getProjectFileFromName(projectName);
    	//if there is not yet a project file for this project
    	if(projectFile==null)
    	    {//creates a new file with the default filename
    		 final String projectPath=createProjectPath(projectName);
    		 projectFile=new File(projectPath);
    		 //attempts to avoid overwriting an existing file
    		 for(int index=0;index<Integer.MAX_VALUE&&projectFile.exists();index++)
    			 {final String tmpPath=createProjectPath(projectName+"_"+Integer.toString(index));
    			  projectFile=new File(tmpPath);
    			 }
    		 if(projectFile.exists())
    			 throw new RuntimeException("The project "+project.getName()+" cannot be saved!");
    	    }
    	saveProject(project,projectFile);
    }
    
    final void saveProject(Project project,final File file){
        if(projectsList.contains(project))
    	    {//this should be tested rather in the GUI
    	     //checks if the internal project has unsaved modifications or 
    	     //if it is an external (exported) project
    		 final File parentFile=file.getParentFile();
        	 if(project.isDirty()||parentFile==null||!parentFile.equals(workspaceDirectory))
    	         {File tmpFile=null;
    			  //saves the project (project.xml and image files have to be put into a ZIP file)
          	      try{if(!file.exists())
          	              {if(!file.createNewFile())
          	                   throw new IOException("cannot create file "+file.getAbsolutePath());
          	              }
          	          //creates a temporary file used as a buffer before putting the data into the archive
          	          tmpFile=File.createTempFile("JFPSM",".tmp");
          	          try(ZipOutputStream zoStream=new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
          	              zoStream.setMethod(ZipOutputStream.DEFLATED);
          	              //creates a ZipEntry for the XML file
          	              ZipEntry projectXMLEntry=new ZipEntry("project.xml");
          	              projectXMLEntry.setMethod(ZipEntry.DEFLATED);
          	              //puts it into the ZipOutputStream
          	              zoStream.putNextEntry(projectXMLEntry);
          	              //creates, uses and closes an XMLEncoder
          	              /**
          	               * Actually, it "should" be possible to use the existing ZIP output stream to avoid creating a
          	               * temporary file by flushing the encoder to avoid closing this stream but it throws a 
          	               * SAX exception. It is necessary to close the XML node by doing zoStream.write("</java> \n".getBytes());
          	               */
          	              try(CustomXMLEncoder encoder=new CustomXMLEncoder(new BufferedOutputStream(new FileOutputStream(tmpFile)))){
          	                  encoder.writeObject(project);
          	              }
          	              //copies the temporary file into the zip entry         	          
          	              int bytesIn;
          	              byte[] readBuffer=new byte[1024];
          	              try(FileInputStream fis=new FileInputStream(tmpFile)){
          	                  while((bytesIn=fis.read(readBuffer))!=-1) 
          	                      zoStream.write(readBuffer,0,bytesIn);
          	              }
          	              ZipEntry entry;
          	              String floorDirectory;
          	              for(FloorSet floorSet:project.getLevelSet().getFloorSetsList())
          	        	      for(Floor floor:floorSet.getFloorsList())
          	                      {floorDirectory="levelset/"+floorSet.getName()+"/"+floor.getName()+"/";
          	                       //saves each map
          	                       for(MapType type:MapType.values())
          	                           {entry=new ZipEntry(floorDirectory+type.getFilename());
              	                        entry.setMethod(ZipEntry.DEFLATED);
              	                        zoStream.putNextEntry(entry);
              	                        ImageIO.write(floor.getMap(type).getImage(),"png",zoStream);
          	                           }
          	                      }
          	              final String tileDirectory="tileset/";
          	              for(Tile tile:project.getTileSet().getTilesList())
          	        	      for(int textureIndex=0;textureIndex<tile.getMaxTextureCount();textureIndex++)
          	                      if(tile.getTexture(textureIndex)!=null)
          	                          {entry=new ZipEntry(tileDirectory+tile.getName()+textureIndex+".png");
          	                           entry.setMethod(ZipEntry.DEFLATED);
                                       zoStream.putNextEntry(entry);
                                       ImageIO.write(tile.getTexture(textureIndex),"png",zoStream);
          	                          }
          	              }
          	         }
          	      catch(IOException ioe)
          	      {throw new RuntimeException("The project "+project.getName()+" cannot be saved!",ioe);}
          	      finally
          	      {//deletes the temporary file
          	       if(tmpFile!=null)
          	           tmpFile.delete();
          	      }
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
    	//ensures that the workspace directory exists and is writable
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
     * @return files of the projects in the file system (in the workspace)
     */
    final File[] getProjectFiles(){
        File[] files=workspaceDirectory.listFiles(projectFileFilter);
        return(files==null?new File[0]:files);
    }
    
    /**
     * Gets the names of the projects in the file system. It may contain duplicates
     * 
     * @return names of the projects in the file system
     */
    final String[] getProjectNames(){
        final File[] files=getProjectFiles();
        final String[] names=new String[files.length];
        for(int i=0;i<names.length;i++)
            names[i]=Project.getProjectNameFromFile(files[i]);
        return(names);
    }
    
    /**
     * Loads a project from a file
     * 
     * @param projectFile project file
     * @return the newly loaded project or the previous one if it had been already loaded
     */
    final Project loadProject(File projectFile){
        final String fullname=projectFile.getName();
        Project project=null;
        if(projectFile.getName().endsWith(Project.getFileExtension()))
            {final int nameLength=fullname.length();
             final String projectName=fullname.substring(0,nameLength-Project.getFileExtension().length());
             try(ZipFile zipFile=new ZipFile(projectFile)){
                 ZipEntry entry;
                 //at first, gets the file project.xml to build the project object as soon as possible
                 entry=zipFile.getEntry("project.xml");
                 if(entry!=null)
                     {Object decodedObject=null;
                	  try(CustomXMLDecoder decoder=new CustomXMLDecoder(zipFile.getInputStream(entry))){
                          decodedObject=decoder.readObject();
                      }
                      if(decodedObject!=null&&decodedObject instanceof Project)
                          {project=(Project)decodedObject;
                           Enumeration<? extends ZipEntry> entries=zipFile.entries();
                           BufferedImage imageMap;
                           String[] path;
                           int textureIndex;
                           String textureIndexString;
                           while(entries.hasMoreElements())
                               {entry=entries.nextElement();
                                if(!entry.getName().equals("project.xml"))
                                    {if(!entry.isDirectory())
                                         {path=entry.getName().split("/");
                                          if(path.length==4&&path[0].equals("levelset"))
                                              {//finds the floor that should contain this map
                                               for(FloorSet floorSet:project.getLevelSet().getFloorSetsList())
                                      	           for(Floor floor:floorSet.getFloorsList())
                                                       if(path[1].equals(floorSet.getName())&&path[2].equals(floor.getName()))
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
                                              if(path.length==2&&path[0].equals("tileset"))
                                                  {//find the tile that should contain this file
                                                   for(Tile tile:project.getTileSet().getTilesList())
                                                       if(path[1].startsWith(tile.getName())&&path[1].endsWith(".png"))
                                                           {textureIndex=-1;
                                                            textureIndexString=path[1].substring(tile.getName().length(),path[1].lastIndexOf(".png"));
                                                            try{textureIndex=Integer.parseInt(textureIndexString);}
                                                            catch(NumberFormatException nfe)
                                                            {//ignore this exception as it might happen and it is not a problem                                                  
                                                            }
                                                            if(textureIndex!=-1)
                                                     	        tile.setTexture(textureIndex,ImageIO.read(zipFile.getInputStream(entry)));                     
                                                           }
                                                  }
                                         }
                                    }
                               }
                           //if the project was already loaded, return 
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
                    	  throw new IllegalArgumentException("The file named \"project.xml\" does not contain a valid project!");
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