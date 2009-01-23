package connection;

import java.util.Arrays;

import tools.IBeanProvider;
import tools.ILevelModelBean;
import tools.TilesGenerator;

public final class DataPreprocessor implements IBeanProvider{
    
    
    private bean.IBeanProvider delegate;

    
    private DataPreprocessor(bean.IBeanProvider factory,IBeanProvider seeker){
        bindBeanProvider(factory);
        bindBeanProvider(seeker);
    }
    
    
    public final void bindBeanProvider(bean.IBeanProvider provider){
        delegate=provider;     
    }
    
    @Override
    public final void bindBeanProvider(tools.IBeanProvider provider){
         provider.bindBeanProvider(this);      
    }


    @Override
    public ILevelModelBean getILevelModelBean(float[] initialSpawnPosition){
        return(new LevelModelBeanConnector(delegate.getILevelModelBean(initialSpawnPosition)));
    }
    
    public static final void main(String[] args){
        DataPreprocessor dp=new DataPreprocessor(bean.BeanProvider.getInstance(),tools.BeanProvider.getInstance());
        if(dp!=null)
            {System.out.println("Data processing initialized");
             TilesGenerator tg=TilesGenerator.getInstance(args);
             if(tg!=null)
                 System.out.println("Tiles and cells generation finished");
             else
                 System.out.println("[WARNING] Tiles and cells generation failed!");
            }
        else
            System.out.println("[WARNING] Data processing initialization failed!");
    }
}
