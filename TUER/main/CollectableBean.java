package main;

import java.util.List;

public final class CollectableBean implements XMLTransportableWrapper<Collectable>{

    
    private String afterCollectName;
    
    private CollidableBean collidableBean;
    
    
    public CollectableBean(){
        this.collidableBean=new CollidableBean();
    }
    
    
    public final String getAfterCollectName(){
        return(afterCollectName);
    }
    
    public final void setAfterCollectName(String afterCollectName){
        this.afterCollectName=afterCollectName;
    }
    
    @Override
    public Collectable getWrappedObject() {
        Collidable collidable=this.collidableBean.getWrappedObject();       
        Collectable collectable=new Collectable(collidable.getX(),
            collidable.getY(),collidable.getZ(),
            collidable.getCoordinatesBuffersList(),collidable.getAnimationList(),
            collidable.getHorizontalDirection(),collidable.getVerticalDirection(),
            null,afterCollectName);
        return(collectable);
    }

    @Override
    public void wrap(Collectable c) {
        this.collidableBean.wrap(c);
        this.afterCollectName=c.getAfterCollectName();
    }
   
    public final double[] getBoundingSphereRadiusArray(){
        return(collidableBean.getBoundingSphereRadiusArray());
    }

    public final void setBoundingSphereRadiusArray(double[] boundingSphereRadiusArray){
        this.collidableBean.setBoundingSphereRadiusArray(boundingSphereRadiusArray);
    }

    public final float getX() {
        return(collidableBean.getX());
    }


    public final void setX(float x) {
        collidableBean.setX(x);
    }


    public final float getY() {
        return(collidableBean.getY());
    }


    public final void setY(float y) {
        collidableBean.setY(y);
    }


    public final float getZ() {
        return(collidableBean.getZ());
    }


    public final void setZ(float z) {
        collidableBean.setZ(z);
    }


    public final float getHorizontalDirection() {
        return(collidableBean.getHorizontalDirection());
    }


    public final void setHorizontalDirection(float horizontalDirection) {
        collidableBean.setHorizontalDirection(horizontalDirection);
    }


    public final float getVerticalDirection() {
        return(collidableBean.getVerticalDirection());
    }


    public final void setVerticalDirection(float verticalDirection) {
        collidableBean.setVerticalDirection(verticalDirection);
    }


    public final int getCurrentFrameIndex() {
        return(collidableBean.getCurrentFrameIndex());
    }


    public final void setCurrentFrameIndex(int currentFrameIndex) {
        collidableBean.setCurrentFrameIndex(currentFrameIndex);
    }


    public final int getCurrentAnimationIndex() {
        return(collidableBean.getCurrentAnimationIndex());
    }


    public final void setCurrentAnimationIndex(int currentAnimationIndex) {
        collidableBean.setCurrentAnimationIndex(currentAnimationIndex);
    }
    
    public final List<float[]> getCoordinatesBuffersList() {
        return(collidableBean.getCoordinatesBuffersList());
    }

    public final void setCoordinatesBuffersList(List<float[]> coordinatesBuffersList) {
        collidableBean.setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    public final List<AnimationInfoBean> getAnimationList(){
        return(collidableBean.getAnimationList());
    }

    public final void setAnimationList(List<AnimationInfoBean> animationList){
        collidableBean.setAnimationList(animationList);
    }
}
