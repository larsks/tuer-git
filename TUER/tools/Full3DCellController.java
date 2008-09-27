package tools;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sun.opengl.util.BufferUtil;

public final class Full3DCellController{
    
    
    //TODO: move it in the view
    private FloatBuffer internalBuffer;
    
    private List<Full3DCellController> neighboursCellsControllersList;
    
    private Full3DCell model;
    
    private Full3DCellView view;
    
    
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

    public final Full3DCell getModel(){
        return(model);
    }
    
    public final Rectangle getEnclosingRectangle(){
        return(model.getEnclosingRectangle());
    }

    public final List<float[]> getTopWalls(){
        return(model.getTopWalls());
    }

    public final List<float[]> getBottomWalls(){
        return(model.getBottomWalls());
    }

    public final List<float[]> getTopPortals(){
        return(model.getTopPortals());
    }

    public final List<float[]> getBottomPortals(){
        return(model.getBottomPortals());
    }

    public final List<float[]> getLeftWalls(){
        return(model.getLeftWalls());
    }

    public final List<float[]> getRightWalls(){
        return(model.getRightWalls());
    }

    public final List<float[]> getLeftPortals(){
        return(model.getLeftPortals());
    }

    public final List<float[]> getRightPortals() {
        return(model.getRightPortals());
    }
    
    public final boolean contains(float[] point){
        return(model.contains(point));
    }
    
    public final boolean contains(float x,float y,float z){
        return(model.contains(x,y,z));
    }

    public final List<float[]> getCeilWalls(){
        return(model.getCeilWalls());
    }

    public final List<float[]> getFloorWalls(){
        return(model.getFloorWalls());
    }

    public final List<float[]> getNeighboursPortalsList(){
        return(model.getNeighboursPortalsList());
    }

    public final List<float[]> getCeilPortals(){
        return(model.getCeilPortals());
    }

    public final List<float[]> getFloorPortals(){
        return(model.getFloorPortals());
    }
}
