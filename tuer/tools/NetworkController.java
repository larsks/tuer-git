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

public final class NetworkController{
  
    
    private Full3DCellController rootCell;
    
    private List<Full3DCellController> cellsControllersList;
    
    private Network model;
    
    private NetworkView view;
    
    
    public NetworkController(Network model,List<Full3DCellController> cellsControllersList){       
        this.model=model;       
        this.model.setController(this);
        buildGraphFromList(cellsControllersList);
    }

    
    private final void buildGraphFromList(List<Full3DCellController> cellsControllersList){
        this.cellsControllersList=cellsControllersList;
        if(!cellsControllersList.isEmpty())
            {this.rootCell=cellsControllersList.get(0);
             Full3DCell cellModel;
             for(Full3DCellController cellController:cellsControllersList)
                 {cellModel=cellController.getModel();
                  for(int i=0;i<cellModel.getNeighboursCount();i++)
                      cellController.addPortalController(new Full3DPortalController(cellModel.getPortal(i)));
                 }
            }        
    }

    public final void setView(NetworkView view){
        this.view=view;
        this.view.setController(this);
    }

    public final Full3DCellController getRootCell(){
        return(rootCell);
    }
    
    public final Full3DCellController locate(float x,float y,float z){
        return(locate(x,y,z,null));
    }
    
    public final Full3DCellController locate(float x,float y,float z,Full3DCellController previousCellController){
        Full3DCellController initialCellController=(previousCellController!=null)?previousCellController:rootCell;
        Full3DCellController c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCellController> fifo=new ArrayList<Full3DCellController>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCellController> markedCellsList=new ArrayList<Full3DCellController>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(initialCellController);
        fifo.add(initialCellController);
        Full3DCellController son;
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment; if the point is in the cell, the travel ends            
             if(c.contains(x,y,z))
                 return(c);
             else
                 {for(int i=0;i<c.getNeighboursControllersCount();i++)
                      {son=c.getNeighbourCellController(i);
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


    public final List<Full3DCellController> getCellsControllersList(){
        return(cellsControllersList);
    }
}
