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
package jme;

import java.util.ArrayList;
import java.util.List;

import bean.NodeIdentifier;

import com.jme.scene.Spatial;

/**
 * Set of walls representing a single room and linked to other rooms
 * by portals
 * @author Julien Gouesse
 *
 */
final class Cell extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    private List<Portal> portalsList;
     
    
    Cell(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,null);
    }
    
    /**
     * build a cell
     * @param levelID identifier of the level
     * @param networkID identifier of the network
     * @param cellID identifier of the cell
     * @param model set of walls
     */
    Cell(int levelID,int networkID,int cellID,Spatial model){
        super(levelID,networkID,cellID);
        portalsList=new ArrayList<Portal>();
        if(model!=null)
            attachChild(model);
    }
    
    void addPortal(Portal portal){
        portalsList.add(portal);
    }
    
    int getPortalCount(){
        return(portalsList.size());
    }
    
    Portal getPortalAt(int index){
        return(portalsList.get(index));
    }
}
