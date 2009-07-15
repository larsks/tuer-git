package tools;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import drawer.IStaticVertexSet;
import drawer.VertexSetFactory;

public final class Full3DCellView{


    private IStaticVertexSet vertexSet;
    
    private Full3DCellController controller;
    
    private List<Full3DPortalView> portalsViewsList;
    
    
    public Full3DCellView(Full3DCellController controller){
        this.portalsViewsList=new ArrayList<Full3DPortalView>();
        controller.setView(this);
        setController(controller);
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
    
    public final void addPortalView(Full3DPortalView portalView){
        portalsViewsList.add(portalView);
    }
    
    public final int getNeighboursViewsCount(){
        return(portalsViewsList.size());
    }
    
    public final Full3DPortalView getPortalView(int index){
        return(portalsViewsList.get(index));
    }
    
    public final Full3DCellView getNeighbourCellView(int index){
        Full3DCellView[] linkedCellsViews=portalsViewsList.get(index).getLinkedCellsViews();
        return(linkedCellsViews[0]==this?linkedCellsViews[1]:linkedCellsViews[0]);
    }

    public final boolean contains(float x,float y,float z){
        //ordinate temporarily ignored
        return(controller.getEnclosingRectangle().contains(x,z));
    }
}
