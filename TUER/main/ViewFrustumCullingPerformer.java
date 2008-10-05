package main;

public interface ViewFrustumCullingPerformer{
    
    void updateProjectionMatrix();
    void computeViewFrustum();
    boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset);
}
