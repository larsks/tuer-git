package bean;

import java.io.Serializable;

public interface ILevelModelBean extends Serializable{
    public float[] getInitialSpawnPosition();
    public void setInitialSpawnPosition(float[] initialSpawnPosition);
    public Serializable getSerializableBean();
}
