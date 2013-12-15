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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
 * Panel that allows to manipulate the entities in a tree containing their sub-components.
 * 
 * @author Julien Gouesse
 */
public abstract class EntityManager extends JPanel{

	
	private static final long serialVersionUID=1L;
	
    protected final MainWindow mainWindow;
    
    protected final JTree tree;
	
	private boolean quitEnabled;
	
	protected final JPopupMenu treePopupMenu;
	
	protected final JMenuItem newMenuItem;
	
	protected final JMenuItem openMenuItem;
	
	protected final JMenuItem closeMenuItem;
	
	protected final JMenuItem deleteMenuItem;

	public EntityManager(final MainWindow mainWindow,final DefaultTreeModel treeModel){
		super();
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.mainWindow=mainWindow;
		this.quitEnabled=true;
		this.treePopupMenu=new JPopupMenu();
		this.tree=new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.addTreeWillExpandListener(new TreeWillExpandListener(){

            @Override
            public final void treeWillCollapse(final TreeExpansionEvent event)throws ExpandVetoException{             
            	EntityManager.this.treeWillCollapse(event);
            }

            @Override
            public final void treeWillExpand(final TreeExpansionEvent event)throws ExpandVetoException{}
        });
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		//creates the menu item
		newMenuItem=new JMenuItem("New");
        openMenuItem=new JMenuItem("Open");
        closeMenuItem=new JMenuItem("Close");
        deleteMenuItem=new JMenuItem("Delete");
        //fills the popup menu
        treePopupMenu.add(newMenuItem);
        treePopupMenu.add(openMenuItem);
        treePopupMenu.add(closeMenuItem);
        treePopupMenu.add(deleteMenuItem);
        //adds the action listeners into the menu items
        newMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				createNewEntityFromSelectedEntity();
			}
		});
        openMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				openSelectedEntities();
			}
		});
        closeMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				closeSelectedEntities();
			}
		});
        deleteMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				deleteSelectedEntities();
			}
		});
		final JScrollPane treePane=new JScrollPane(tree);
		add(treePane);
	}
	
	private void expandPathDeeplyFromPath(TreePath path){
		tree.expandPath(path);
	    final DefaultMutableTreeNode node=(DefaultMutableTreeNode)path.getLastPathComponent();
	    final DefaultTreeModel treeModel=(DefaultTreeModel)tree.getModel();
	    for(int i=0;i<node.getChildCount();i++)
	        expandPathDeeplyFromPath(new TreePath(treeModel.getPathToRoot(node.getChildAt(i))));
	}
	
	protected void openSelectedEntities(){
        final TreePath[] paths=tree.getSelectionPaths();
        for(TreePath path:paths)
            {final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
             if(userObject.isOpenable())
                 expandPathDeeplyFromPath(path);
            }
    }
	
	protected void closeSelectedEntities(){
        final TreePath[] paths=tree.getSelectionPaths();
        for(TreePath path:paths)
            {final DefaultMutableTreeNode selectedNode=(DefaultMutableTreeNode)path.getLastPathComponent();
             final JFPSMUserObject userObject=(JFPSMUserObject)selectedNode.getUserObject();
             if(userObject.isOpenable())
                 tree.collapsePath(path);
            }
    }
	
	protected abstract void deleteSelectedEntities();
	
	protected abstract Namable createNewEntityFromSelectedEntity();
	
	public synchronized boolean isQuitEnabled(){
    	return(quitEnabled);
    }
    
	public synchronized void setQuitEnabled(final boolean quitEnabled){
    	this.quitEnabled=quitEnabled;
    }
	
	protected void treeWillCollapse(final TreeExpansionEvent event)throws ExpandVetoException{
		//prevents the user from collapsing the root
        if(event.getPath().getLastPathComponent() == tree.getModel().getRoot())
            throw new ExpandVetoException(event);
	}
}