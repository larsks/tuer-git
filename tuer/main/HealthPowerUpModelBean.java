package main;

import java.util.List;

public final class HealthPowerUpModelBean implements XMLTransportableWrapper<HealthPowerUpModel> {

    private int healthIncrease;
    
    private CollectableBean collectableBean;
    
    
    public HealthPowerUpModelBean(){
        this.collectableBean=new CollectableBean();
    }
    
    public HealthPowerUpModelBean(HealthPowerUpModel hpum){
        this();
        wrap(hpum);
    }
    
    public final int getHealthIncrease(){
        return(healthIncrease);
    }
    
    public final void setHealthIncrease(int healthIncrease){
        this.healthIncrease=healthIncrease;
    }
    
    public final String getAfterCollectName(){
        return(this.collectableBean.getAfterCollectName());
    }
    
    public final void setAfterCollectName(String afterCollectName){
        this.collectableBean.setAfterCollectName(afterCollectName);
    }
    
    public final double[] getBoundingSphereRadiusArray(){
        return(collectableBean.getBoundingSphereRadiusArray());
    }

    public final void setBoundingSphereRadiusArray(double[] boundingSphereRadiusArray){
        this.collectableBean.setBoundingSphereRadiusArray(boundingSphereRadiusArray);
    }
    
    public final float getX() {
        return(collectableBean.getX());
    }


    public final void setX(float x) {
        collectableBean.setX(x);
    }


    public final float getY() {
        return(collectableBean.getY());
    }


    public final void setY(float y) {
        collectableBean.setY(y);
    }


    public final float getZ() {
        return(collectableBean.getZ());
    }


    public final void setZ(float z) {
        collectableBean.setZ(z);
    }


    public final float getHorizontalDirection() {
        return(collectableBean.getHorizontalDirection());
    }


    public final void setHorizontalDirection(float horizontalDirection) {
        collectableBean.setHorizontalDirection(horizontalDirection);
    }


    public final float getVerticalDirection() {
        return(collectableBean.getVerticalDirection());
    }


    public final void setVerticalDirection(float verticalDirection) {
        collectableBean.setVerticalDirection(verticalDirection);
    }


    public final int getCurrentFrameIndex() {
        return(collectableBean.getCurrentFrameIndex());
    }


    public final void setCurrentFrameIndex(int currentFrameIndex) {
        collectableBean.setCurrentFrameIndex(currentFrameIndex);
    }


    public final int getCurrentAnimationIndex() {
        return(collectableBean.getCurrentAnimationIndex());
    }


    public final void setCurrentAnimationIndex(int currentAnimationIndex) {
        collectableBean.setCurrentAnimationIndex(currentAnimationIndex);
    }
    
    public final List<float[]> getCoordinatesBuffersList() {
        return(collectableBean.getCoordinatesBuffersList());
    }

    public final void setCoordinatesBuffersList(List<float[]> coordinatesBuffersList) {
        collectableBean.setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    public final List<AnimationInfoBean> getAnimationList(){
        return(collectableBean.getAnimationList());
    }

    public final void setAnimationList(List<AnimationInfoBean> animationList){
        collectableBean.setAnimationList(animationList);
    }

    @Override
    public HealthPowerUpModel getWrappedObject(){
        Collectable collectable=collectableBean.getWrappedObject();       
        HealthPowerUpModel hpum=new HealthPowerUpModel(collectable.getX(),
                collectable.getY(),collectable.getZ(),
                collectable.getHorizontalDirection(),
                collectable.getVerticalDirection(),null,
                collectable.getAfterCollectName(),healthIncrease);
        //copy data
        /*hpum.setX(collectable.getX());
        hpum.setY(collectable.getY());
        hpum.setZ(collectable.getZ());       
        hpum.setAnimationList(collectable.getAnimationList());       
        hpum.setHorizontalDirection(collectable.getHorizontalDirection());
        hpum.setVerticalDirection(collectable.getVerticalDirection());        
        hpum.setCoordinatesBuffersList(collectable.getCoordinatesBuffersList());
        hpum.setCurrentFrameIndex(collectable.getCurrentFrameIndex());
        hpum.setCurrentAnimationIndex(collectable.getCurrentAnimationIndex());
        hpum.setCoordinatesBuffersList(collectable.getCoordinatesBuffersList());
        hpum.setBoundingSphereRadiusArray(collectable.getBoundingSphereRadiusArray());
        hpum.setAfterCollectName(collectable.getAfterCollectName());
        hpum.setHealthIncrease(healthIncrease);*/
        return(hpum);
    }

    @Override
    public void wrap(HealthPowerUpModel hpum){
        this.collectableBean.wrap(hpum);
        this.healthIncrease=hpum.getHealthIncrease();
    }
}
