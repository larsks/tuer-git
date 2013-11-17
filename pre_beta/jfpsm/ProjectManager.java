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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
	
	private boolean quitEnabled;
	
	private final ProgressDialog progressDialog;

	
	/**
	 * build a project manager
	 * @param mainWindow window that contains this manager
	 */
	public ProjectManager(final MainWindow mainWindow){
		this.mainWindow=mainWindow;
		quitEnabled=true;
		progressDialog=new ProgressDialog(mainWindow.getApplicativeFrame(),"Work in progress...");
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));		
        final DefaultMutableTreeNode projectsRoot=new DefaultMutableTreeNode(new ProjectSet("Project Set"));
        projectsTree=new JTree(new DefaultTreeModel(projectsRoot));
        JScrollPane treePane=new JScrollPane(projectsTree);
        projectsTree.setShowsRootHandles(true);
        projectsTree.addTreeWillExpandListener(new TreeWillExpandListener(){

            @Override
            public final void treeWillCollapse(TreeExpansionEvent event)throws ExpandVetoException{             
                //prevents the user from collapsing the root
                if(((DefaultMutableTreeNode)event.getPath().getLastPathComponent()).getUserObject() instanceof ProjectSet)
                    throw new ExpandVetoException(event);
            }

            @Override
            public final void treeWillExpand(TreeExpansionEvent event)throws ExpandVetoException{}
        });
        projectsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        loadExistingProjects();
        //builds the popup menu
        final JPopupMenu treePopupMenu=new JPopupMenu();
        final JMenuItem newMenuItem=new JMenuItem("New");
        final JMenuItem renameMenuItem=new JMenuItem("Rename");
        final JMenuItem importMenuItem=new JMenuItem("Import");        
        final JMenuItem exportMenuItem=new JMenuItem("Export");
        //FIXME: re-organizes the GUI that allows to generate game files
        final JMenuItem generateGameFilesMenuItem=new JMenuItem("Generate game files");
        final JMenuItem refreshMenuItem=new JMenuItem("Refresh");       
        final JMenuItem openMenuItem=new JMenuItem("Open");        
        final JMenuItem closeMenuItem=new JMenuItem("Close");       
        final JMenuItem deleteMenuItem=new JMenuItem("Delete");       
        final JMenuItem saveMenuItem=new JMenuItem("Save");
        final JPopupMenu.Separator separator=new JPopupMenu.Separator();
        final JMenu toolsMenu=new JMenu("Tools");
        final JMenuItem converterMenuItem=new JMenuItem("Converter");
        treePopupMenu.add(newMenuItem);
        treePopupMenu.add(renameMenuItem);
        treePopupMenu.add(importMenuItem);
        treePopupMenu.add(exportMenuItem);
        treePopupMenu.add(generateGameFilesMenuItem);
        treePopupMenu.add(refreshMenuItem);
        treePopupMenu.add(openMenuItem);
        treePopupMenu.add(closeMenuItem);
        treePopupMenu.add(deleteMenuItem);
        treePopupMenu.add(saveMenuItem);
        treePopupMenu.add(separator);
        treePopupMenu.add(toolsMenu);
        toolsMenu.add(converterMenuItem);
        //builds action listeners
        newMenuItem.addActionListener(new CreateNewEntityFromSelectedEntityAction(this));
        renameMenuItem.addActionListener(new RenameSelectedEntityAction(this));
        importMenuItem.addActionListener(new ImportSelectedEntityAction(this));
        exportMenuItem.addActionListener(new ExportSelectedEntityAction(this));
        generateGameFilesMenuItem.addActionListener(new GenerateGameFilesAction(this));
        refreshMenuItem.addActionListener(new RefreshSelectedEntitiesAction(this));
        openMenuItem.addActionListener(new OpenSelectedEntitiesAction(this));
        closeMenuItem.addActionListener(new CloseSelectedEntitiesAction(this));
        deleteMenuItem.addActionListener(new DeleteSelectedEntitiesAction(this));
        saveMenuItem.addActionListener(new SaveSelectedEntitiesAction(this));
        converterMenuItem.addActionListener(new OpenConverterAction(this));
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
                    {//gets all selected paths
                     TreePath[] paths=projectsTree.getSelectionPaths();
                	 TreePath mouseOverPath=projectsTree.getClosestPathForLocation(e.getX(),e.getY());
                     if(mouseOverPath!=null)
                         {//if no node is selected or if the node under the mouse pointer is not selected
                          //then selects only the node under the mouse pointer and invalidate the previous selection
                          boolean found=false;
                          if(paths!=null)
                              for(TreePath path:paths)
                                  if(path.equals(mouseOverPath))
                                      {found=true;
                                       break;
                                      }
                          if(!found)
                              {//selects the path on right click if none was already selected                      
                               projectsTree.setSelectionPath(mouseOverPath);
                               paths=new TreePath[]{mouseOverPath};
                              }                                                     
                         }
                     if(paths!=null)
                         {final boolean singleSelection=paths.length==1;
                          //gets the first selected tree node
                          final TreePath path=projectsTree.getSelectionPath();
                          final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                          final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
                          final boolean showNew=singleSelection&&userObject.canInstantiateChildren();
                          final boolean showImport=singleSelection&&(userObject instanceof ProjectSet||userObject instanceof Map);
                          final boolean showExport=singleSelection&&userObject instanceof Project||userObject instanceof Map;
                          final boolean showGenerateGameFiles=singleSelection&&userObject instanceof Project;
                          final boolean showRefresh=singleSelection&&userObject instanceof ProjectSet;
                          final boolean showRename=singleSelection&&(userObject instanceof FloorSet||userObject instanceof Floor||userObject instanceof Tile);
                          final boolean showSave=singleSelection&&userObject instanceof Project;
                          final boolean showTools=singleSelection&&userObject instanceof ProjectSet;
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
                          showDelete=false;
                          for(TreePath currentPath:paths)
                              {currentUserObject=(JFPSMUserObject)((DefaultMutableTreeNode)currentPath.getLastPathComponent()).getUserObject();
                               if(currentUserObject.isRemovable())
                                   {showDelete=true;
                                    break;
                            	   }
                              }
                    	  newMenuItem.setVisible(showNew);
                    	  renameMenuItem.setVisible(showRename);
                          importMenuItem.setVisible(showImport);
                          exportMenuItem.setVisible(showExport);
                          generateGameFilesMenuItem.setVisible(showGenerateGameFiles);
                          refreshMenuItem.setVisible(showRefresh);
                          openMenuItem.setVisible(showOpenAndClose);
                          closeMenuItem.setVisible(showOpenAndClose);
                          deleteMenuItem.setVisible(showDelete);
                          saveMenuItem.setVisible(showSave);
                          separator.setVisible(showTools);
                          toolsMenu.setVisible(showTools);
                          treePopupMenu.show(mainWindow.getApplicativeFrame(),e.getXOnScreen(),e.getYOnScreen());
                         }
                    }
                else
                	//double-click
                	if(e.getClickCount()==2)
                	    {final TreePath path=projectsTree.getSelectionPath();
                         final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                         final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();    
                         if(userObject!=null)
                             {final Project project=getProjectFromSelectedNode(selectedNode);
                              if(project!=null)
                        	      mainWindow.getEntityViewer().openEntityView(userObject,project);                        
                             }
                	    }
            }           
        });       
        add(treePane);       
	}
	
	private static final Project getProjectFromSelectedNode(final DefaultMutableTreeNode selectedNode){
		DefaultMutableTreeNode node=selectedNode;
		Project project=null;
		Object userObject;
		while(node!=null)
		    {userObject=node.getUserObject();
			 if(userObject!=null&&userObject instanceof Project)
		         {project=(Project)userObject;
		    	  break;
		         }
			 else
				 node=(DefaultMutableTreeNode)node.getParent();
		    }
		return(project);
	}
	
	/**
	 * Gets the project names as they are in the graphical user interface
	 * @return all project names in the tree
	 */
	private final ArrayList<String> getAllProjectNames(){
        return(getAllChildrenNames((DefaultMutableTreeNode)projectsTree.getModel().getRoot()));
    }
    
    private final ArrayList<String> getAllChildrenNames(DefaultMutableTreeNode parentNode){
        ArrayList<String> namesList=new ArrayList<>();
        final int size=parentNode.getChildCount();
        for(int index=0;index<size;index++)
            namesList.add(((DefaultMutableTreeNode)parentNode.getChildAt(index)).getUserObject().toString());
        return(namesList);
    }
    
    /**
     * saves all projects of the current projects set
     */
    final void saveCurrentWorkspace(){
    	//checks if the tree has been fully created
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
   			                              {displayErrorMessage(throwable,false);}
   			                             }       			                      
   			                    }
   				           }
   			          }
   		         }
   	        }
    }
    
    final void displayErrorMessage(Throwable throwable,boolean fatal){
        mainWindow.displayErrorMessage(throwable,fatal);
    }
    
    /**
     * loads existing projects, skips already loaded projects
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
                 //loads it into the workspace
                 project=workspace.loadProject(projectFile);
             else
                 {//creates the project
                  project=new Project(name);
                  //adds it into the workspace
                  workspace.addProject(project);
                 }
             //now the project is the workspace
             //puts it into the tree
             DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
             DefaultMutableTreeNode projectsRoot=(DefaultMutableTreeNode)treeModel.getRoot();
             DefaultMutableTreeNode projectRootNode=new DefaultMutableTreeNode(project);
             treeModel.insertNodeInto(projectRootNode,projectsRoot,projectsRoot.getChildCount());
             //adds the level set
             LevelSet levelSet=project.getLevelSet();
             DefaultMutableTreeNode levelSetNode=new DefaultMutableTreeNode(levelSet);
             treeModel.insertNodeInto(levelSetNode,projectRootNode,projectRootNode.getChildCount());
             DefaultMutableTreeNode floorSetRootNode;
             for(FloorSet floorSet:levelSet.getFloorSetsList())
                 {floorSetRootNode=new DefaultMutableTreeNode(floorSet);
                  treeModel.insertNodeInto(floorSetRootNode,levelSetNode,levelSetNode.getChildCount());
                  //adds the floors
                  for(Floor floor:floorSet.getFloorsList())
                      {DefaultMutableTreeNode floorNode=new DefaultMutableTreeNode(floor);
                       treeModel.insertNodeInto(floorNode,floorSetRootNode,floorSetRootNode.getChildCount());
                       //adds each node of a map
                       DefaultMutableTreeNode mapNode;
                       for(MapType type:MapType.values())
                           {mapNode=new DefaultMutableTreeNode(floor.getMap(type));
                            ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(mapNode,floorNode,floorNode.getChildCount());
                           }
                       TreePath floorPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(floorNode));
                       if(!projectsTree.isExpanded(floorPath))
                           projectsTree.expandPath(floorPath);
                      }
                  TreePath floorSetPath=new TreePath(treeModel.getPathToRoot(floorSetRootNode));
                  if(!projectsTree.isExpanded(floorSetPath))
                      projectsTree.expandPath(floorSetPath);            	  
                 }
             TreePath levelSetPath=new TreePath(treeModel.getPathToRoot(levelSetNode));
             if(!projectsTree.isExpanded(levelSetPath))
                 projectsTree.expandPath(levelSetPath);
             //adds the tile set
             TileSet tileSet=project.getTileSet();
             DefaultMutableTreeNode tilesRootNode=new DefaultMutableTreeNode(tileSet);
             treeModel.insertNodeInto(tilesRootNode,projectRootNode,projectRootNode.getChildCount());
             //adds the tiles
             for(Tile tile:tileSet.getTilesList())
                 {DefaultMutableTreeNode tileNode=new DefaultMutableTreeNode(tile);
                  treeModel.insertNodeInto(tileNode,tilesRootNode,tilesRootNode.getChildCount());                 
                 }
             TreePath tileSetPath=new TreePath(treeModel.getPathToRoot(tilesRootNode));
             if(!projectsTree.isExpanded(tileSetPath))
                 projectsTree.expandPath(tileSetPath);
             TreePath rootPath=new TreePath(projectsRoot);
             //expands the root path if it is not already expanded
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
                    {final ArrayList<Color> colors=new ArrayList<>();
                     //white is used for void
                     colors.add(Color.WHITE);
                     for(Tile tile:((TileSet)userObject).getTilesList())
                         colors.add(tile.getColor());
                     enterNameDialog=new TileCreationDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),colors);                   
                    }
    			else
    			    if(userObject instanceof LevelSet)
    			        enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),"level");
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
                           //adds each node of a map
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
    		    		  else
    		    		      if(userObject instanceof LevelSet)
    		    		          {FloorSet floorSet=new FloorSet(name);
    		    		           ((LevelSet)userObject).addFloorSet(floorSet);
    		    		           DefaultMutableTreeNode floorSetNode=new DefaultMutableTreeNode(floorSet);
    	                           ((DefaultTreeModel)projectsTree.getModel()).insertNodeInto(floorSetNode,selectedNode,selectedNode.getChildCount());
    	                           TreePath floorSetPath=new TreePath(((DefaultTreeModel)projectsTree.getModel()).getPathToRoot(selectedNode));
                                   if(!projectsTree.isExpanded(floorSetPath))
                                       projectsTree.expandPath(floorSetPath);
                                   newlyCreatedEntity=floorSet;
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
                  try{projectSet.saveProject(project);}
                  catch(Throwable throwable)
                  {displayErrorMessage(throwable,false);}
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
                  {displayErrorMessage(throwable,false);}
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
                      {displayErrorMessage(throwable,false);}                  
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
                 {String projectName=Project.getProjectNameFromFile(fileChooser.getSelectedFile());
                  ProjectSet workspace=(ProjectSet)userObject;
                  boolean confirmLoad=true;
                  if(Arrays.asList(workspace.getProjectNames()).contains(projectName))
                      {//prompts the user
                       confirmLoad=JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(),"Overwrite project \""+projectName+"\""+"?","Overwrite project",JOptionPane.OK_CANCEL_OPTION )==JOptionPane.OK_OPTION;
                       //if he confirms, deletes the project
                       if(confirmLoad)
                           {final int count=selectedNode.getChildCount();
                            DefaultMutableTreeNode projectNode=null;
                            for(int i=0;i<count;i++)
                                if(((Project)((DefaultMutableTreeNode)selectedNode.getChildAt(i)).getUserObject()).getName().equals(projectName))
                                    {projectNode=(DefaultMutableTreeNode)selectedNode.getChildAt(i);
                                     break;
                                    }
                            if(projectNode!=null)
                                {final Project project=(Project)projectNode.getUserObject();  
                                 //removes all its entities from the entity viewer
                                 for(FloorSet floorSet:project.getLevelSet().getFloorSetsList())
                            	     for(Floor floor:floorSet.getFloorsList())
                            		     mainWindow.getEntityViewer().closeEntityView(floor);
                                 for(Tile tile:project.getTileSet().getTilesList())
                                     mainWindow.getEntityViewer().closeEntityView(tile);
                                 //removes the project from the project set and the file system
                                 workspace.removeProject(project);
                                 //removes it from the tree
                                 ((DefaultTreeModel)projectsTree.getModel()).removeNodeFromParent(projectNode);
                                }
                           }
                      }
                  if(confirmLoad)
                      {//copies the file into the workspace
                       File projectFile=new File(workspace.createProjectPath(projectName));
                       boolean success=true;
                       try{success=projectFile.createNewFile();
                           if(success)
                               {try(BufferedInputStream bis=new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()))){
                                    try(BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(projectFile))){
                                        byte[] buf=new byte[1024];
                                        int len;
                                        while((len=bis.read(buf))>0)
                                            bos.write(buf,0,len);
                                    }
                                }
                               }
                          } 
                       catch(Throwable throwable)
                       {displayErrorMessage(throwable,false);
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
    
    final BufferedImage openFileAndLoadImage(){
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images","bmp","gif","jpg","jpeg","png"));
        int result=fileChooser.showOpenDialog(mainWindow.getApplicativeFrame());
        BufferedImage image=null;
        if(result==JFileChooser.APPROVE_OPTION)
            {try{image=ImageIO.read(fileChooser.getSelectedFile());}
             catch(Throwable throwable)
             {displayErrorMessage(throwable,false);}                  
            }
        return(image);
    }
    
    private final void importImageForSelectedMap(Floor floor,Map map){
        BufferedImage imageMap=openFileAndLoadImage();
        if(imageMap!=null)
            {//puts the image map into the floor
             map.setImage(imageMap);
             //computes the max size
             int maxWidth=0,maxHeight=0,rgb;
             Map currentMap;
             BufferedImage nextImageMap;
             for(MapType currentType:MapType.values())
                 {currentMap=floor.getMap(currentType);
                  maxWidth=Math.max(currentMap.getWidth(),maxWidth);
                  maxHeight=Math.max(currentMap.getHeight(),maxHeight);
                 }
             //resizes each map that is too small
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
             //updates the display
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
    
    final void openConverter(){
    	//TODO
    }
    
    final void openSelectedEntities(){
        TreePath[] paths=projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        JFPSMUserObject userObject;
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=(JFPSMUserObject)selectedNode.getUserObject();
             if(userObject instanceof Project||userObject instanceof LevelSet||userObject instanceof FloorSet||userObject instanceof TileSet||userObject instanceof Floor)
                 expandPathDeeplyFromPath(path);
             if(userObject instanceof Tile)
                 {Project project=(Project)((DefaultMutableTreeNode)selectedNode.getParent().getParent()).getUserObject();                                 
                  mainWindow.getEntityViewer().openEntityView(userObject,project);                       
                 }
             else
                 if(userObject instanceof Floor)
                     {Project project=(Project)((DefaultMutableTreeNode)selectedNode.getParent().getParent().getParent()).getUserObject();                                 
                      //opens a tab view for this entity
                      mainWindow.getEntityViewer().openEntityView(userObject,project);                       
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
             if(userObject instanceof Project||userObject instanceof  LevelSet||userObject instanceof FloorSet||userObject instanceof TileSet||userObject instanceof Floor)
                 {projectsTree.collapsePath(path);
                  //closes the tab views of their children
                  if(userObject instanceof LevelSet)
                      {for(FloorSet floorSet:((LevelSet)userObject).getFloorSetsList())
                    	  for(Floor floor:floorSet.getFloorsList())
                    		  mainWindow.getEntityViewer().closeEntityView(floor);
                      }
                  else
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
                		           for(FloorSet floorSet:project.getLevelSet().getFloorSetsList())
                                	   for(Floor floor:floorSet.getFloorsList())
                                		   mainWindow.getEntityViewer().closeEntityView(floor);
                		           for(Tile tile:project.getTileSet().getTilesList())
                       	               mainWindow.getEntityViewer().closeEntityView(tile);
                		          }
                 }             
             if(userObject instanceof Floor||userObject instanceof Tile)
            	 //closes the tab view of this entity
                 mainWindow.getEntityViewer().closeEntityView((Namable)userObject);
            }
    }
    
    final void renameSelectedEntity(){
        TreePath path=projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
        JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
        NamingDialog enterNameDialog=null;
        if(userObject instanceof Floor)
            {enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),"floor");
             enterNameDialog.setTitle("Rename floor");
            }
        else
            if(userObject instanceof Tile)
                {enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),"tile");
                 enterNameDialog.setTitle("Rename tile");
                }
            else
            	if(userObject instanceof FloorSet)
            	    {enterNameDialog=new NamingDialog(mainWindow.getApplicativeFrame(),getAllChildrenNames(selectedNode),"level");
                     enterNameDialog.setTitle("Rename level");
            	    }
        if(enterNameDialog!=null)
	        {enterNameDialog.setVisible(true);
             String name=enterNameDialog.getValidatedText();
             enterNameDialog.dispose();
		     if(name!=null)
		         {userObject.setName(name);
		          //renames the entity view opened on this entity if any
		          mainWindow.getEntityViewer().renameEntityView(userObject);
		         }
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
             //checks if the node is a tile and if this tile comes from the project
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
        JFPSMUserObject userObject;
        ArrayList<DefaultMutableTreeNode> floorsTrashList=new ArrayList<>();
        ArrayList<DefaultMutableTreeNode> floorSetsTrashList=new ArrayList<>();
        ArrayList<DefaultMutableTreeNode> tilesTrashList=new ArrayList<>();
        ArrayList<DefaultMutableTreeNode> projectsTrashList=new ArrayList<>();        
        for(TreePath path:paths)
            {selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             userObject=(JFPSMUserObject)selectedNode.getUserObject();
             if(userObject instanceof Tile)
                 tilesTrashList.add(selectedNode);
             else
                 if(userObject instanceof Floor)
                     floorsTrashList.add(selectedNode);
                 else
                     if(userObject instanceof FloorSet)
                         floorSetsTrashList.add(selectedNode);
                 else
                     if(userObject instanceof Project)
                         projectsTrashList.add(selectedNode);
            }
        final int elementsCount=floorsTrashList.size()+floorSetsTrashList.size()+tilesTrashList.size()+projectsTrashList.size();
        if(elementsCount>=1)
            {StringBuffer entitiesBuffer=new StringBuffer();
             for(int index=0;index<floorsTrashList.size();index++)
                 entitiesBuffer.append(", \""+floorsTrashList.get(index).getUserObject().toString()+"\"");
             for(int index=0;index<floorSetsTrashList.size();index++)
                 entitiesBuffer.append(", \""+floorSetsTrashList.get(index).getUserObject().toString()+"\"");
             for(int index=0;index<tilesTrashList.size();index++)
                 entitiesBuffer.append(", \""+tilesTrashList.get(index).getUserObject().toString()+"\"");
             for(int index=0;index<projectsTrashList.size();index++)
                 entitiesBuffer.append(", \""+projectsTrashList.get(index).getUserObject().toString()+"\"");
             //deletes the useless string ", " at the beginning
             entitiesBuffer.delete(0,2);
             String questionStart;
             final boolean noFloor=floorsTrashList.isEmpty();
             final boolean noLevel=floorSetsTrashList.isEmpty();
             final boolean noProject=projectsTrashList.isEmpty();
             final boolean noTile=tilesTrashList.isEmpty();
             if(noFloor&&noProject&&noLevel)
                 questionStart="Delete tile";                
             else
                 if(noTile&&noProject&&noLevel)
                     questionStart="Delete floor";
                 else
                     if(noFloor&&noTile&&noLevel)
                         questionStart="Delete project";
                     else
                         if(noFloor&&noTile&&noProject)
                             questionStart="Delete level";
                         else
                             questionStart="Delete element";
             //checks if a plural is needed
             if(elementsCount>1)
                 questionStart+="s";
             String windowTitle="Confirm "+questionStart.toLowerCase();             
             if(JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(),questionStart+" "+entitiesBuffer.toString()+"?",windowTitle,JOptionPane.OK_CANCEL_OPTION )==JOptionPane.OK_OPTION)
                 {final DefaultTreeModel treeModel=(DefaultTreeModel)projectsTree.getModel();
            	  for(DefaultMutableTreeNode node:tilesTrashList)
                      {TileSet tileSet=(TileSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();   
                       Tile tile=(Tile)node.getUserObject();
                       //removes the tile from the entity viewer by closing its tab view
                       mainWindow.getEntityViewer().closeEntityView(tile);
                       //removes the tile from the workspace and from the file if any
                       tileSet.removeTile(tile);
                       //removes the tile from the tree
                       treeModel.removeNodeFromParent(node);
                      }
                  for(DefaultMutableTreeNode node:floorsTrashList)
                      {FloorSet floorSet=(FloorSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                       Floor floor=(Floor)node.getUserObject();
                       mainWindow.getEntityViewer().closeEntityView(floor);
                       floorSet.removeFloor(floor);
                       treeModel.removeNodeFromParent(node);
                      }
                  for(DefaultMutableTreeNode node:floorSetsTrashList)
                      {LevelSet levelSet=(LevelSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                       FloorSet floorSet=(FloorSet)node.getUserObject();
                       for(Floor floor:floorSet.getFloorsList())
                           mainWindow.getEntityViewer().closeEntityView(floor);
                       floorSet.removeAllFloors();
                       levelSet.removeFloorSet(floorSet);
                       treeModel.removeNodeFromParent(node);
                      }
                  for(DefaultMutableTreeNode node:projectsTrashList)
                      {Project project=(Project)node.getUserObject();
                       ProjectSet projectSet=(ProjectSet)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                       //removes all its entities from the entity viewer
                       for(FloorSet floorSet:project.getLevelSet().getFloorSetsList())
                       	   for(Floor floor:floorSet.getFloorsList())
                       		   mainWindow.getEntityViewer().closeEntityView(floor);
                       for(Tile tile:project.getTileSet().getTilesList())
                           mainWindow.getEntityViewer().closeEntityView(tile);
                       projectSet.removeProject(project);
                       treeModel.removeNodeFromParent(node);
                      }
                 }
            }   
    }
    
    final String createRawDataPath(String name){
        ProjectSet workspace=(ProjectSet)((DefaultMutableTreeNode)projectsTree.getModel().getRoot()).getUserObject();
        return(workspace.createRawDataPath(name));
    }
    
    final synchronized boolean isQuitEnabled(){
    	return(quitEnabled);
    }
    
    final synchronized void setQuitEnabled(final boolean quitEnabled){
    	this.quitEnabled=quitEnabled;
    }
    
    /**
     * generates level files one by one
     */
    final void generateGameFiles(){
        TreePath path=projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
        JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
        if(userObject instanceof Project)
            {final Project project=(Project)userObject;             
             new GameFileExportSwingWorker(this,project,progressDialog).execute();
            }
    }
    
    private static final class GameFileExportSwingWorker extends SwingWorker<ArrayList<String>,String>{
    	
    	
    	private final ArrayList<FloorSet> levelsList;
    	
    	private final ProjectManager projectManager;
    	
    	private final Project project;
    	
    	private final ProgressDialog dialog;
    	
    	
    	private GameFileExportSwingWorker(final ProjectManager projectManager,final Project project,final ProgressDialog dialog){
    		this.levelsList=project.getLevelSet().getFloorSetsList();
    		this.projectManager=projectManager;
    		this.project=project;
    		this.dialog=dialog;
    		SwingUtilities.invokeLater(new Runnable(){
    			 @Override
    			 public final void run(){
    				 dialog.reset();
    				 dialog.setVisible(true);
    			 }
    		});
    	}
    	
    	
    	@Override
    	protected final ArrayList<String> doInBackground(){
    		//prevents the user from leaving the application during an export
    		projectManager.setQuitEnabled(false);
    		ArrayList<String> filenamesList=new ArrayList<>();
    		File levelFile,levelCollisionFile;
    		int levelIndex=0;
    		for(FloorSet level:levelsList)
    		    {levelFile=new File(projectManager.createRawDataPath(level.getName()+".abin"));
    		     levelCollisionFile=new File(projectManager.createRawDataPath(level.getName()+".collision.abin"));
                 try{GameFilesGenerator.getInstance().writeLevel(level,levelIndex,project,levelFile,levelCollisionFile);}
                 catch(Throwable throwable)
                 {projectManager.displayErrorMessage(throwable,false);}
                 filenamesList.add(level.getName());
                 publish(level.getName());
                 setProgress(100*filenamesList.size()/levelsList.size());
                 levelIndex++;
    		    }    		
    		return(filenamesList);
    	}
    	
    	@Override
    	protected final void process(List<String> chunks){
    		StringBuilder builder=new StringBuilder();
    		for(String chunk:chunks)
    			{builder.append(chunk);
    			 builder.append(" ");
    			}
    		dialog.setText(builder.toString());
    		dialog.setValue(100*chunks.size()/levelsList.size());
    	}
    	
    	@Override
        protected final void done(){
    		//allows the user to leave the application
   	        projectManager.setQuitEnabled(true);
   	        dialog.setVisible(false);
   	        dialog.reset();
    	}
    }
}
