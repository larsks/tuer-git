package tools;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import drawer.IStaticVertexSet;
import drawer.VertexSetFactory;

public final class Full3DCellView{


    private IStaticVertexSet vertexSet;
    
    private Full3DCellController controller;
    
    private transient List<Full3DCellView> neighboursCellsViewsList;
    
    
    public Full3DCellView(){
        this.neighboursCellsViewsList=new ArrayList<Full3DCellView>();
    }
    
    
    public void draw(){
        vertexSet.draw();
    }

    public final void setController(Full3DCellController controller){
        this.controller=controller;
        //TODO: use a better approach to respect the software-components-based architecture
        //rather than using directly the package "drawer"
        vertexSet=VertexSetFactory.getInstance().getIStaticVertexSetInstance(this.controller.getInternalBuffer(),GL.GL_QUADS);
    }

    public final Full3DCellController getController(){
        return(controller);
    }

    public final List<Full3DCellView> getNeighboursCellsViewsList(){
        return(neighboursCellsViewsList);
    }

    public final void setNeighboursCellsViewsList(List<Full3DCellView> neighboursCellsViewsList){
        this.neighboursCellsViewsList=neighboursCellsViewsList;
    }

    public final List<float[]> getNeighboursPortalsList(){
        return(controller.getNeighboursPortalsList());
    }

    public final Rectangle getEnclosingRectangle(){
        return(controller.getEnclosingRectangle());
    } 
}
