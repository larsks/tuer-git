package tools;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import drawer.IStaticVertexSet;
import drawer.VertexSetFactory;

public final class Full3DCellView{


    private IStaticVertexSet vertexSet;
    
    private Full3DCellController controller;
    
    private boolean visible;
    
    private transient List<Full3DCellView> neighboursCellsViewListsList;
    
    private GL gl;
    
    
    public Full3DCellView(GL gl){
        this.visible=false;
        this.gl=gl;
        this.neighboursCellsViewListsList=new ArrayList<Full3DCellView>();
    }
    
    
    public void draw(){
        vertexSet.draw();
    }

    public final void setController(Full3DCellController controller){
        this.controller=controller;
        //TODO: use a better approach to respect the software-components-based architecture
        //rather than using directly the package "drawer"
        vertexSet=VertexSetFactory.getInstance().getIStaticVertexSetInstance(this.gl,this.controller.getInternalBuffer(),GL.GL_QUADS);
    }

    public final Full3DCellController getController(){
        return(controller);
    }

    public final List<Full3DCellView> getNeighboursCellsViewListsList(){
        return(neighboursCellsViewListsList);
    }

    public final void setNeighboursCellsViewListsList(List<Full3DCellView> neighboursCellsViewListsList){
        this.neighboursCellsViewListsList=neighboursCellsViewListsList;
    }

    public final void setVisible(boolean visible){      
        this.visible=visible;
    }
    
    public final boolean getVisible(){
        return(visible);
    } 
}
