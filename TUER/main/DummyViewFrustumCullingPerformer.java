package main;

import java.awt.Rectangle;

final class DummyViewFrustumCullingPerformer implements ViewFrustumCullingPerformer{

    private GameController gameController;
    
    //private Arc2D circularPart;
    
    private Rectangle rectangle;
    
    private static final float arcContributionSize=GameModel.factor*25;
    
    
    DummyViewFrustumCullingPerformer(GameController gameController){
        this.gameController=gameController;
        //this.circularPart=new Arc2D.Float();
        rectangle=new Rectangle();
    }
    
    @Override
    public final void computeViewFrustum() {
        //circularPart.setArcByCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos(),arcContributionSize,(float)(gameController.getPlayerDirection()*180/Math.PI)+180,180,Arc2D.PIE);
        rectangle.setFrameFromCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos(),gameController.getPlayerXpos()+arcContributionSize,gameController.getPlayerZpos()+arcContributionSize);
    }

    @Override
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        final int[] indirectionTable=new int[]{0+dataOffset,1+dataOffset,2+dataOffset};
        /*float minx=Math.min(p1[indirectionTable[0]],Math.min(p2[indirectionTable[0]],Math.min(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float maxx=Math.max(p1[indirectionTable[0]],Math.max(p2[indirectionTable[0]],Math.max(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float minz=Math.min(p1[indirectionTable[2]],Math.min(p2[indirectionTable[2]],Math.min(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        float maxz=Math.max(p1[indirectionTable[2]],Math.max(p2[indirectionTable[2]],Math.max(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        if(minx==maxx)
            return(this.circularPart.intersects(minx,minz,1,maxz-minz));
        else
            if(minz==maxz)
                return(this.circularPart.intersects(minx,minz,maxx-minx,1));
            else
                return(false);*/ 
        //return(true);
        /*Rectangle2D playerRect=circularPart.getBounds2D();
        return(playerRect.intersectsLine(p1[2],p1[4],p2[2],p2[4])||
                playerRect.intersectsLine(p2[2],p2[4],p3[2],p3[4])||
                playerRect.intersectsLine(p3[2],p3[4],p4[2],p4[4])||
                playerRect.intersectsLine(p4[2],p4[4],p1[2],p1[4]));*/
        return(rectangle.intersectsLine(p1[indirectionTable[0]],p1[indirectionTable[2]],p2[indirectionTable[0]],p2[indirectionTable[2]])||
                rectangle.intersectsLine(p2[indirectionTable[0]],p2[indirectionTable[2]],p3[indirectionTable[0]],p3[indirectionTable[2]])||
                rectangle.intersectsLine(p3[indirectionTable[0]],p3[indirectionTable[2]],p4[indirectionTable[0]],p4[indirectionTable[2]])||
                rectangle.intersectsLine(p4[indirectionTable[0]],p4[indirectionTable[2]],p1[indirectionTable[0]],p1[indirectionTable[2]]));
    }

    @Override
    public final void updateProjectionMatrix(){}
}
