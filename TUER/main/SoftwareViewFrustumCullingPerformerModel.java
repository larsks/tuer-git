package main;

public final class SoftwareViewFrustumCullingPerformerModel{
    
    
    private SoftwareViewFrustumCullingPerformerController controller;
    
    
    public SoftwareViewFrustumCullingPerformerModel(){}
    
    
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        return(controller.isQuadInViewFrustum(p1,p2,p3,p4,dataOffset));
    }
    
    public final void setController(SoftwareViewFrustumCullingPerformerController controller){
        this.controller=controller;
    }
}
