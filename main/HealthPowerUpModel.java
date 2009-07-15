package main;

public final class HealthPowerUpModel extends Collectable {

    
    private int healthIncrease;
    
    
    public HealthPowerUpModel(float x, float y, float z,
            float horizontalDirection,
            float verticalDirection, Clock internalClock,
            String afterCollectName,int healthIncrease) {
        super(x, y, z, HealthPowerUpModelFactory.getInstance().getCoordinatesBuffersList(),
                HealthPowerUpModelFactory.getInstance().getAnimationList(), 
                horizontalDirection,verticalDirection,internalClock,afterCollectName);
        this.healthIncrease=healthIncrease;
    }

    @Override
    boolean performCollect(Collector collector) {
        return(collector.increaseHealth(healthIncrease)>0);
    }
    
    //only for the XML encoding and decoding
    final int getHealthIncrease(){
        return(healthIncrease);
    }
    
    //only for the XML encoding and decoding
    final void setHealthIncrease(int healthIncrease){
        this.healthIncrease=healthIncrease;
    }
}
