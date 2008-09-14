package main;

import java.util.List;

public final class CollidableBean implements XMLTransportableWrapper<Collidable>{

    
    private double[] boundingSphereRadiusArray;
    
    private Object3DModelBean object3DModelBean;
    
    
    public CollidableBean(){       
        this.object3DModelBean=new Object3DModelBean();             
    }
    
    
    @Override
    public final Collidable getWrappedObject() {
        //we reuse the 3D object model to avoid recomputing the bounding volume
        Object3DModel o3dm=this.object3DModelBean.getWrappedObject();
        Collidable collidable=new Collidable();
        //copy data
        collidable.setX(o3dm.getX());
        collidable.setY(o3dm.getY());
        collidable.setZ(o3dm.getZ());       
        collidable.setAnimationList(o3dm.getAnimationList());       
        collidable.setHorizontalDirection(o3dm.getHorizontalDirection());
        collidable.setVerticalDirection(o3dm.getVerticalDirection());        
        collidable.setCoordinatesBuffersList(o3dm.getCoordinatesBuffersList());
        collidable.setCurrentFrameIndex(o3dm.getCurrentFrameIndex());
        collidable.setCurrentAnimationIndex(o3dm.getCurrentAnimationIndex());
        collidable.setCoordinatesBuffersList(o3dm.getCoordinatesBuffersList());
        collidable.setBoundingSphereRadiusArray(boundingSphereRadiusArray);
        return(collidable);
    }

    @Override
    public final void wrap(Collidable c) {
        object3DModelBean.wrap(c);
        boundingSphereRadiusArray=c.getBoundingSphereRadiusArray();       
    }

    public final double[] getBoundingSphereRadiusArray(){
        return(boundingSphereRadiusArray);
    }

    public final void setBoundingSphereRadiusArray(double[] boundingSphereRadiusArray){
        this.boundingSphereRadiusArray = boundingSphereRadiusArray;
    }
    
    public final float getX() {
        return(object3DModelBean.getX());
    }


    public final void setX(float x) {
        object3DModelBean.setX(x);
    }


    public final float getY() {
        return(object3DModelBean.getY());
    }


    public final void setY(float y) {
        object3DModelBean.setY(y);
    }


    public final float getZ() {
        return(object3DModelBean.getZ());
    }


    public final void setZ(float z) {
        object3DModelBean.setZ(z);
    }


    public final float getHorizontalDirection() {
        return(object3DModelBean.getHorizontalDirection());
    }


    public final void setHorizontalDirection(float horizontalDirection) {
        object3DModelBean.setHorizontalDirection(horizontalDirection);
    }


    public final float getVerticalDirection() {
        return(object3DModelBean.getVerticalDirection());
    }


    public final void setVerticalDirection(float verticalDirection) {
        object3DModelBean.setVerticalDirection(verticalDirection);
    }


    public final int getCurrentFrameIndex() {
        return(object3DModelBean.getCurrentFrameIndex());
    }


    public final void setCurrentFrameIndex(int currentFrameIndex) {
        object3DModelBean.setCurrentFrameIndex(currentFrameIndex);
    }


    public final int getCurrentAnimationIndex() {
        return(object3DModelBean.getCurrentAnimationIndex());
    }


    public final void setCurrentAnimationIndex(int currentAnimationIndex) {
        object3DModelBean.setCurrentAnimationIndex(currentAnimationIndex);
    }
    
    public final List<float[]> getCoordinatesBuffersList() {
        return(object3DModelBean.getCoordinatesBuffersList());
    }

    public final void setCoordinatesBuffersList(List<float[]> coordinatesBuffersList) {
        object3DModelBean.setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    public final List<AnimationInfoBean> getAnimationList(){
        return(object3DModelBean.getAnimationList());
    }

    public final void setAnimationList(List<AnimationInfoBean> animationList){
        object3DModelBean.setAnimationList(animationList);
    }
}
