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
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

abstract class Viewer extends JPanel {

    
    private static final long serialVersionUID=1L;

    private final ProjectManager projectManager;
    
    private final Project project;
    
    private final Dirtyable entity;
    
    
    Viewer(final Dirtyable entity,final Project project,final ProjectManager projectManager){
        this.entity=entity;
        this.project=project;
        this.projectManager=projectManager;
    }
    
    
    final Color getSelectedTileColor(){
        return(projectManager.getSelectedTileColor(project));
    }
    
    final BufferedImage openFileAndLoadImage(){
        return(projectManager.openFileAndLoadImage());
    }
    
    final Dirtyable getEntity(){
    	return(entity);
    }
}
