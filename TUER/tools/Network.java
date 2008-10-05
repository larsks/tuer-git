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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Network implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private Full3DCell rootCell;
    
    private List<Full3DCell> cellsList;
    
    private transient NetworkController controller;
    
    
    public Network(){}
    
    /**
     * Build a connected graph
     * @param full3DCellsList list of connected cells
     */
    public Network(List<Full3DCell> full3DCellsList){
        buildGraphFromList(full3DCellsList);
    }
    
    
    private final void writeObject(java.io.ObjectOutputStream out)throws IOException{
        out.writeObject(cellsList);
    }

    private final void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
        //read a list of cells
        List<Full3DCell> full3DCellsList=(List<Full3DCell>)in.readObject();
        buildGraphFromList(full3DCellsList);       
    }   
    
    private final void buildGraphFromList(List<Full3DCell> full3DCellsList){
        cellsList=full3DCellsList;              
        //the first cell becomes the root cell
        if(!full3DCellsList.isEmpty())
            {rootCell=full3DCellsList.get(0);
             connectCellsTogether(full3DCellsList);
            }
    }
    
    static final void connectCellsTogether(List<Full3DCell> full3DCellsList){
        List<float[]> commonPortalsList;
        //compute the neighbors list of each cell in order to rebuild the network
        int i=0,j;
        //for each cell, we look for its sons
        for(Full3DCell fatherCell:full3DCellsList)
            {j=0;
             for(Full3DCell fullCell:full3DCellsList)
                 {//if the current cell is not the father cell and if it is a son of the father cell
                  if(i!=j&&!(commonPortalsList=Full3DCell.getCommonPortalsList(fatherCell,fullCell)).isEmpty())
                      {//add the son cell into the neighbors cells list of the father cell
                       fatherCell.getNeighboursCellsList().add(fullCell);
                       //update the list of neighbors portals
                       fatherCell.getNeighboursPortalsList().addAll(commonPortalsList);
                      }
                  j++;
                 }
             i++;
            }
    }
    
    public final List<Full3DCell> getCellsList(){
        return(cellsList);
    }
    
    final Full3DCell locate(float x,float y,float z){
        return(locate(x,y,z,rootCell));
    }
    
    /*
     * Breadth First Search to locate the cell in which the point is.
     * BFS has been chosen because it is faster when we know that the player has gone 
     * to a close neighbor of the previous occupied cell
     */
    final Full3DCell locate(float x,float y,float z,Full3DCell firstTraveledCell){
        Full3DCell c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCell> fifo=new ArrayList<Full3DCell>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(firstTraveledCell);
        fifo.add(firstTraveledCell);
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment; if the point is in the cell, the travel ends            
             if(c.contains(x,y,z))
                 return(c);
             else
                 {for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(son);
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                          }
                 }
            }
        //FIXME: treat the case of single isolated cells (11 cells are isolated)
        //it is a bad fix, it falls back on the list
        /*for(Full3DCell cell:cellsList)
            if(cell.contains(x,y,z))
                return(cell);*/
        //It means that you are completely outside the network
        return(null);
    }
    
    
    /*public final void updateVisibleCellsList(SoftwareViewFrustumCullingPerformerModel frustum,float x,float y,float z,float direction){
        setRootCell(locate(x,y,z));
        updateVisibleCellsList(frustum,getRootCell());
    }*/
    
    /*
     * Breadth First Search to locate the cell in which the point is.
     * BFS has been chosen because it is faster when we know that the player has gone 
     * to a close neighbor of the previous occupied cell
     */
    /*private static final void updateVisibleCellsList(SoftwareViewFrustumCullingPerformerModel frustum,Full3DCell firstTraveledCell){
        //List<Full3DCell> visibleCellsList=new ArrayList<Full3DCell>();
        Full3DCell c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCell> fifo=new ArrayList<Full3DCell>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(firstTraveledCell);
        fifo.add(firstTraveledCell);
        int portalIndex;
        float[] p1,p2,p3,p4;
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //Add the cell into the list of visible cells 
             //visibleCellsList.add(c);
             //update the visibility
             //c.setVisible(true);
             portalIndex=0;
             for(Full3DCell son:c.getNeighboursCellsList())
                 {if(!markedCellsList.contains(son))
                      {//Mark the cell to avoid traveling it more than once
                       markedCellsList.add(son);
                       //check if the portal is visible to know whether to add the 
                       //cell into the FIFO                      
                       p1=c.getNeighboursPortalsList().get(portalIndex);
                       p2=c.getNeighboursPortalsList().get(portalIndex+1);
                       p3=c.getNeighboursPortalsList().get(portalIndex+2);
                       p4=c.getNeighboursPortalsList().get(portalIndex+3);
                       //dataOffset=2 because we use T2_V3
                       if(frustum.isQuadInViewFrustum(p1, p2, p3, p4,2))
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                      }
                  portalIndex+=4;
                 }
            }
        //return(visibleCellsList);
    }*/

    public final Full3DCell getRootCell(){
        return(rootCell);
    }

    public final void setRootCell(Full3DCell rootCell){
        this.rootCell=rootCell;
    }

    public final NetworkController getController(){
        return(controller);
    }

    public final void setController(NetworkController controller){
        this.controller=controller;
    }

    public final void setCellsList(List<Full3DCell> cellsList){
        this.cellsList=cellsList;
    }  
}
