package main;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import tools.GameIO;

final class SphericalBeastModelFactory extends ModelFactory{

    
    private static SphericalBeastModelFactory instance=null;
    
    
    private SphericalBeastModelFactory(){
        List<FloatBuffer> coordinatesBuffersList=null;
        try{
            coordinatesBuffersList=GameIO.readGameMultiBufferFloatDataFile("/pic256/sphericalBeast.data");
           }
        catch(IOException ioe)
        {ioe.printStackTrace();}
        List<AnimationInfo> animationList=new Vector<AnimationInfo>(7);
        //TODO: add a mechanism to read this data from a file
        animationList.add(new AnimationInfo("",0,19,40));//stay in position (breath)
        animationList.add(new AnimationInfo("",20,39,40));//move
        animationList.add(new AnimationInfo("",40,59,40));//shoot
        //TODO: add a second attack (close combat)
        animationList.add(new AnimationInfo("",60,79,40));//die (shot)
        animationList.add(new AnimationInfo("",80,99,40));//die (exploded)
        animationList.add(new AnimationInfo("",100,100,1));//dead (shot)
        animationList.add(new AnimationInfo("",101,101,1));//dead (exploded)
        setAnimationList(animationList);
        setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    
    final static SphericalBeastModelFactory getInstance(){      
        if(instance==null)
            instance=new SphericalBeastModelFactory();
        return(instance);
    }
}
