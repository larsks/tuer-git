/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package main;

import java.awt.geom.Arc2D;

final class DummyViewFrustumCullingPerformer implements ViewFrustumCullingPerformer{

    private GameController gameController;
    
    private Arc2D circularFrustum;
    
    private static final float arcContributionSize=GameController.factor*25;
    
    
    DummyViewFrustumCullingPerformer(GameController gameController){
        this.gameController=gameController;
        this.circularFrustum=new Arc2D.Float();
    }
    
    @Override
    public final void computeViewFrustum(){
        circularFrustum.setArcByCenter(gameController.getPlayerXpos(),gameController.getPlayerZpos(),arcContributionSize,(gameController.getPlayerDirection()*180/Math.PI)-180,180,Arc2D.PIE);
    }

    @Override
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        final int[] indirectionTable=new int[]{0+dataOffset,1+dataOffset,2+dataOffset};
        float minx=Math.min(p1[indirectionTable[0]],Math.min(p2[indirectionTable[0]],Math.min(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float maxx=Math.max(p1[indirectionTable[0]],Math.max(p2[indirectionTable[0]],Math.max(p3[indirectionTable[0]],p4[indirectionTable[0]])));
        float minz=Math.min(p1[indirectionTable[2]],Math.min(p2[indirectionTable[2]],Math.min(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        float maxz=Math.max(p1[indirectionTable[2]],Math.max(p2[indirectionTable[2]],Math.max(p3[indirectionTable[2]],p4[indirectionTable[2]])));
        //System.out.println("angle:"+gameController.getPlayerDirection()*180/Math.PI);       
        return(circularFrustum.intersects(minx,minz,Math.max(1,maxx-minx),Math.max(1,maxz-minz)));       
    }

    @Override
    public final void updateProjectionMatrix(){}
}
