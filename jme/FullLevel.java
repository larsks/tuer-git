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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import bean.NodeIdentifier;

import com.jme.math.Vector3f;

public final class FullLevel implements Serializable{

    
    private static final long serialVersionUID = 1L;

    static{TransientMarkerForXMLSerialization.updateTransientModifierForXMLSerialization(FullLevel.class);}
    
    private Vector3f initialPlayerPosition;
    
    private NodeIdentifier[] nodeIdentifiers;
    
    private HashMap<Vector3f,String> entityLocationTable;
    
    private transient Level levelNode;
    
    
    public FullLevel(){}


    public final Vector3f getInitialPlayerPosition(){
        return(initialPlayerPosition);
    }

    public final void setInitialPlayerPosition(Vector3f initialPlayerPosition){
        this.initialPlayerPosition=initialPlayerPosition;
    }

    public final NodeIdentifier[] getNodeIdentifiers(){
        return(nodeIdentifiers);
    }

    public final void setNodeIdentifiers(NodeIdentifier[] nodeIdentifiers){
        this.nodeIdentifiers=nodeIdentifiers;
    }

    public final HashMap<Vector3f,String> getEntityLocationTable(){
        return(entityLocationTable);
    }

    public final void setEntityLocationTable(HashMap<Vector3f,String> entityLocationTable){
        this.entityLocationTable=entityLocationTable;
    }
    
    public final Level getLevelNode(FullWorld world){
        if(levelNode==null)
            {int levelIndex=nodeIdentifiers[0].getLevelID();
             try{levelNode=new Level(levelIndex,nodeIdentifiers);} 
             catch(IOException ioe)
             {throw new RuntimeException("The creation of the level has failed!",ioe);}
             //TODO: use the entity table
            }       
        return(levelNode);
    }
}
