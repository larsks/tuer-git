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
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

import bean.NodeIdentifier;

final class Level extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    Level(){
        this(NodeIdentifier.unknownID);
    }
    
    Level(int levelID){
        super(levelID);
    }
    
    @Override
    public final int attachChild(Spatial child){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChild(child));
    }
    
    @Override
    public final int attachChildAt(Spatial child, int index){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChildAt(child,index));
    }
    
    final Cell locate(Vector3f position,Cell previousLocation){
        Cell location=null;
        int previousNetworkIndex=previousLocation!=null?previousLocation.getNetworkID():0;
        int networkCount=getChildren()!=null?getChildren().size():0;
        Network networkNode;
        for(int networkIndex=previousNetworkIndex,j=0;j<networkCount&&location==null;j++,networkIndex=(networkIndex+1)%networkCount)
            {networkNode=(Network)getChild(networkIndex);
             if(networkIndex==previousNetworkIndex && previousLocation!=null)
                 location=networkNode.locate(position,previousLocation);
             else
                 location=networkNode.locate(position);
            }
        return(location);
    }
    
    final List<IdentifiedNode> getVisibleNodesList(Cell currentLocation){
        List<IdentifiedNode> visibleNodesList=new ArrayList<IdentifiedNode>();
        if(currentLocation!=null)
            {//FIXME: we should check if the level is in the view frustum
             //add the level node
             visibleNodesList.add(this);
             //add the network node
             Network networkNode=(Network)currentLocation.getParent();
             visibleNodesList.add(networkNode);            
             //look for other visible cells in the network
             visibleNodesList.addAll(networkNode.getVisibleNodesList(currentLocation));
            }
        return(visibleNodesList);
    }
}
