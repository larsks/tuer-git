package tools;

import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public final class NetworkControllerSet{
    
    
    private List<NetworkController> networkControllersList;
    
    
    public NetworkControllerSet(NetworkSet networkSet){
        List<Full3DCellController> cellsControllersList;
        Full3DCellController cellController;
        this.networkControllersList=new ArrayList<NetworkController>();
        for(Network network:networkSet.getNetworksList())
            {cellsControllersList=new ArrayList<Full3DCellController>();
             for(Full3DCell cellModel:network.getCellsList())
                 {cellController=new Full3DCellController(cellModel);
                  cellsControllersList.add(cellController);                     
                 }
             //build the network controller
             networkControllersList.add(new NetworkController(network,cellsControllersList));
            }
    }
    
    /*@SuppressWarnings("unused")
    private final Entry<Full3DCellController,Integer> locate(float x,float y,float z,Entry<Full3DCellController,Integer> previousPositioning){
        int previousNetworkControllerIndex;
        Full3DCellController previousFull3DCellController;
        if(previousPositioning!=null)
            {previousNetworkControllerIndex=previousPositioning.getValue().intValue();
             previousFull3DCellController=previousPositioning.getKey();
            }
        else
            {previousNetworkControllerIndex=0;
             previousFull3DCellController=networkControllersList.get(0).getRootCell();
            }
        NetworkController networkController;
        Full3DCellController currentPositioningCellController=null;
        final int networkControllerCount=networkControllersList.size();
        int currentNetworkIndex=-1;
        for(int networkIndex=previousNetworkControllerIndex,j=0;j<networkControllerCount&&currentPositioningCellController==null;j++,networkIndex=(networkIndex+1)%networkControllerCount)
            {networkController=networkControllersList.get(networkIndex);
             if(networkIndex==previousNetworkControllerIndex)
                 currentPositioningCellController=networkController.locate(x,y,z,previousFull3DCellController);
             else
                 currentPositioningCellController=networkController.locate(x,y,z);  
             currentNetworkIndex=networkIndex;
            }
        Entry<Full3DCellController,Integer> currentPositioning;
        if(currentPositioningCellController!=null)
            currentPositioning=new SimpleEntry<Full3DCellController,Integer>(currentPositioningCellController,currentNetworkIndex);
        else
            //this should never happen, it means that you are outside all networks
            currentPositioning=null;
        return(currentPositioning);
    }*/

    public final List<NetworkController> getNetworkControllersList(){
        return(networkControllersList);
    }
}
