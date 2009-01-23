package tools;

import java.io.Serializable;

public interface ILevelModelBean{
    public float[] getInitialSpawnPosition();
    public void setInitialSpawnPosition(float[] initialSpawnPosition);
    public Serializable getSerializableBean();
}
