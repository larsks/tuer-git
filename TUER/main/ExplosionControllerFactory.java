package main;

import java.nio.FloatBuffer;
import java.util.List;

final class ExplosionControllerFactory extends ControllerFactory{
    
    
    private static ExplosionControllerFactory instance=null;
    
    
    private ExplosionControllerFactory(){}
    
    
    static final ExplosionControllerFactory getInstance(){
        if(instance==null)
            instance=new ExplosionControllerFactory();
        return(instance);
    }

    @Override
    final List<FloatBuffer> getCoordinatesBuffersList() {
        return(ExplosionModelFactory.getInstance().getCoordinatesBuffersList());
    }
}
