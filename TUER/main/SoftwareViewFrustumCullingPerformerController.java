package main;

public final class SoftwareViewFrustumCullingPerformerController{
    
    
    private SoftwareViewFrustumCullingPerformerModel model;
    
    private SoftwareViewFrustumCullingPerformer view;
    
    
    public SoftwareViewFrustumCullingPerformerController(SoftwareViewFrustumCullingPerformerModel model,
            SoftwareViewFrustumCullingPerformer view){
        this.model=model;
        this.model.setController(this);
        this.view=view;
        this.view.setController(this);
    }
    
    
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        return(view.isQuadInViewFrustum(p1,p2,p3,p4,dataOffset));
    }

}
