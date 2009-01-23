package bean;

import java.io.Serializable;

public final class LevelModelBean implements ILevelModelBean,Serializable{
    
    
    private static final long serialVersionUID = 1L;
    /**
     * Position when the player appears at the first time
     * he enters the level
     */
    private float[] initialSpawnPosition;
    
    
    public LevelModelBean(){}


    @Override
    public final float[] getInitialSpawnPosition(){
        return(initialSpawnPosition);
    }

    @Override
    public final void setInitialSpawnPosition(float[] initialSpawnPosition){
        this.initialSpawnPosition=initialSpawnPosition;
    }
    
    @Override
    public final Serializable getSerializableBean(){
        return(this);    
    }
}
