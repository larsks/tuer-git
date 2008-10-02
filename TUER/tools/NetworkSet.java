package tools;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * This class is a kind of decorator to use a network set as a network. A network set
 * contains several networks, i.e several connected graphs
 * @author Julien Gouesse
 *
 */
public final class NetworkSet implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private List<Network> networksList;
    
    public NetworkSet(){
        networksList=new ArrayList<Network>();
    }
    
    public NetworkSet(List<Full3DCell> full3DCellsList){
        networksList=new ArrayList<Network>();
        List<Full3DCell> cellsList=new ArrayList<Full3DCell>();
        cellsList.addAll(full3DCellsList);
        //connect each cell to its neighbors
        Network.connectCellsTogether(cellsList);       
        Full3DCell c;
        List<Full3DCell> subCellsList;
        Network network;
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCell> fifo=new ArrayList<Full3DCell>();
        while(!cellsList.isEmpty())
            {network=new Network();
             network.setRootCell(cellsList.get(0));  
             //now we use the BFS to get all connected cells of a single graph
             subCellsList=new ArrayList<Full3DCell>();
             markedCellsList.clear();
             //We use the first traveled cell suggested by the user
             markedCellsList.add(network.getRootCell());
             fifo.add(network.getRootCell());
             while(!fifo.isEmpty())
                 {//Get the first added element as it is a FIFO (pop operation)
                  c=fifo.remove(0);
                  //This is the main treatment, save all connected cells of a single graph
                  subCellsList.add(c);
                  for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(son);
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                          }
                 }
             network.setCellsList(subCellsList);
             networksList.add(network);
             //we remove the cells already used by the most recently created network
             cellsList.removeAll(subCellsList);            
            }
    }
    
    private final void writeObject(java.io.ObjectOutputStream out)throws IOException{
        out.writeObject(networksList);
    }
    
    private final void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
        networksList=(List<Network>)in.readObject();       
    }

    public final Entry<Full3DCell,Integer> locate(float x,float y,float z){
        return(locate(x,y,z,null));
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param previousPositioning: a couple composed of a cell and the index of the network
     * @return the new positioning
     */
    public final Entry<Full3DCell,Integer> locate(float x,float y,float z,Entry<Full3DCell,Integer> previousPositioning){
        int previousNetworkIndex;
        Full3DCell previousFull3DCell;
        if(previousPositioning!=null)
            {previousNetworkIndex=previousPositioning.getValue().intValue();
             previousFull3DCell=previousPositioning.getKey();
            }
        else
            {previousNetworkIndex=0;
             previousFull3DCell=networksList.get(0).getRootCell();
            }
        Network network;
        Full3DCell currentPositioningCell=null;
        final int networkCount=networksList.size();
        int currentNetworkIndex=-1;
        for(int networkIndex=previousNetworkIndex,j=0;j<networkCount&&currentPositioningCell==null;j++,networkIndex=(networkIndex+1)%networkCount)
            {network=networksList.get(networkIndex);
             if(networkIndex==previousNetworkIndex)
                 currentPositioningCell=network.locate(x,y,z,previousFull3DCell);
             else
                 currentPositioningCell=network.locate(x,y,z);  
             currentNetworkIndex=networkIndex;
            }
        Entry<Full3DCell,Integer> currentPositioning;
        if(currentPositioningCell!=null)
            currentPositioning=new SimpleEntry<Full3DCell,Integer>(currentPositioningCell,currentNetworkIndex);
        else
            //this should never happen, it means that you are outside all networks
            currentPositioning=null;
        return(currentPositioning);
    }
}
