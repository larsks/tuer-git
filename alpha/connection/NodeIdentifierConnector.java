package connection;

import java.io.Serializable;

final class NodeIdentifierConnector implements tools.INodeIdentifier{
    
    
    private bean.INodeIdentifier delegate;
    
    
    NodeIdentifierConnector(bean.INodeIdentifier delegate){
        this.delegate=delegate;
    }

    
    @Override
    public final int getCellID(){
        return(delegate.getCellID());
    }

    @Override
    public final int getLevelID(){
        return(delegate.getLevelID());
    }

    @Override
    public final int getNetworkID(){
        return(delegate.getNetworkID());
    }

    @Override
    public final int getSecondaryCellID(){
        return(delegate.getSecondaryCellID());
    }

    @Override
    public final Serializable getSerializableBean(){
        return(delegate.getSerializableBean());
    }

    @Override
    public final void setCellID(int cellID){
        delegate.setCellID(cellID);
    }

    @Override
    public final void setLevelID(int levelID){
        delegate.setLevelID(levelID);
    }

    @Override
    public final void setNetworkID(int networkID){
        delegate.setNetworkID(networkID);
    }

    @Override
    public final void setSecondaryCellID(int secondaryCellID){
        delegate.setSecondaryCellID(secondaryCellID);
    }
    
    @Override
    public final String toString(){
        return(delegate.toString());
    }
    
    @Override
    public final int hashCode(){
        return(delegate.hashCode());
    }
    
    @Override
    public final boolean equals(Object o){
        boolean result;
        if(o==null || !(o instanceof NodeIdentifierConnector))
            result=false;
        else
            result=delegate.equals(((NodeIdentifierConnector)o).delegate);
        return(result);
    }
}
