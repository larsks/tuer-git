package tools;

public interface IBeanProvider{
    public ILevelModelBean getILevelModelBean(float[] initialSpawnPosition);
    public void bindBeanProvider(IBeanProvider provider);
}
