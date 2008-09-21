package tools;

import javax.media.opengl.GL;

import drawer.IStaticVertexSet;
import drawer.VertexSetFactory;

public final class Full3DCellView{


    private IStaticVertexSet vertexSet;
    
    
    public Full3DCellView(Full3DCellController full3DCellController,GL gl){
        //TODO: use a better approach to respect the software-components-based architecture
        //rather than using directly the package "drawer"
        vertexSet=VertexSetFactory.getInstance().getIStaticVertexSetInstance(gl,full3DCellController.getInternalBuffer(),GL.GL_QUADS);       
    }
    
    public void draw(){
        vertexSet.draw();
    }
}
