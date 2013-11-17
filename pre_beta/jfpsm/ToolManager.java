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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Panel that allows to manipulate the tools in a tree containing their use cases
 * 
 * @author Julien Gouesse
 *
 */
public final class ToolManager extends JPanel{
	
	
    private static final long serialVersionUID=1L;
	
	private final JTree toolsTree;
	
	private final MainWindow mainWindow;
	
	private boolean quitEnabled;
	
	private final JPopupMenu treePopupMenu;
	
	private final JMenuItem newMenuItem;
	
	private final JMenuItem openMenuItem;
	
	private final JMenuItem closeMenuItem;

	public ToolManager(final MainWindow mainWindow) {
		super();
		this.mainWindow=mainWindow;
		this.quitEnabled=true;
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    	final DefaultMutableTreeNode toolsRoot=new DefaultMutableTreeNode(new ToolSet("Tool Set"));
    	toolsTree=new JTree(new DefaultTreeModel(toolsRoot));
    	final JScrollPane treePane=new JScrollPane(toolsTree);
    	toolsTree.setShowsRootHandles(true);
    	toolsTree.addTreeWillExpandListener(new TreeWillExpandListener(){

            @Override
            public final void treeWillCollapse(TreeExpansionEvent event)throws ExpandVetoException{             
            	toolsTreeWillCollapse(event);
            }

            @Override
            public final void treeWillExpand(TreeExpansionEvent event)throws ExpandVetoException{}
        });
    	toolsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    	//builds the popup menu
        treePopupMenu=new JPopupMenu();
        newMenuItem=new JMenuItem("New");
        openMenuItem=new JMenuItem("Open");
        closeMenuItem=new JMenuItem("Close");
        treePopupMenu.add(newMenuItem);
        treePopupMenu.add(openMenuItem);
        treePopupMenu.add(closeMenuItem);
        //TODO add action listeners
        toolsTree.addMouseListener(new MouseAdapter(){   
            
            @Override
            public final void mousePressed(MouseEvent e){
                handleMouseEvent(e);
            }
            
            @Override
            public final void mouseReleased(MouseEvent e){
                handleMouseEvent(e);
            }
        });
    	add(treePane);
	}
	
	private void handleMouseEvent(MouseEvent me){
    	if(me.isPopupTrigger())
            {//gets all selected paths
             TreePath[] paths=toolsTree.getSelectionPaths();
    	     TreePath mouseOverPath=toolsTree.getClosestPathForLocation(me.getX(),me.getY());
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
            	       toolsTree.setSelectionPath(mouseOverPath);
                       paths=new TreePath[]{mouseOverPath};
                      }
                 }
             if(paths!=null)
                 {final boolean singleSelection=paths.length==1;
                  //gets the first selected tree node
                  final TreePath path=toolsTree.getSelectionPath();
                  final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                  final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
                  final boolean showNew=singleSelection&&userObject.canInstantiateChildren();
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
                  newMenuItem.setVisible(showNew);
                  openMenuItem.setVisible(showOpenAndClose);
                  closeMenuItem.setVisible(showOpenAndClose);
                  if(showNew||showOpenAndClose)
                      treePopupMenu.show(mainWindow.getApplicativeFrame(),me.getXOnScreen(),me.getYOnScreen());
                 }
            }
    	else
    		//double-click
        	if(me.getClickCount()==2)
        	    {final TreePath path=toolsTree.getSelectionPath();
                 final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
                 final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();    
                 if(userObject!=null&&userObject.isOpenable())
                     mainWindow.getEntityViewer().openEntityView(userObject,null);
        	    }
    }
	
	private void toolsTreeWillCollapse(final TreeExpansionEvent event)throws ExpandVetoException{
		//prevents the user from collapsing the root
        if(((DefaultMutableTreeNode)event.getPath().getLastPathComponent()).getUserObject() instanceof ToolSet)
            throw new ExpandVetoException(event);
	}
	
	final synchronized boolean isQuitEnabled(){
    	return(quitEnabled);
    }
    
    final synchronized void setQuitEnabled(final boolean quitEnabled){
    	this.quitEnabled=quitEnabled;
    }
}
