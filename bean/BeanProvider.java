package bean;

public final class BeanProvider implements IBeanProvider{
    
    
    private static final BeanProvider instance=new BeanProvider();
    
    
    private BeanProvider(){}
    
    
    public static final BeanProvider getInstance(){
        return(instance);
    }
    
    @Override
    public final ILevelModelBean getILevelModelBean(float[] initialSpawnPosition){
        LevelModelBean lmb=new LevelModelBean();
        lmb.setInitialSpawnPosition(initialSpawnPosition);
        return(lmb);
    }
    
    @Override
    public final void bindBeanProvider(IBeanProvider provider){}
}
