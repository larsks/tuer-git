package main;

import java.nio.FloatBuffer;
import java.util.List;

final class SphericalBeastControllerFactory extends ControllerFactory {

    
    private static SphericalBeastControllerFactory instance=null;
    
    
    private SphericalBeastControllerFactory(){}
    
    
    static final SphericalBeastControllerFactory getInstance(){
        if(instance==null)
            instance=new SphericalBeastControllerFactory();
        return(instance);
    }

    @Override
    final List<FloatBuffer> getCoordinatesBuffersList() {
        return(SphericalBeastModelFactory.getInstance().getCoordinatesBuffersList());
    }
}
