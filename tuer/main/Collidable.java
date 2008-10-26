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

import java.nio.FloatBuffer;
import java.util.List;

/**
 * This class handles the collision with 3 layers: 
 * - a bounding sphere
 * - an axis-aligned bounding box (that can be coupled with a transform)
 * - the vertices (implicit)
 * @author Julien Gouesse
 *
 */
public class Collidable extends Object3DModel{

    
    /**
     * array of the bounding spheres radius centered on the center of the object
     */
    private double[] boundingSphereRadiusArray;
    
    /**
     * array of the axis-aligned bounding boxes, each array contains
     * 2 points, each one contains the extremums
     */
    //private double[][][] boundingBoxesArray;
    
    
    //only for the XML encoding and decoding
    public Collidable(){}
    
    Collidable(float x,float y,float z,List<FloatBuffer> coordinatesBuffersList,
            List<AnimationInfo> animationList,float horizontalDirection,
            float verticalDirection,Clock internalClock){
        super(x,y,z,coordinatesBuffersList,animationList,horizontalDirection,
                verticalDirection,internalClock);
        final int size=coordinatesBuffersList.size();
        //compute the bounding spheres
        this.boundingSphereRadiusArray=new double[size];        
        int i,j;
        FloatBuffer coordinatesBuffer;
        double norm;
        float[] point=new float[5];
        for(i=0;i<size;i++)
            {boundingSphereRadiusArray[i]=0;
             coordinatesBuffer=coordinatesBuffersList.get(i);
             //we skip the texture coordinates
             for(j=0;j<coordinatesBuffer.capacity()/5;j+=5)
                 {coordinatesBuffer.get(point);
                  norm=Math.sqrt(point[2]*point[2]+point[3]*point[3]+point[4]*point[4]);
                  if(norm>boundingSphereRadiusArray[i])
                      boundingSphereRadiusArray[i]=norm;
                 }
             coordinatesBuffer.rewind();
            }
        //compute the bounding boxes
        /*this.boundingBoxesArray=new double[3][2][size];       
        for(i=0;i<size;i++)
            {boundingBoxesArray[0][0][i]=Double.MAX_VALUE;
             boundingBoxesArray[1][0][i]=Double.MAX_VALUE;
             boundingBoxesArray[2][0][i]=Double.MAX_VALUE;
             boundingBoxesArray[0][1][i]=0.0D;
             boundingBoxesArray[1][1][i]=0.0D;
             boundingBoxesArray[2][1][i]=0.0D;
             coordinatesBuffer=coordinatesBuffersList.get(i);             
             //TODO: use an offset as it contains texture coordinates too
             for(j=0;j<coordinatesBuffer.capacity()/3;j+=3)
                 {coordinatesBuffer.get(point);
                  //minimum
                  if(point[0]<boundingBoxesArray[0][0][i])
                      boundingBoxesArray[0][0][i]=point[0];
                  if(point[1]<boundingBoxesArray[1][0][i])
                      boundingBoxesArray[1][0][i]=point[1];
                  if(point[2]<boundingBoxesArray[2][0][i])
                      boundingBoxesArray[2][0][i]=point[2];
                  //maximum
                  if(point[0]>boundingBoxesArray[0][1][i])
                      boundingBoxesArray[0][1][i]=point[0];
                  if(point[1]>boundingBoxesArray[1][1][i])
                      boundingBoxesArray[1][1][i]=point[1];
                  if(point[2]>boundingBoxesArray[2][1][i])
                      boundingBoxesArray[2][1][i]=point[2];
                 }
            }*/
    }
    
    boolean intersectsWith(Collidable collidable){
        double radiusSumSquare,distanceSquare;
        radiusSumSquare=boundingSphereRadiusArray[currentFrameIndex]+
        collidable.boundingSphereRadiusArray[collidable.currentFrameIndex];
        radiusSumSquare*=radiusSumSquare;
        distanceSquare=(x-collidable.x)*(x-collidable.x)+
                       (y-collidable.y)*(y-collidable.y)+
                       (z-collidable.z)*(z-collidable.z);
        //use the bounding spheres at first
        if(distanceSquare<radiusSumSquare)
            {//TODO: perform finer tests, use bounding boxes with transforms
             //TODO: perform finest tests on the whole geometry
             return(true);
            }
        else
            return(false);
    }
    
    //only for the XML encoding and decoding
    //TODO: refactor classes that call this
    final double[] getBoundingSphereRadiusArray(){
        return(boundingSphereRadiusArray);
    }
    
    //only for the XML encoding and decoding
    final void setBoundingSphereRadiusArray(double[] boundingSphereRadiusArray){
        this.boundingSphereRadiusArray=boundingSphereRadiusArray;
    }
}
