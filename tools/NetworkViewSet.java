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
package tools;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import main.ViewFrustumCullingPerformer;

public final class NetworkViewSet{
    
    
    private List<NetworkView> networkViewsList;
    
    
    public NetworkViewSet(List<NetworkView> networkViewsList){
        this.networkViewsList=networkViewsList;
    }

    
    public final Entry<Full3DCellView,Integer> draw(float x,float y,float z,float direction,Entry<Full3DCellView,Integer> previousPositioning,ViewFrustumCullingPerformer frustum){
        Entry<Full3DCellView,Integer> currentPositioning=locate(x,y,z,previousPositioning);
        if(currentPositioning!=null)
            this.networkViewsList.get(currentPositioning.getValue().intValue()).draw(x,y,z,currentPositioning.getKey(),frustum);
        return(currentPositioning);
    }
    
    private final Entry<Full3DCellView,Integer> locate(float x,float y,float z,Entry<Full3DCellView,Integer> previousPositioning){
        int previousNetworkViewIndex;
        Full3DCellView previousFull3DCellView;
        if(previousPositioning!=null)
            {previousNetworkViewIndex=previousPositioning.getValue().intValue();
             previousFull3DCellView=previousPositioning.getKey();
            }
        else
            {previousNetworkViewIndex=0;
             previousFull3DCellView=networkViewsList.get(0).getRootCell();
            }
        NetworkView networkView;
        Full3DCellView currentPositioningCellView=null;
        final int networkViewCount=networkViewsList.size();
        int currentNetworkIndex=-1;
        for(int networkIndex=previousNetworkViewIndex,j=0;j<networkViewCount&&currentPositioningCellView==null;j++,networkIndex=(networkIndex+1)%networkViewCount)
            {networkView=networkViewsList.get(networkIndex);
             if(networkIndex==previousNetworkViewIndex)
                 currentPositioningCellView=networkView.locate(x,y,z,previousFull3DCellView);
             else
                 currentPositioningCellView=networkView.locate(x,y,z);  
             currentNetworkIndex=networkIndex;
            }
        Entry<Full3DCellView,Integer> currentPositioning;
        if(currentPositioningCellView!=null)
            currentPositioning=new SimpleEntry<Full3DCellView,Integer>(currentPositioningCellView,currentNetworkIndex);
        else
            //this should never happen, it means that you are outside all networks
            currentPositioning=null;
        return(currentPositioning);
    }
}
