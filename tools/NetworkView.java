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

import java.util.ArrayList;
import java.util.List;
import main.ViewFrustumCullingPerformer;

public final class NetworkView{
    
    
    private Full3DCellView rootCell;
    
    private List<Full3DCellView> cellsViewsList;
    
    private transient NetworkController controller;
    
    
    public NetworkView(List<Full3DCellView> cellsViewsList){
        buildGraphFromList(cellsViewsList);
    }
    
    
    private final void buildGraphFromList(List<Full3DCellView> cellsViewsList){
        this.cellsViewsList=cellsViewsList;
        if(!this.cellsViewsList.isEmpty())
            {this.rootCell=cellsViewsList.get(0);
             Full3DCellController cellController;
             for(Full3DCellView cellView:this.cellsViewsList)
                 {cellController=cellView.getController();
                  for(int i=0;i<cellController.getNeighboursControllersCount();i++)
                      cellView.addPortalView(new Full3DPortalView(cellController.getPortalController(i)));
                 }
            }                     
    }
    
    public final NetworkController getController(){
        return(controller);
    }

    public final void setController(NetworkController controller){
        this.controller=controller;
    }  
    
    public final void setRootCell(Full3DCellView rootCell){
        this.rootCell=rootCell;
    }
    
    public final void draw(float x,float y,float z,Full3DCellView cellView,ViewFrustumCullingPerformer frustum){      
        Full3DCellView c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCellView> fifo=new ArrayList<Full3DCellView>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCellView> markedCellsList=new ArrayList<Full3DCellView>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(cellView);
        fifo.add(cellView);
        float[][] portalVertices;
        Full3DCellView son;
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment, draw the cell and watch its neighbors               
             c.draw();
             //for each portal of this cell
             for(int i=0;i<c.getNeighboursViewsCount();i++)
                 {portalVertices=c.getPortalView(i).getPortalVertices();
                  son=c.getNeighbourCellView(i);
                  if(!markedCellsList.contains(son))
                      {//Mark the cell to avoid traveling it more than once
                       markedCellsList.add(son);
                       //check if the portal is visible to know whether to add the 
                       //cell into the FIFO  
                       //dataOffset=2 because we use T2_V3
                       if(frustum.isQuadInViewFrustum(portalVertices[0],
                               portalVertices[1],portalVertices[2],
                               portalVertices[3],2))
                           {//Add a new cell to travel (push operation)
                            fifo.add(son);
                           }
                      }                 
                 }
            }  
    }

    public final Full3DCellView getRootCell(){
        return(rootCell);
    }

    public final Full3DCellView locate(float x,float y,float z,Full3DCellView previousCellView){
        Full3DCellView initialCellView=(previousCellView!=null)?previousCellView:rootCell;
        Full3DCellView c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCellView> fifo=new ArrayList<Full3DCellView>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCellView> markedCellsList=new ArrayList<Full3DCellView>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(initialCellView);
        fifo.add(initialCellView);
        Full3DCellView son;
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment; if the point is in the cell, the travel ends            
             if(c.contains(x,y,z))
                 return(c);
             else
                 {for(int i=0;i<c.getNeighboursViewsCount();i++)
                      {son=c.getNeighbourCellView(i);
                       if(!markedCellsList.contains(son))
                           {//Mark the cell to avoid traveling it more than once
                            markedCellsList.add(son);
                            //Add a new cell to travel (push operation)
                            fifo.add(son);
                           }
                      }
                 }
            }       
        return(null);
    }
    
    public final Full3DCellView locate(float x,float y,float z){
        return(locate(x,y,z,null));
    }
}
