package tools;

import java.util.ArrayList;
import java.util.List;

public final class NetworkView{
    
    
    private Full3DCellView rootCell;
    
    private List<Full3DCellView> cellsViewsList;
    
    private transient NetworkController controller;
    
    
    public NetworkView(List<Full3DCellView> cellsViewsList){
        buildGraphFromList(cellsViewsList);
    }
    
    
    private final void buildGraphFromList(List<Full3DCellView> cellsViewsList){
        this.cellsViewsList=cellsViewsList;
        for(Full3DCellView cellView:this.cellsViewsList)
            for(Full3DCellController cellController:cellView.getController().getNeighboursCellsControllersList())
                cellView.getNeighboursCellsViewsList().add(cellController.getView());               
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
    
    public final void hideAllCells(){       
        for(Full3DCellView cellView:cellsViewsList)
            cellView.setVisible(false);           
    }
    
    public final void draw(){
        /*Full3DCellView c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCellView> fifo=new ArrayList<Full3DCellView>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCellView> markedCellsList=new ArrayList<Full3DCellView>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(rootCell);
        fifo.add(rootCell);
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment; if the cell is visible, draw it, hide it and watch its neighbors            
             if(!c.getVisible())
                 continue;
             else                
                 {c.draw();
                  c.setVisible(false);
                  for(Full3DCellView son:c.getNeighboursCellsViewsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(son);
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                          }
                 }
            }  */ 
        for(Full3DCellView cellView:cellsViewsList)
            if(cellView.getVisible())
                {cellView.draw();
                 cellView.setVisible(false);
                }
    }
}
