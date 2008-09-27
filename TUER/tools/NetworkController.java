package tools;

import java.util.List;

public final class NetworkController{
    
    private Full3DCellController rootCell;
    
    //private List<Full3DCellController> cellsControllersList;
    
    private Network model;
    
    private NetworkView view;
    
    
    public NetworkController(Network model,NetworkView view,List<Full3DCellController> cellsControllersList){       
        this.model=model;
        this.view=view;
        this.view.setController(this);
        this.model.setController(this);        
        buildGraphFromList(cellsControllersList);
    }

    
    private final void buildGraphFromList(List<Full3DCellController> cellsControllersList){
        //this.cellsControllersList=cellsControllersList;
        for(Full3DCellController cellController:cellsControllersList)
            for(Full3DCell cellModel:cellController.getModel().getNeighboursCellsList())
                cellController.getNeighboursCellsControllersList().add(cellModel.getController());
    }
    
    public final void setRootCell(Full3DCellController rootCell){
        this.rootCell=rootCell;
        view.setRootCell(this.rootCell.getView());
    }
}
