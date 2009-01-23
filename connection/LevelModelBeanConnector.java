package connection;

import java.io.Serializable;

final class LevelModelBeanConnector implements tools.ILevelModelBean{

    
    private bean.ILevelModelBean delegate;
    
    
    LevelModelBeanConnector(bean.ILevelModelBean delegate){
        this.delegate=delegate;
    }
    
    
    @Override
    public final float[] getInitialSpawnPosition(){
        return(delegate.getInitialSpawnPosition());
    }

    @Override
    public final void setInitialSpawnPosition(float[] initialSpawnPosition){
        delegate.setInitialSpawnPosition(initialSpawnPosition);
    }

    @Override
    public final Serializable getSerializableBean(){
        return(delegate.getSerializableBean());
    }
}
