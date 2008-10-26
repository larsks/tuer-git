package main;

import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;

import com.sun.opengl.util.BufferUtil;


public final class Object3DModelBean implements XMLTransportableWrapper<Object3DModel>{

    
    private List<float[]> coordinatesBuffersList;
    
    private float x;
    
    private float y;
    
    private float z;
    
    private float horizontalDirection;//in degrees
    
    private float verticalDirection;//in degrees
    
    private int currentFrameIndex;
    
    private int currentAnimationIndex;
    
    private List<AnimationInfoBean> animationList;
    
    
    public Object3DModelBean(){}
    
    
    @Override
    public final Object3DModel getWrappedObject(){
        //convert float arrays into float buffers
        List<FloatBuffer> trueCoordinatesBuffersList=new ArrayList<FloatBuffer>();
        FloatBuffer buffer;
        for(float[] array:coordinatesBuffersList)
            {buffer=BufferUtil.newFloatBuffer(array.length);
             buffer.put(array);
             buffer.rewind();
             trueCoordinatesBuffersList.add(buffer);
            }
        //convert animation info beans into animation info
        List<AnimationInfo> trueAnimationList=new ArrayList<AnimationInfo>();
        for(AnimationInfoBean aib:animationList)
            trueAnimationList.add(aib.getWrappedObject());
        return(new Object3DModel(x,y,z,trueCoordinatesBuffersList,
                trueAnimationList,horizontalDirection,
                verticalDirection,null));               
    }

    @Override
    public final void wrap(Object3DModel o3dm){
        x=o3dm.getX();
        y=o3dm.getY();
        z=o3dm.getZ();
        horizontalDirection=o3dm.getHorizontalDirection();
        verticalDirection=o3dm.getVerticalDirection();
        currentFrameIndex=o3dm.getCurrentFrameIndex();
        currentAnimationIndex=o3dm.getCurrentAnimationIndex();
        coordinatesBuffersList=new ArrayList<float[]>();
        float[] tmpArray;
        for(FloatBuffer fb:o3dm.coordinatesBuffersList)
            {tmpArray=new float[fb.capacity()];
             fb.rewind();
             fb.get(tmpArray);
             fb.rewind();
             coordinatesBuffersList.add(tmpArray);            
            }
        animationList=new ArrayList<AnimationInfoBean>();
        AnimationInfoBean aib;
        for(AnimationInfo ai:o3dm.getAnimationList())
            {aib=new AnimationInfoBean();
             aib.wrap(ai);
             animationList.add(aib);
            }
    }

    public final float getX() {
        return x;
    }

    public final void setX(float x) {
        this.x = x;
    }

    public final float getY() {
        return y;
    }

    public final void setY(float y) {
        this.y = y;
    }

    public final float getZ() {
        return z;
    }

    public final void setZ(float z) {
        this.z = z;
    }

    public final float getHorizontalDirection() {
        return horizontalDirection;
    }

    public final void setHorizontalDirection(float horizontalDirection) {
        this.horizontalDirection = horizontalDirection;
    }

    public final float getVerticalDirection() {
        return verticalDirection;
    }

    public final void setVerticalDirection(float verticalDirection) {
        this.verticalDirection = verticalDirection;
    }

    public final int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    public final void setCurrentFrameIndex(int currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }

    public final int getCurrentAnimationIndex() {
        return currentAnimationIndex;
    }

    public final void setCurrentAnimationIndex(int currentAnimationIndex) {
        this.currentAnimationIndex = currentAnimationIndex;
    }

    public final List<float[]> getCoordinatesBuffersList() {
        return(coordinatesBuffersList);
    }

    public final void setCoordinatesBuffersList(List<float[]> coordinatesBuffersList) {
        this.coordinatesBuffersList = coordinatesBuffersList;
    }

    public final List<AnimationInfoBean> getAnimationList(){
        return(animationList);
    }

    public final void setAnimationList(List<AnimationInfoBean> animationList){
        this.animationList=animationList;
    }
}
