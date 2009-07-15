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

/**
 * This class has the role of the model  
 * (in the meaning of the design pattern "MVC") 
 * of a 3D object
 * @author Julien Gouesse
 */

package main;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;


//TODO : add a mechanism to update the buffers inside the view 
//if the buffers inside the model are changed
public class Object3DModel{

    
    /**
     * list of coordinates buffer, a coordinates buffer per frame,
     * coordinates centered on (0,0,0)
     */
    protected List<FloatBuffer> coordinatesBuffersList;
    
    /**
     * last start of an animation
     */
    protected long lastStartOfAnimationTime;
    
    protected float x;
    
    protected float y;
    
    protected float z;
    
    protected float horizontalDirection;//in degrees
    
    protected float verticalDirection;//in degrees
    
    protected int currentFrameIndex;
    
    protected int currentAnimationIndex;
    
    protected List<AnimationInfo> animationList;
    
    protected Object3DController controller;
    
    private Clock internalClock;
    
    // 3 vertices + 2 texture coordinates
    protected final static int coordinatesPerPrimitive = 5;
    
    
    Object3DModel(){
        this(0.0f,0.0f,0.0f,new Vector<FloatBuffer>(0),new Vector<AnimationInfo>(0),0.0f,0.0f,null);
    }
    
    Object3DModel(float x,float y,float z,Clock internalClock){
        this(x,y,z,new Vector<FloatBuffer>(0),new Vector<AnimationInfo>(0),0.0f,0.0f,internalClock);       
    }
    
    Object3DModel(float x,float y,float z,List<FloatBuffer> coordinatesBuffersList,Clock internalClock){
        this(x,y,z,coordinatesBuffersList,new Vector<AnimationInfo>(0),0.0f,0.0f,internalClock);             
    }
    
    Object3DModel(float x,float y,float z,List<FloatBuffer> coordinatesBuffersList,
            List<AnimationInfo> animationList,float horizontalDirection,
            float verticalDirection,Clock internalClock){
        this.coordinatesBuffersList=coordinatesBuffersList;        
        this.x=x;
        this.y=y;
        this.z=z;
        this.horizontalDirection=horizontalDirection;
        this.verticalDirection=verticalDirection;
        this.currentFrameIndex=0;
        this.currentAnimationIndex=0;
        this.animationList=new Vector<AnimationInfo>();
        this.animationList.addAll(animationList);
        this.controller=null;
        this.internalClock=internalClock;
        this.lastStartOfAnimationTime=getElapsedTime();
    }
    
    
    public List<FloatBuffer> getCoordinatesBuffersList(){
        return(coordinatesBuffersList);
    }
    
    FloatBuffer getCoordinatesBuffer(int index){
        return(coordinatesBuffersList.get(index));
    }
    
    public void setCoordinatesBuffersList(List<FloatBuffer> coordinatesBuffersList){
        this.coordinatesBuffersList=coordinatesBuffersList;
    }
    
    void setCoordinatesBuffer(int index,FloatBuffer coordinatesBuffer){
        if(index<this.coordinatesBuffersList.size())
            this.coordinatesBuffersList.set(index,coordinatesBuffer);
        else
            if(index==this.coordinatesBuffersList.size())
                this.coordinatesBuffersList.add(coordinatesBuffer);
    }       
    
    public long getLastStartOfAnimationTime(){
        return(lastStartOfAnimationTime);
    }
    
    public void setLastStartOfAnimationTime(long lastStartOfAnimationTime){
        this.lastStartOfAnimationTime=lastStartOfAnimationTime;
    }
    
    public float getX(){
        return(x);
    }
    
    public void setX(float x){
        this.x=x;
    }
    
    public float getY(){
        return(y);
    }
    
    public void setY(float y){
        this.y=y;
    }
    
    public float getZ(){
        return(z);
    }
    
    public void setZ(float z){
        this.z=z;
    }
    
    public float getHorizontalDirection(){
        return(horizontalDirection);
    }
    
    public void setHorizontalDirection(float horizontalDirection){
        this.horizontalDirection=horizontalDirection;
    }
    
    public float getVerticalDirection(){
        return(verticalDirection);
    }
    
    public void setVerticalDirection(float verticalDirection){
        this.verticalDirection=verticalDirection;
    }
    
    public int getCurrentFrameIndex(){
        return(currentFrameIndex);
    }
    
    public void setCurrentFrameIndex(int currentFrameIndex){
        this.currentFrameIndex=currentFrameIndex;	
    }
            
    public int getCurrentAnimationIndex(){
        return(currentAnimationIndex);
    }
    
    public void setCurrentAnimationIndex(int currentAnimationIndex){
        this.currentAnimationIndex=currentAnimationIndex;	
    }
    
    void toggleCurrentAnimationIndex(int currentAnimationIndex){
        this.currentAnimationIndex=currentAnimationIndex;
        this.lastStartOfAnimationTime=getElapsedTime();
    }
    
    public List<AnimationInfo> getAnimationList(){
        return(animationList);
    }
    
    AnimationInfo getAnimation(int index){
        return(animationList.get(index));
    }
    
    public void setAnimationList(List<AnimationInfo> animationList){
        this.animationList=animationList;
    }
    
    public Object3DController getController(){
        return(controller);
    }
    
    public void setController(Object3DController controller){
        this.controller=controller;
    }
    
    void setLocation(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    void translate(float dx,float dy,float dz){
        this.x+=dx;
        this.y+=dy;
        this.z+=dz;
    }
    
    void updateFrameIndex(){
        updateAnimationIndex();
        this.currentFrameIndex=(int)((((getElapsedTime()-lastStartOfAnimationTime)*
	                       animationList.get(currentAnimationIndex).getFramesPerSecond())/1000)
			        %animationList.get(currentAnimationIndex).getFrameCount());
    }
    
    //dummy implementation as some objects have only one animation
    void updateAnimationIndex(){}
    
    /**
     * By default, synchronize the time on the system.
     * It is highly recommended to override this behavior.
     * @return the current time
     */
    protected long getElapsedTime(){
        return(internalClock!=null?internalClock.getElapsedTime():System.currentTimeMillis());
    }
    
    public final void setInternalClock(Clock internalClock){
        this.internalClock=internalClock;
    }
    
    //only for the XML encoding and decoding
    public final Clock getInternalClock(){
        return(internalClock);
    }
    
    protected void dispose(){
        if(this.controller!=null)
            this.controller.dispose();           
    }
}
