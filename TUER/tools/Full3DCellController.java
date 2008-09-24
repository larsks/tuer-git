package tools;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sun.opengl.util.BufferUtil;

public final class Full3DCellController{
    
    
    private transient FloatBuffer internalBuffer;
    
    private transient List<Full3DCellController> neighboursCellsControllersList;
    
    private transient Full3DCell model;
    
    private transient Full3DCellView view;
    
    
    public Full3DCellController(Full3DCell full3DCellModel,Full3DCellView full3DCellView){
        this.neighboursCellsControllersList=new ArrayList<Full3DCellController>();
        this.model=full3DCellModel;
        this.model.setController(this);       
        this.internalBuffer=BufferUtil.newFloatBuffer(
                (model.getBottomWalls().size()+
                 model.getTopWalls().size()+
                 model.getLeftWalls().size()+
                 model.getRightWalls().size()+
                 model.getCeilWalls().size()+
                 model.getFloorWalls().size())*5);//5 because T2_V3
        for(float[] wall:model.getBottomWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getTopWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getLeftWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getRightWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getCeilWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getFloorWalls())
            this.internalBuffer.put(wall);
        this.internalBuffer.rewind();
        //the view is bound to its controller here because now, the buffer is ready
        this.view=full3DCellView;
        this.view.setController(this);        
    }


    public final FloatBuffer getInternalBuffer(){
        return(internalBuffer);
    }

    public final Full3DCellView getView(){
        return(view);
    }

    public final List<Full3DCellController> getNeighboursCellsControllersList(){
        return(neighboursCellsControllersList);
    }

    public final void setNeighboursCellsControllersList(List<Full3DCellController> neighboursCellsControllersList){
        this.neighboursCellsControllersList=neighboursCellsControllersList;
    }

    public final Full3DCell getModel(){
        return(model);
    }

    public final void setVisible(boolean visible){       
        this.view.setVisible(visible);
    }
}
