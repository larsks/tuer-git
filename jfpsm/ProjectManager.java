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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Panel that allows to manipulate the projects in a tree containing their
 * sub-components.
 * @author Julien Gouesse
 *
 */
public final class ProjectManager extends JPanel{

	
	private static final long serialVersionUID = 1L;
	
	private final JTree projectsTree;
	
	private final MainWindow mainWindow;

	
	/**
	 * build a project manager
	 * @param mainWindow window that contains this manager
	 */
	public ProjectManager(final MainWindow mainWindow){
		this.mainWindow=mainWindow;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));		
        final DefaultMutableTreeNode projectsRoot=new DefaultMutableTreeNode(new ProjectSet("Project Set"));
        projectsTree=new JTree(new DefaultTreeModel(projectsRoot));
        JScrollPane treePane=new JScrollPane(projectsTree);
        projectsTree.setShowsRootHandles(true);
        projectsTree.addTreeWillExpandListener(new TreeWillExpandListener(){

            @Override
            public final void treeWillCollapse(TreeExpansionEvent event)throws ExpandVetoException{             
                //prevent the user from collapsing the root
                if(((DefaultMutableTreeNode)event.getPath().getLastPathComponent()).getUserObject() instanceof ProjectSet)
                    throw new ExpandVetoException(event);
            }

            @Override
            public final void treeWillExpand(TreeExpansionEvent event)throws ExpandVetoException{}
        });
        projectsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        loadExistingProjects();
        //build the popup menu
        final JPopupMenu treePopupMenu=new JPopupMenu();
        final JMenuItem newMenuItem=new JMenuItem("New");
        final JMenuItem renameMenuItem=new JMenuItem("Rename");
        final JMenuItem importMenuItem=new JMenuItem("Import");        
        final JMenuItem exportMenuItem=new JMenuItem("Export");
        final JMenuItem refreshMenuItem=new JMenuItem("Refresh");       
        final JMenuItem openMenuItem=new JMenuItem("Open");        
        final JMenuItem closeMenuItem=new JMenuItem("Close");       
        final JMenuItem deleteMenuItem=new JMenuItem("Delete");       
        final JMenuItem saveMenuItem=new JMenuItem("Save");
        treePopupMenu.add(newMenuItem);
        treePopupMenu.add(renameMenuItem);
        treePopupMenu.add(importMenuItem);
        treePopupMenu.add(exportMenuItem);
        treePopupMenu.add(refreshMenuItem);
        treePopupMenu.add(openMenuItem);
        treePopupMenu.add(closeMenuItem);
        treePopupMenu.add(deleteMenuItem);
        treePopupMenu.add(saveMenuItem);
        //build action listeners
        newMenuItem.addActionListener(new CreateNewEntityFromSelectedEntityAction(this));
        renameMenuItem.addActionListener(new RenameSelectedEntityAction(this));
        importMenuItem.addActionListener(new ImportSelectedEntityAction(this));
        exportMenuItem.addActionListener(new ExportSelectedEntityAction(this));
        refreshMenuItem.addActionListener(new RefreshSelectedEntitiesAction(this));
        openMenuItem.addActionListener(new OpenSelectedEntitiesAction(this));
        closeMenuItem.addActionListener(new CloseSelectedEntitiesAction(this));
        deleteMenuItem.addActionListener(new DeleteSelectedEntitiesAction(this));
        saveMenuItem.addActionListener(new SaveSelectedEntitiesAction(this));
        projectsTree.setCellRenderer(new DefaultTreeCellRenderer(){
			
			private static final long serialVersionUID=1L;
			
			private ImageIcon coloredTileLeafIcon=null;
			
			private Icon defaultLeafIcon=null;

			@Override
			public final Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean selected, boolean expanded, boolean leaf, int row,
					boolean hasFocus) {
				if(leaf)
				    {DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
					 Object userObject=node.getUserObject();
					 if(defaultLeafIcon==null)
						 //get the default icon stored in the super class
						 defaultLeafIcon=super.getLeafIcon();
				     if(userObject instanceof Tile)
				    	 {int w=super.getLeafIcon().getIconWidth(),h=super.getLeafIcon().getIconHeight();
				    	  if(coloredTileLeafIcon==null)
				    		  {//build the image icon used to render the icon of the tile in the tree				    		   
				    		   BufferedImage coloredTileImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);				    		  
				    		   coloredTileLeafIcon=new ImageIcon(coloredTileImage);				    		   
				    		  }
				    	  //set the color
				    	  Tile tile=(Tile)userObject;
				    	  for(int x=0;x<w;x++)
				    		  for(int y=0;y<h;y++)
				    	          ((BufferedImage)coloredTileLeafIcon.getImage()).setRGB(x,y,tile.getColor().getRGB());
				    	  //set the colored icon
				    	  setLeafIcon(coloredTileLeafIcon);
				    	 }
				     else
				    	 setLeafIcon(defaultLeafIcon);
				    }
				return(super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus));
			}
		});
        projectsTree.addMouseListener(new MouseAdapter(){   
            
            @Override
            public final void mousePressed(MouseEvent e){
                handleMouseEvent(e);
            }
            
            @Override
            public final void mouseReleased(MouseEvent e){
                handleMouseEvent(e);
            }
            
            private final void handleMouseEvent(MouseEvent e){           	
                if(e.isPopupTrigger())
                    {//get all selected paths
                     TreePath[] paths=projectsTree.getSelectionPaths();
                	 TreePath mouseOverPath=projectsTree.getClosestPathForLocation(e.getX(),e.getY());
                     if(mouseOverPath!=null)
                         {//if no node is selected or if the node under the mouse pointer is not selected
                          //then select only the node under the mouse pointer and invalidate the previous selection
                          boolean found=false;
                          if(paths!=null)
                              for(TreePath path:paths)
                                  if(path.equals(mouseOverPath))
                                      {found=true;
                                       break;
                                      }
                          if(!found)
                              {//select the path on right click if none was already selected                      
                               projectsTree.setSelectionPath(mouseOverPath);
                               paths=new TreePath[]{mouseOverPath};
                              }                                                     
                         }
                     if(paths!=null)
                         {final boolean singleSelection=paths.length==1;
                          //get the first selected tree node
                          final TreePath path=projectsTree.getSelectionPath();
                          final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                          final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
                          final boolean showNew=singleSelection&&userObject.canInstantiateChildren();
                          final boolean showImport=singleSelection&&(userObject instanceof ProjectSet||userObject instanceof Map);
                          final boolean showExport=singleSelection&&userObject instanceof Project||userObject instanceof Map;
                          final boolean showRefresh=singleSelection&&userObject instanceof ProjectSet;
                          final boolean showRename=singleSelection&&(userObject instanceof Project||userObject instanceof Floor||userObject instanceof Tile);
                          final boolean showSave=singleSelection&&userObject instanceof Project;
                          boolean showOpenAndClose;
                          showOpenAndClose=false;
                          JFPSMUserObject currentUserObject;
                          for(TreePath currentPath:paths)
                              {currentUserObject=(JFPSMUserObject)((DefaultMutableTreeNode)currentPath.getLastPathComponent()).getUserObject();
                               if(currentUserObject.isOpenable())
                            	   {showOpenAndClose=true;
                            		break;
                            	   }
                              }            
                          boolean showDelete;
                          if(showNew)
                        	  showDelete=false;
                          else
                              {showDelete=false;
                               for(TreePath currentPath:paths)
                            	   {currentUserObject=(JFPSMUserObject)((DefaultMutableTreeNode)currentPath.getLastPathComponent()).getUserObject();
                            	    if(currentUserObject.isRemovable())
                            	        {showDelete=true;
                            		     break;
                            	        }
                            	   }
                              }                         
                    	  newMenuItem.setVisible(showNew);
                    	  renameMenuItem.setVisible(showRename);
                          importMenuItem.setVisible(showImport);
                          exportMenuItem.setVisible(showExport);
                          refreshMenuItem.setVisible(showRefresh);
                          openMenuItem.setVisible(showOpenAndClose);
                          closeMenuItem.setVisible(showOpenAndClose);
                          deleteMenuItem.setVisible(showDelete);
                          saveMenuItem.setVisible(showSave);
                          treePopupMenu.show(mainWindow.getApplicativeFrame(),e.getXOnScreen(),e.getYOnScreen());
                         }
                    }
                else
                	//double-click
                	if(e.getClickCount()==2)
                	    {final TreePath path=projectsTree.getSelectionPath();
                         final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                         final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();                 
                         if(userObject instanceof Floor||userObject instanceof Tile)
                             {Project project=(Project)((DefaultMutableTreeNode)selectedNode.getParent().getParent()).getUserObject();                                 
                              ProjectManager.this.mainWindow.getEntityViewer().openEntityView(userObject,project);                		 
                             }
                	    }
            }           
        });       
        add(treePane);       
	}
	
	/**
	 * get project names
	 * @return all project names in the tree
	 */
	private final ArrayList<String> getAllProjectNames(){
        return(getAllChildrenNames((DefaultMutableTreeNode)projectsTree.getModel().getRoot()));
    }
    
    private final ArrayList<String> getAllChildrenNames(DefaultMutableTreeNode parentNode){
        ArrayList<String> namesList=new ArrayList<String>();
        final int size=parentNode.getChildCount();
        for(int index=0;index<size;index++)
            namesList.add(((DefaultMutableTreeNode)parentNode.getChildAt(index)).getUserObject().toString());
        return(namesList);
    }
    
    /**
     * save all projects of the current projects set
     */
    final void saveCurrentWorkspace(){
    	//check if the tree has been fully created
   	    //this method may be called very early if something does not work
   	    if(projectsTree!=null)
   	        {DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
   	         if(treeModel!=null)
   		         {DefaultMutableTreeNode rootNode=(DefaultMutableTreeNode)treeModel.getRoot();
   			      if(rootNode!=null)
   			          {ProjectSet projectSet=(ProjectSet)rootNode.getUserObject();
   				       if(projectSet!=null)
   				           {if(projectSet.isDirty())
   			                    {for(Project project:projectSet.getProjectsList())
   			                         if(project.isDirty())
   			                             {//TODO: use a monitor
   			                       	      try{projectSet.saveProject(project);}
   			                              catch(Throwable throwable)
   			                              {mainWindow.displayErrorMessage(throwable,false);}
   			                             }       			                      
   			                    }
   				           }
   			          }
   		         }
   	        }
    }
    
    /**
     * load existing projects, skips already loaded projects
     */
    final void loadExistingProjects(){
        ProjectSet workspace=(ProjectSet)((DefaultMutableTreeNode)projectsTree.getModel().getRoot()).getUserObject();
        for(String projectName:workspace.getProjectNames())
            addProject(projectName);
    }
    
    private final Project addProject(String name){
        Project project=null;
        //if it is not in the tree
        if(!getAllProjectNames().contains(name))
            {ProjectSet workspace=(ProjectSet)((DefaultMutableTreeNode)projectsTree.getModel().getRoot()).getUserObject();            
             String[] projectNames=workspace.getProjectNames();
             File[] projectFiles=workspace.getProjectFiles();
             File projectFile=null;
             for(int i=0;i<projectNames.length;i++)
                 if(projectNames[i].equals(name))
                     {projectFile=projectFiles[i];
                      break;
                     }
             //if it is in the file system          
             if(projectFile!=null)
                 //load it into the workspace
                 project=workspace.loadProject(projectFile);
             else
                 {//create the project
                  project=new Project(name);
                  //add it into the workspace
                  workspace.addProject(project);
                 }
             //now the project is the workspace
             //put it into the tree
             DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
             DefaultMutableTreeNode projectsRoot=(DefaultMutableTreeNode)treeModel.getRoot();
             DefaultMutableTreeNode projectRootNode=new DefaultMutableTreeNode(project);
             treeModel.insertNodeInto(projectRootNode,projectsRoot,projectsRoot.getChildCount());
             //add the floor set
             FloorSet floorSet=project.getFloorSet();
             DefaultMutableTreeNode floorsRootNode=new DefaultMutableTreeNode(floorSet);
             treeModel.insertNodeInto(floorsRootNode,projectRootNode,projectRootNode.getChildCount());
             //add the floors
             for(Floor floor:floorSet.getFloorsList())
                 {DefaultMutableTreeNode floorNode=new DefaultMutableTreeNode(floor);
                  treeModel.insertNodeInto(floorNode,floorsRootNode,floorsRootNode.getChildCount());
                  //add each node of a map
                  DefaultMutableTreeNode mapNode;
                  for(MapType type:MapType.values())
                      {mapNode=new DefaultMutableTreeNode(floor.getMap(type));
                       ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(mapNode,floorNode,floorNode.getChildCount());
                      }
                  TreePath floorPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(floorNode));
                  if(!projectsTree.isExpanded(floorPath))
                      projectsTree.expandPath(floorPath);             
                 }
             TreePath floorSetPath=new TreePath(treeModel.getPathToRoot(floorsRootNode));
             if(!projectsTree.isExpanded(floorSetPath))
                 projectsTree.expandPath(floorSetPath);
             //add the tile set
             TileSet tileSet=project.getTileSet();
             DefaultMutableTreeNode tilesRootNode=new DefaultMutableTreeNode(tileSet);
             treeModel.insertNodeInto(tilesRootNode,projectRootNode,projectRootNode.getChildCount());
             //add the tiles
             for(Tile tile:tileSet.getTilesList())
                 {DefaultMutableTreeNode tileNode=new DefaultMutableTreeNode(tile);
                  treeModel.insertNodeInto(tileNode,tilesRootNode,tilesRootNode.getChildCount());                 
                 }
             TreePath tileSetPath=new TreePath(treeModel.getPathToRoot(tilesRootNode));
             if(!projectsTree.isExpanded(tileSetPath))
                 projectsTree.expandPath(tileSetPath);
             TreePath rootPath=new TreePath(projectsRoot);
             //expand the root path if it is not already expanded
             if(!projectsTree.isExpanded(rootPath))
                 projectsTree.expandPath(rootPath);
             TreePath projectPath=new TreePath(new Object[]{projectsRoot,projectRootNode});
             projectsTree.expandPath(projectPath);
            }
        return(project);
    }
    
    final Namable createNewEntityFromSelectedEntity(){
    	TreePath path=projectsTree.getSelectionPath();
    	DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
    	Object userObject=selectedNode.getUserObject();
    	NamingDialog enterNameDialog=null;
    	Namable newlyCreatedEntity=null;
    	if(userObject instanceof ProjectSet)
    	    enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllProjectNames(),"project");
    	else
    		if(userObject instanceof FloorSet)
                enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),"floor");
    		else
    			if(userObject instanceof TileSet)
                    {final ArrayList<Color> colors=new ArrayList<Color>();
                     //white is used for void
                     colors.add(Color.WHITE);
                     for(Tile tile:((TileSet)userObject).getTilesList())
                         colors.add(tile.getColor());
                     enterNameDialog=new TileCreationDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),colors);                   
                    }
    	if(enterNameDialog!=null)
    	    {enterNameDialog.setVisible(true);
             String name=enterNameDialog.getValidatedText();
             enterNameDialog.dispose();
             //if the name is correct and not already in use => not null
    		 if(name!=null)
    		     {if(userObject instanceof ProjectSet)
    		         newlyCreatedEntity=addProject(name);
    		      else
    		    	  if(userObject instanceof FloorSet)
    		    	      {Floor floor=new Floor(name);
                           ((FloorSet)userObject).addFloor(floor);
                           DefaultMutableTreeNode floorNode=new DefaultMutableTreeNode(floor);
                           ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(floorNode,selectedNode,selectedNode.getChildCount());
                           //add each node of a map
                           DefaultMutableTreeNode mapNode;
                           for(MapType type:MapType.values())
                               {mapNode=new DefaultMutableTreeNode(floor.getMap(type));
                                ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(mapNode,floorNode,floorNode.getChildCount());
                               }
                           TreePath floorSetPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(selectedNode));
                           if(!projectsTree.isExpanded(floorSetPath))
                    	       projectsTree.expandPath(floorSetPath);
                           TreePath floorPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(floorNode));
                           if(!projectsTree.isExpanded(floorPath))
                    	       projectsTree.expandPath(floorPath);
                           newlyCreatedEntity=floor;
    		    	      }
    		    	  else
    		    		  if(userObject instanceof TileSet)
    		    		      {Tile tile=new Tile(name);
    		    		       tile.setColor(((TileCreationDialog)enterNameDialog).getValidatedColor());
                               ((TileSet)userObject).addTile(tile);  
                               DefaultMutableTreeNode tileNode=new DefaultMutableTreeNode(tile);
                               ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(tileNode,selectedNode,selectedNode.getChildCount());
                               TreePath tileSetPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(selectedNode));
                               if(!projectsTree.isExpanded(tileSetPath))
                            	   projectsTree.expandPath(tileSetPath);
                               newlyCreatedEntity=tile;
    		    		      }
    		     }
    	    }
    	return(newlyCreatedEntity);
    }
    
    final void saveSelectedEntities(){
    	TreePath[] paths=projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        Object userObject;
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=selectedNode.getUserObject();
             Project project=null;
             ProjectSet projectSet=null;
             if(userObject instanceof Project)
                 {project=(Project)userObject;
                  projectSet=(ProjectSet)((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
                 }
             else
            	 if(userObject instanceof Floor || userObject instanceof Tile)
                     {project=(Project)((DefaultMutableTreeNode)selectedNode.getParent().getParent()).getUserObject();
                      projectSet=(ProjectSet)((DefaultMutableTreeNode)selectedNode.getParent().getParent().getParent()).getUserObject();                      
                     }
             if(project!=null&&projectSet!=null)
                 {try{projectSet.saveProject(project);}
                  catch(Throwable throwable)
                  {mainWindow.displayErrorMessage(throwable,false);}           	  
                 }
            }
    }
    
    final void exportSelectedEntity(){
        TreePath path=projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
        JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
        if(userObject instanceof Project)
            {Project project=(Project)userObject;
             ProjectSet projectSet=(ProjectSet)((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
             JFileChooser fileChooser=new JFileChooser();
             fileChooser.setMultiSelectionEnabled(false);
             fileChooser.setFileFilter(new FileNameExtensionFilter("JFPSM Projects","jfpsm.zip"));
             int result=fileChooser.showSaveDialog(mainWindow.getApplicativeFrame());
             if(result==JFileChooser.APPROVE_OPTION)
                 {try{projectSet.saveProject(project,fileChooser.getSelectedFile());}
                  catch(Throwable throwable)
                  {mainWindow.displayErrorMessage(throwable,false);}
                 }
            }
        else
            if(userObject instanceof Map)
                {Map map=(Map)userObject;
                 JFileChooser fileChooser=new JFileChooser();
                 fileChooser.setMultiSelectionEnabled(false);
                 fileChooser.setFileFilter(new FileNameExtensionFilter("Images","bmp","gif","jpg","jpeg","png"));
                 int result=fileChooser.showSaveDialog(mainWindow.getApplicativeFrame());
                 if(result==JFileChooser.APPROVE_OPTION)
                     {File imageFile=fileChooser.getSelectedFile();
                      int lastIndexOfDot=imageFile.getName().lastIndexOf(".");
                      String formatName=lastIndexOfDot>=0?imageFile.getName().substring(lastIndexOfDot+1):"";
                      try{if(formatName.equals(""))
                              throw new UnsupportedOperationException("Cannot export an image into a file without extension");
                          else
                              ImageIO.write(map.getImage(),formatName,imageFile);
                         }
                      catch(Throwable throwable)
                      {mainWindow.displayErrorMessage(throwable,false);}                  
                     }
                }
    }
    
    final void importSelectedEntity(){
        TreePath path=projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
        Object userObject=selectedNode.getUserObject();
        if(userObject instanceof ProjectSet)
            {JFileChooser fileChooser=new JFileChooser();
             fileChooser.setMultiSelectionEnabled(false);
             fileChooser.setFileFilter(new FileNameExtensionFilter("JFPSM Projects","jfpsm.zip"));
             int result=fileChooser.showOpenDialog(mainWindow.getApplicativeFrame());
             if(result==JFileChooser.APPROVE_OPTION)
                 {String fullname=fileChooser.getSelectedFile().getName();
                  String projectName=fullname.substring(0,fullname.length()-Project.getFileExtension().length());
                  ProjectSet workspace=(ProjectSet)userObject;
                  boolean confirmLoad=true;
                  if(Arrays.asList(workspace.getProjectNames()).contains(projectName))
                      {//prompt the user
                       confirmLoad=JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(),"Overwrite project \""+projectName+"\""+"?","Overwrite project",JOptionPane.OK_CANCEL_OPTION )==JOptionPane.OK_OPTION;
                       //if he confirms, delete the project
                       if(confirmLoad)
                           {final int count=selectedNode.getChildCount();
                            DefaultMutableTreeNode projectNode=null;
                            for(int i=0;i<count;i++)
                                if(((Project)((DefaultMutableTreeNode)selectedNode.getChildAt(i)).getUserObject()).getName().equals(projectName))
                                    {projectNode=(DefaultMutableTreeNode)selectedNode.getChildAt(i);
                                     break;
                                    }
                            Project project=(Project)projectNode.getUserObject();  
                            //remove all its entities from the entity viewer                           
                            for(Floor floor:project.getFloorSet().getFloorsList())
                                mainWindow.getEntityViewer().closeEntityView(floor);
                            for(Tile tile:project.getTileSet().getTilesList())
                                mainWindow.getEntityViewer().closeEntityView(tile);
                            //remove the project from the project set and the file system
                            workspace.removeProject(project);
                            //remove it from the tree
                            ((DefaultTreeModel)projectsTree.getModel()).removeNodeFromParent(projectNode);                            
                           }
                      }
                  if(confirmLoad)
                      {//copy the file into the workspace
                       File projectFile=new File(workspace.createProjectPath(projectName));
                       boolean success=true;
                       try{success=projectFile.createNewFile();
                           if(success)
                               {BufferedInputStream bis=new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                                BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(projectFile));
                                byte[] buf=new byte[1024];
                                int len;
                                while((len=bis.read(buf))>0)
                                    bos.write(buf,0,len);
                                bis.close();
                                bos.close();
                               }
                          } 
                       catch (Throwable throwable)
                       {mainWindow.displayErrorMessage(throwable,false);
                        success=false;
                       }
                       if(success)
                           addProject(projectName);
                      }
                 }  
            }
        else
            if(userObject instanceof Map)
                {Map map=(Map)userObject;
                 Floor floor=(Floor)((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject();
                 importImageForSelectedMap(floor,map);
                }
    }
    
    private final BufferedImage openFileAndLoadImage(){
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images","bmp","gif","jpg","jpeg","png"));
        int result=fileChooser.showOpenDialog(mainWindow.getApplicativeFrame());
        BufferedImage image=null;
        if(result==JFileChooser.APPROVE_OPTION)
            {try{image=ImageIO.read(fileChooser.getSelectedFile());}
             catch(Throwable throwable)
             {mainWindow.displayErrorMessage(throwable,false);}                  
            }
        return(image);
    }
    
    private final void importImageForSelectedMap(Floor floor,Map map){
        BufferedImage imageMap=openFileAndLoadImage();
        if(imageMap!=null)
            {//put the image map into the floor
             map.setImage(imageMap);
             //compute the max size
             int maxWidth=0,maxHeight=0,rgb;
             Map currentMap;
             BufferedImage nextImageMap;
             for(MapType currentType:MapType.values())
                 {currentMap=floor.getMap(currentType);
                  maxWidth=Math.max(currentMap.getWidth(),maxWidth);
                  maxHeight=Math.max(currentMap.getHeight(),maxHeight);
                 }
             //resize each map that is too small
             for(MapType currentType:MapType.values())
                 {currentMap=floor.getMap(currentType);
                  if(currentMap.getWidth()!=maxWidth||maxHeight!=currentMap.getHeight())
                      {nextImageMap=new BufferedImage(maxWidth,maxHeight,BufferedImage.TYPE_INT_ARGB);
                       for(int x=0;x<nextImageMap.getWidth();x++)
                           for(int y=0;y<nextImageMap.getHeight();y++)
                               {if(x<currentMap.getWidth()&&y<currentMap.getHeight())
                                    rgb=currentMap.getImage().getRGB(x,y);
                                else
                                    rgb=Color.WHITE.getRGB();
                                nextImageMap.setRGB(x,y,rgb);
                               }
                       floor.getMap(currentType).setImage(nextImageMap);
                      }
                 }
             //update the display
             mainWindow.getEntityViewer(). repaint();
            }
    }
    
    private final void expandPathDeeplyFromPath(TreePath path){
        projectsTree.expandPath(path);
        DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
        DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
        for(int i=0;i<node.getChildCount();i++)
            expandPathDeeplyFromPath(new TreePath(treeModel.getPathToRoot(node.getChildAt(i))));
    }
    
    final void openSelectedEntities(){
        TreePath[] paths=projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        JFPSMUserObject userObject;
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=(JFPSMUserObject)selectedNode.getUserObject();
             if(userObject instanceof Project||userObject instanceof FloorSet||userObject instanceof TileSet||userObject instanceof Floor)
                 expandPathDeeplyFromPath(path);
             if(userObject instanceof Floor||userObject instanceof Tile)
            	 {Project project=(Project)((DefaultMutableTreeNode)selectedNode.getParent().getParent()).getUserObject();
            	  //open a tab view for this entity
                  mainWindow.getEntityViewer().openEntityView((Namable)userObject,project);
            	 }
            }
    }
    
    final void closeSelectedEntities(){
        TreePath[] paths=projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        Object userObject;
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=selectedNode.getUserObject();
             if(userObject instanceof Project||userObject instanceof FloorSet||userObject instanceof TileSet||userObject instanceof Floor)
                 {projectsTree.collapsePath(path);
                  //close the tab views of their children
                  if(userObject instanceof FloorSet)
                      {for(Floor floor:((FloorSet)userObject).getFloorsList())
                    	   mainWindow.getEntityViewer().closeEntityView(floor);
                      }
                  else
                	  if(userObject instanceof TileSet)
                	      {for(Tile tile:((TileSet)userObject).getTilesList())
                   	           mainWindow.getEntityViewer().closeEntityView(tile);
                          }
                	  else
                		  if(userObject instanceof Project)
                		      {Project project=(Project)userObject;
                		       for(Floor floor:project.getFloorSet().getFloorsList())
                           	       mainWindow.getEntityViewer().closeEntityView(floor);
                		       for(Tile tile:project.getTileSet().getTilesList())
                       	           mainWindow.getEntityViewer().closeEntityView(tile);
                		      }
                 }             
             if(userObject instanceof Floor||userObject instanceof Tile)
            	 //close the tab view of this entity
                 mainWindow.getEntityViewer().closeEntityView((Namable)userObject);
            }
    }
    
    final void renameSelectedEntity(){
        TreePath path=projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        Object userObject=selectedNode.getUserObject();
        if(userObject instanceof Project)
            {//TODO: delete the file
             //      mark the project as dirty
            }
        else
            if(userObject instanceof Floor)
                {
                 
                }
            else
                if(userObject instanceof Tile)
                    {
                     
                    }
    }
    
    final Color getSelectedTileColor(final Project project){
    	Color color=null;
    	TreePath[] paths=projectsTree.getSelectionPaths();
    	DefaultMutableTreeNode selectedNode;
        Object userObject;
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=selectedNode.getUserObject();
             //check if the node is a tile and if this tile comes from the project
             if(userObject instanceof Tile&&((DefaultMutableTreeNode)selectedNode.getParent().getParent()).getUserObject()==project)
            	 {color=((Tile)userObject).getColor();
            	  break;
            	 }
            }
    	return(color);
    }
    
    final void deleteSelectedEntities(){
        TreePath[] paths=projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        ArrayList<DefaultMutableTreeNode> floorsTrashList=new ArrayList<DefaultMutableTreeNode>();
        ArrayList<DefaultMutableTreeNode> tilesTrashList=new ArrayList<DefaultMutableTreeNode>();
        ArrayList<DefaultMutableTreeNode> projectsTrashList=new ArrayList<DefaultMutableTreeNode>();        
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             if(selectedNode.getUserObject() instanceof Tile)
                 tilesTrashList.add(selectedNode);
             else
                 if(selectedNode.getUserObject() instanceof Floor)
                     floorsTrashList.add(selectedNode);
                 else
                     if(selectedNode.getUserObject() instanceof Project)
                         projectsTrashList.add(selectedNode);
            }
        final int elementsCount=floorsTrashList.size()+tilesTrashList.size()+projectsTrashList.size();
        if(elementsCount>=1)
            {StringBuffer entitiesBuffer=new StringBuffer();
             for(int index=0;index<floorsTrashList.size();index++)
                 entitiesBuffer.append(", \""+floorsTrashList.get(index).getUserObject().toString()+"\"");
             for(int index=0;index<tilesTrashList.size();index++)
                 entitiesBuffer.append(", \""+tilesTrashList.get(index).getUserObject().toString()+"\"");
             for(int index=0;index<projectsTrashList.size();index++)
                 entitiesBuffer.append(", \""+projectsTrashList.get(index).getUserObject().toString()+"\"");
             //delete the useless string ", " at the beginning
             entitiesBuffer.delete(0,2);
             String questionStart;
             final boolean noFloor=floorsTrashList.isEmpty();
             final boolean noProject=projectsTrashList.isEmpty();
             final boolean noTile=tilesTrashList.isEmpty();
             if(noFloor&&noProject)
                 questionStart="Delete tile";                
             else
                 if(noTile&&noProject)
                     questionStart="Delete floor";
                 else
                     if(noFloor&&noTile)
                         questionStart="Delete project";
                     else
                         questionStart="Delete element";
             //check if a plural is needed
             if(elementsCount>1)
                 questionStart+="s";
             String windowTitle="Confirm "+questionStart.toLowerCase();             
             if(JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(),questionStart+" "+entitiesBuffer.toString()+"?",windowTitle,JOptionPane.OK_CANCEL_OPTION )==JOptionPane.OK_OPTION)
                 {final DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
            	  for(DefaultMutableTreeNode node:tilesTrashList)
                      {TileSet tileSet=(TileSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();   
                       Tile tile=(Tile)node.getUserObject();
                       //remove the tile from the entity viewer by closing its tab view
                       mainWindow.getEntityViewer().closeEntityView(tile);
                       //remove the tile from the workspace and from the file if any
                       tileSet.removeTile(tile);
                       //remove the tile from the tree
                       treeModel.removeNodeFromParent(node);                       
                      }
                  for(DefaultMutableTreeNode node:floorsTrashList)
                      {FloorSet floorSet=(FloorSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                       Floor floor=(Floor)node.getUserObject();
                       mainWindow.getEntityViewer().closeEntityView(floor);
                       floorSet.removeFloor(floor);
                       treeModel.removeNodeFromParent(node);    
                      }
                  for(DefaultMutableTreeNode node:projectsTrashList)
                      {Project project=(Project)node.getUserObject();
                       ProjectSet projectSet=(ProjectSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                       //remove all its entities from the entity viewer
                       for(Floor floor:project.getFloorSet().getFloorsList())
                           mainWindow.getEntityViewer().closeEntityView(floor);
                       for(Tile tile:project.getTileSet().getTilesList())
                           mainWindow.getEntityViewer().closeEntityView(tile);
                       projectSet.removeProject(project);
                       treeModel.removeNodeFromParent(node);
                      }
                 }
            }   
    }
}