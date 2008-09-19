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
import java.util.List;

public final class Full3DCell implements Serializable{
    
    
    private static final long serialVersionUID = 1L;

    private transient List<Full3DCell> neighboursCellsList;
    
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
    
    
    public Full3DCell(){
        neighboursCellsList=new ArrayList<Full3DCell>();       
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
    }
    

    public final Object readResolve()throws ObjectStreamException{
        computeEnclosingRectangle();
        return(this);
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
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
                 if(vertex[4]<maxz) 
                     maxz=vertex[4];
            }
        enclosingRectangle=new Rectangle();
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
        //ordinate temporarily ignored
        return(this.enclosingRectangle.contains(point[0],point[2]));
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
}
