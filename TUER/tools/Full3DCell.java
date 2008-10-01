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

package tools;

import java.awt.Rectangle;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Full3DCell implements Serializable{
    
    
    private static final long serialVersionUID = 1L;

    private transient List<Full3DCell> neighboursCellsList;
    
    private transient List<float[]> neighboursPortalsList;
    
    private transient Full3DCellController controller;
    
    //TODO: rather use at least an axis-aligned bounding box
    private transient Rectangle enclosingRectangle;
    //each array contains a single vertex
    private List<float[]> topWalls;
    
    private List<float[]> bottomWalls;
    
    private List<float[]> topPortals;
    
    private List<float[]> bottomPortals;
    
    private List<float[]> leftWalls;
    
    private List<float[]> rightWalls;
    
    private List<float[]> leftPortals;
    
    private List<float[]> rightPortals;
    
    private List<float[]> ceilWalls;
    
    private List<float[]> floorWalls;
    
    private List<float[]> ceilPortals;
    
    private List<float[]> floorPortals;
    
    
    public Full3DCell(){
        neighboursCellsList=new ArrayList<Full3DCell>(); 
        neighboursPortalsList=new ArrayList<float[]>();
        topWalls=new ArrayList<float[]>();
        bottomWalls=new ArrayList<float[]>();
        topPortals=new ArrayList<float[]>();
        bottomPortals=new ArrayList<float[]>();
        leftWalls=new ArrayList<float[]>();
        rightWalls=new ArrayList<float[]>();
        leftPortals=new ArrayList<float[]>();
        rightPortals=new ArrayList<float[]>();
        ceilWalls=new ArrayList<float[]>();
        floorWalls=new ArrayList<float[]>();
        ceilPortals=new ArrayList<float[]>();
        floorPortals=new ArrayList<float[]>();
        enclosingRectangle=new Rectangle();
    }
    

    public final Object readResolve()throws ObjectStreamException{
        if(neighboursCellsList==null)
            neighboursCellsList=new ArrayList<Full3DCell>(); 
        if(neighboursPortalsList==null)
            neighboursPortalsList=new ArrayList<float[]>();
        if(enclosingRectangle==null)    
            enclosingRectangle=new Rectangle();
        computeEnclosingRectangle();
        controller=null;
        return(this);
    }
    
    public final boolean equals(Object o){
        if(o==null||!(o instanceof Full3DCell))
            return(false);
        else
            {Full3DCell c=(Full3DCell)o;
             return(topWalls.equals(c.getTopWalls())&&topPortals.equals(c.getTopPortals())&&
                    bottomWalls.equals(c.getBottomWalls())&&bottomPortals.equals(c.getBottomPortals())&&
                    leftWalls.equals(c.getLeftWalls())&&leftPortals.equals(c.getLeftPortals())&&
                    rightWalls.equals(c.getRightWalls())&&rightPortals.equals(c.getRightPortals()));
            }
    }
    
    /**
     * 
     * NB: THIS METHOD IS COSTLY (O(n²)), use it only when you have not yet 
     * computed the neighbors cells list
     * @param c1
     * @param c2
     * @return
     */
    final static List<float[]> getCommonPortalsList(Full3DCell c1,Full3DCell c2){
        //test if a left portal of c1 is a right portal of c2
        List<float[]> commonPortalsList=new ArrayList<float[]>();
        for(float[] c1LeftPortal:c1.leftPortals)
            for(float[] c2RightPortal:c2.rightPortals)
                if(Arrays.equals(c1LeftPortal,c2RightPortal))
                    commonPortalsList.add(c1LeftPortal);
        //test if a right portal of c1 is a left portal of c2
        for(float[] c1RightPortal:c1.rightPortals)
            for(float[] c2LeftPortal:c2.leftPortals)
                if(Arrays.equals(c1RightPortal,c2LeftPortal))
                    commonPortalsList.add(c1RightPortal);
        //test if a top portal of c1 is a bottom portal of c2
        for(float[] c1TopPortal:c1.topPortals)
            for(float[] c2BottomPortal:c2.bottomPortals)
                if(Arrays.equals(c1TopPortal,c2BottomPortal))
                    commonPortalsList.add(c1TopPortal);
        //test if a bottom portal of c1 is a top portal of c2
        for(float[] c1BottomPortal:c1.bottomPortals)
            for(float[] c2TopPortal:c2.topPortals)
                if(Arrays.equals(c1BottomPortal,c2TopPortal))
                    commonPortalsList.add(c1BottomPortal);
        return(commonPortalsList);
    }

    final void computeEnclosingRectangle(){
        float minx=Float.MAX_VALUE,minz=Float.MAX_VALUE,maxx=Float.MIN_VALUE,maxz=Float.MIN_VALUE;
        //compute the enclosing rectangle
        //remind: format T2_V3
        for(float[] vertex:topWalls)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:bottomWalls)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:leftWalls)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:rightWalls)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:topPortals)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:bottomPortals)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:leftPortals)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        for(float[] vertex:rightPortals)
            {if(vertex[2]<minx) 
                 minx=vertex[2];
             else
                 if(vertex[2]>maxx) 
                     maxx=vertex[2];
             if(vertex[4]<minz) 
                 minz=vertex[4];
             else
                 if(vertex[4]>maxz) 
                     maxz=vertex[4];
            }
        enclosingRectangle.setFrameFromDiagonal(minx,minz,maxx,maxz);
    }
    
    public final List<Full3DCell> getNeighboursCellsList(){
        return(neighboursCellsList);
    }

    public final void setNeighboursCellsList(List<Full3DCell> neighboursCellsList){
        this.neighboursCellsList=neighboursCellsList;
    }

    public final Rectangle getEnclosingRectangle(){
        return(enclosingRectangle);
    }

    public final List<float[]> getTopWalls(){
        return(topWalls);
    }

    public final void setTopWalls(List<float[]> topWalls){
        this.topWalls=topWalls;
    }

    public final List<float[]> getBottomWalls(){
        return(bottomWalls);
    }

    public final void setBottomWalls(List<float[]> bottomWalls){
        this.bottomWalls=bottomWalls;
    }

    public final List<float[]> getTopPortals(){
        return(topPortals);
    }

    public final void setTopPortals(List<float[]> topPortals){
        this.topPortals=topPortals;
    }

    public final List<float[]> getBottomPortals(){
        return(bottomPortals);
    }

    public final void setBottomPortals(List<float[]> bottomPortals){
        this.bottomPortals=bottomPortals;
    }

    public final List<float[]> getLeftWalls(){
        return(leftWalls);
    }

    public final void setLeftWalls(List<float[]> leftWalls){
        this.leftWalls=leftWalls;
    }

    public final List<float[]> getRightWalls(){
        return(rightWalls);
    }

    public final void setRightWalls(List<float[]> rightWalls){
        this.rightWalls = rightWalls;
    }

    public final List<float[]> getLeftPortals(){
        return(leftPortals);
    }

    public final void setLeftPortals(List<float[]> leftPortals){
        this.leftPortals=leftPortals;
    }

    public final List<float[]> getRightPortals() {
        return(rightPortals);
    }

    public final void setRightPortals(List<float[]> rightPortals){
        this.rightPortals=rightPortals;
    }

    public final void setEnclosingRectangle(Rectangle enclosingRectangle){
        this.enclosingRectangle=enclosingRectangle;
    }
    
    public final boolean contains(float[] point){
        return(contains(point[0],point[1],point[2]));
    }
    
    public final boolean contains(float x,float y,float z){
        //ordinate temporarily ignored
        return(this.enclosingRectangle.contains(x,z));
    }

    public final List<float[]> getCeilWalls(){
        return(ceilWalls);
    }

    public final void setCeilWalls(List<float[]> ceilWalls){
        this.ceilWalls=ceilWalls;
    }

    public final List<float[]> getFloorWalls(){
        return(floorWalls);
    }

    public final void setFloorWalls(List<float[]> floorWalls){
        this.floorWalls=floorWalls;
    }

    public final List<float[]> getNeighboursPortalsList(){
        return(neighboursPortalsList);
    }

    public final void setNeighboursPortalsList(List<float[]> neighboursPortalsList){
        this.neighboursPortalsList=neighboursPortalsList;
    }

    public final List<float[]> getCeilPortals(){
        return(ceilPortals);
    }

    public final void setCeilPortals(List<float[]> ceilPortals){
        this.ceilPortals=ceilPortals;
    }

    public final List<float[]> getFloorPortals(){
        return(floorPortals);
    }

    public final void setFloorPortals(List<float[]> floorPortals){
        this.floorPortals=floorPortals;
    }

    public final Full3DCellController getController(){
        return(controller);
    }

    public final void setController(Full3DCellController controller){
        this.controller=controller;
    }
}
