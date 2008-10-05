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
                cellController.getNeighboursCellsControllersList().add(cellModel.getController());
    }


    public final void setView(NetworkView view){
        this.view = view;
        this.view.setController(this);
    }
}
