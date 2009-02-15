package bean;

import java.io.Serializable;

public interface INodeIdentifier extends Serializable{
    public int getLevelID();
    public void setLevelID(int levelID);
    public int getNetworkID();
    public void setNetworkID(int networkID);
    public int getCellID();
    public void setCellID(int cellID);
    public int getSecondaryCellID();
    public void setSecondaryCellID(int secondaryCellID);
    public Serializable getSerializableBean();
}
