package main;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import tools.GameIO;

final class HealthPowerUpModelFactory extends ModelFactory {

    
    private static HealthPowerUpModelFactory instance=null;
    
    
    private HealthPowerUpModelFactory(){
        List<FloatBuffer> coordinatesBuffersList=new Vector<FloatBuffer>(1);
        FloatBuffer healthPowerUpCoordinatesBuffer=null;
        try{
            healthPowerUpCoordinatesBuffer=GameIO.readGameFloatDataFile("/pic256/healthPowerUp.data");
           }
        catch(IOException ioe)
        {ioe.printStackTrace();}                   
        coordinatesBuffersList.add(healthPowerUpCoordinatesBuffer);
        List<AnimationInfo> animationList=new Vector<AnimationInfo>(1);
        animationList.add(new AnimationInfo("",0,0,8));
        setAnimationList(animationList);
        setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    final static HealthPowerUpModelFactory getInstance(){      
        if(instance==null)
            instance=new HealthPowerUpModelFactory();
        return(instance);
    }
}
