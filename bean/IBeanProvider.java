package bean;

public interface IBeanProvider{
    public ILevelModelBean getILevelModelBean(float[] initialSpawnPosition);
    public INodeIdentifier getINodeIdentifier();
    public void bindBeanProvider(IBeanProvider provider);
}
