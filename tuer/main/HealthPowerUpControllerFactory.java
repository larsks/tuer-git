package main;

import java.nio.FloatBuffer;
import java.util.List;

final class HealthPowerUpControllerFactory extends ControllerFactory{

    
    private static HealthPowerUpControllerFactory instance=null;
    
    
    private HealthPowerUpControllerFactory(){}
    
    
    static final HealthPowerUpControllerFactory getInstance(){
        if(instance==null)
            instance=new HealthPowerUpControllerFactory();
        return(instance);
    }

    @Override
    final List<FloatBuffer> getCoordinatesBuffersList() {
        return(HealthPowerUpModelFactory.getInstance().getCoordinatesBuffersList());
    }
}
