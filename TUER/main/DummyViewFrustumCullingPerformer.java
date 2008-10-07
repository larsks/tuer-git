package main;

import java.awt.Rectangle;

final class DummyViewFrustumCullingPerformer implements ViewFrustumCullingPerformer{

    private GameController gameController;
    
    //private Rectangle rectangle;
    
    private Rectangle bottomRectangle;
    
    private Rectangle topRectangle;
    
    private Rectangle leftRectangle;
    
    private Rectangle rightRectangle;
    
    private static final float arcContributionSize=GameModel.factor*25;
    
    
    DummyViewFrustumCullingPerformer(GameController gameController){
        this.gameController=gameController;
        //rectangle=new Rectangle();
        bottomRectangle=new Rectangle();
        topRectangle=new Rectangle();
        leftRectangle=new Rectangle();
        rightRectangle=new Rectangle();
    }
    
    @Override
    public final void computeViewFrustum() {
        //rectangle.setFrameFromCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos(),gameController.getPlayerXpos()+arcContributionSize,gameController.getPlayerZpos()+arcContributionSize);
        bottomRectangle.setFrameFromCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos()-(arcContributionSize/2),gameController.getPlayerXpos()+arcContributionSize,gameController.getPlayerZpos());
        topRectangle.setFrameFromCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos()+(arcContributionSize/2),gameController.getPlayerXpos()+arcContributionSize,gameController.getPlayerZpos()+arcContributionSize);
        leftRectangle.setFrameFromCenter(gameController.getPlayerXpos()-(arcContributionSize/2),gameController.getPlayerZpos(),gameController.getPlayerXpos(),gameController.getPlayerZpos()+arcContributionSize);
        rightRectangle.setFrameFromCenter(gameController.getPlayerXpos()+(arcContributionSize/2),gameController.getPlayerZpos(),gameController.getPlayerXpos()+arcContributionSize,gameController.getPlayerZpos()+arcContributionSize);
    }

    @Override
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        final int[] indirectionTable=new int[]{0+dataOffset,1+dataOffset,2+dataOffset};
        float minx=Math.min(p1[indirectionTable[0]],Math.min(p2[indirectionTable[0]],Math.min(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float maxx=Math.max(p1[indirectionTable[0]],Math.max(p2[indirectionTable[0]],Math.max(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float minz=Math.min(p1[indirectionTable[2]],Math.min(p2[indirectionTable[2]],Math.min(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        float maxz=Math.max(p1[indirectionTable[2]],Math.max(p2[indirectionTable[2]],Math.max(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        //System.out.println("angle:"+gameController.getPlayerDirection()*180/Math.PI);
        if(gameController.getPlayerDirection()>=0&&gameController.getPlayerDirection()<Math.PI/2)
            return(topRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz))||rightRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));
        else
            if(gameController.getPlayerDirection()>=Math.PI/2&&gameController.getPlayerDirection()<Math.PI)
                return(bottomRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz))||rightRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));
            else
                if(gameController.getPlayerDirection()>=Math.PI&&gameController.getPlayerDirection()<1.5*Math.PI)
                    return(bottomRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz))||leftRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));
                else
                    if(gameController.getPlayerDirection()>=1.5*Math.PI&&gameController.getPlayerDirection()<2*Math.PI)
                        return(topRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz))||leftRectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));
                    else
                        return(false);
        //return(rectangle.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));
    }

    @Override
    public final void updateProjectionMatrix(){}
}
