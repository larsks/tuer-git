package tools;

public final class BeanProvider implements IBeanProvider{

    
    private IBeanProvider delegate;
    
    private static final BeanProvider instance=new BeanProvider();
    
    
    private BeanProvider(){}
    
    
    public static final BeanProvider getInstance(){
        return(instance);
    }
    
    @Override
    public void bindBeanProvider(IBeanProvider provider) {
        delegate=provider;
    }

    @Override
    public ILevelModelBean getILevelModelBean(float[] initialSpawnPosition){
        return(delegate.getILevelModelBean(initialSpawnPosition));
    }

}
