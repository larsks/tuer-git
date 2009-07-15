package configuration;

import main.XMLTransportableWrapper;

public final class GameLevelModelConfigurationBean implements XMLTransportableWrapper<GameLevelModelConfigurationBean> {

    
    private long identifier;
    
    private String structureDataPath;
    
    private String healthPowerUpModelListPath;
    
    private String animatedObjectListPath;
    
    
    public GameLevelModelConfigurationBean(){}
    
    @Override
    public GameLevelModelConfigurationBean getWrappedObject() {
        return(this);
    }

    @Override
    public void wrap(GameLevelModelConfigurationBean glmcb) {
        //TODO: only copy
    }
  
    public final long getIdentifier(){
        return(identifier);
    }

    public final void setIdentifier(long identifier){
        this.identifier=identifier;
    }

    public final String getStructureDataPath(){
        return(structureDataPath);
    }

    public final void setStructureDataPath(String structureDataPath){
        this.structureDataPath=structureDataPath;
    }

    public final String getHealthPowerUpModelListPath(){
        return(healthPowerUpModelListPath);
    }

    public final void setHealthPowerUpModelListPath(String healthPowerUpModelListPath){
        this.healthPowerUpModelListPath=healthPowerUpModelListPath;
    }

    public final String getAnimatedObjectListPath(){
        return animatedObjectListPath;
    }

    public final void setAnimatedObjectListPath(String animatedObjectListPath){
        this.animatedObjectListPath=animatedObjectListPath;
    }
}
