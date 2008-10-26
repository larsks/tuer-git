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

public final class NetworkController{
  
    
    private Network model;
    
    private NetworkView view;
    
    
    public NetworkController(Network model,List<Full3DCellController> cellsControllersList){       
        this.model=model;       
        this.model.setController(this);        
        buildGraphFromList(cellsControllersList);
    }

    
    private final void buildGraphFromList(List<Full3DCellController> cellsControllersList){
        for(Full3DCellController cellController:cellsControllersList)
            for(Full3DCell cellModel:cellController.getModel().getNeighboursCellsList())
                cellController.addNeighbourCellController(cellModel.getController());
    }


    public final void setView(NetworkView view){
        this.view = view;
        this.view.setController(this);
    }
}
