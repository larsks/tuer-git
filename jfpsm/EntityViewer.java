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

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Widget that displays the entities. For example, a floor has a container map, a light map, a contained map and path maps.
 * This widget allows editing too.
 * @author Julien Gouesse
 *
 */
final class EntityViewer extends JPanel{

    
    private static final long serialVersionUID=1L;
    
    private final JTabbedPane entityTabbedPane;
    
    private final HashMap<Namable,JPanel> entityToTabComponentMap;
    
    private final ProjectManager projectManager;

    
    EntityViewer(ProjectManager projectManager){
    	this.projectManager=projectManager;
    	entityToTabComponentMap=new HashMap<Namable,JPanel>();
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        entityTabbedPane=new JTabbedPane();
        add(entityTabbedPane);
    }
    
    final boolean openEntityView(final JFPSMUserObject entity,final Project project){
    	final boolean success;
    	JPanel tabComponent;
    	Viewer entityView=null;
    	if(success=((tabComponent=entityToTabComponentMap.get(entity))==null&&(entityView=entity.createViewer(project,projectManager))!=null))
    	    {entityTabbedPane.addTab(entity.getName(),entityView);
             tabComponent=new JPanel(new FlowLayout(FlowLayout.LEFT,3,0));
             entityToTabComponentMap.put(entity,tabComponent);
             tabComponent.setOpaque(false);
             tabComponent.add(new JLabel(entity.getName()));
             JButton closeButton=new JButton("x");
             closeButton.addActionListener(new ActionListener(){               
                 @Override
                 public final void actionPerformed(ActionEvent e){
                	 closeEntityView(entity);
                 }
             });
             //remove all margins
             closeButton.setMargin(new Insets(0,0,0,0));
             tabComponent.add(closeButton);
             entityTabbedPane.setTabComponentAt(entityTabbedPane.indexOfComponent(entityView),tabComponent);
    	    }
    	else
    		//if the view of this entity is already open, select it
    		if(tabComponent!=null)
    		    entityTabbedPane.setSelectedIndex(entityTabbedPane.indexOfTabComponent(tabComponent));
        return(success);
    }
    
    final boolean renameEntityView(final Namable entity){
    	final boolean success;
    	final JPanel tabComponent;
    	if(success=(tabComponent=entityToTabComponentMap.get(entity))!=null)
	        ((JLabel)tabComponent.getComponent(0)).setText(entity.getName());
    	return(success);
    }
        
    final boolean closeEntityView(final Namable entity){
    	JPanel tabComponent=entityToTabComponentMap.get(entity);
    	final boolean success;
    	if(success=tabComponent!=null)
    	    {entityTabbedPane.removeTabAt(entityTabbedPane.indexOfTabComponent(tabComponent));
    	     entityToTabComponentMap.remove(entity);
    	    }
    	return(success);
    }
}
