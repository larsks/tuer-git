package main;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import configuration.ApplicationBehavior;
import configuration.GameLevelModelConfigurationBean;

final class GameLevelModel{
    
    private long identifier;
    
    private List<HealthPowerUpModel> healthPowerUpModelList;
    
    private List<Object3DModel> animatedObjectList;
    
    private FloatBuffer structureCoordinatesBuffer;
    
    //TODO: replace it by a network of cells linked by portals
    private byte[] collisionMap;

    private GameLevelModel(long identifier,
            List<HealthPowerUpModel> healthPowerUpModelList,
            FloatBuffer structureCoordinatesBuffer,
            byte[] collisionMap){
        this.identifier=identifier;
        this.healthPowerUpModelList=healthPowerUpModelList;
        this.structureCoordinatesBuffer=structureCoordinatesBuffer;
        this.collisionMap=collisionMap;
    }
    
    /**
     * 
     * @param filename: filename of the level configuration file 
     * (that contains sub-paths to load data), only the relative path
     * @return
     */
    static final GameLevelModel load(String filename,Clock clock){       
        //use the default level configuration path to build the full path
        String fullPath=ApplicationBehavior.getInstance().getLevelsConfigurationDirectoryPath()+filename;
        //use the full path to decode the GameLevelModelConfigurationBean
        BufferedInputStream bis=null;
        GameLevelModelConfigurationBean glmcb=null;
        try{bis=new BufferedInputStream(GameLevelModel.class.getResourceAsStream(fullPath));
            XMLDecoder decoder = new XMLDecoder(bis);
            glmcb=(GameLevelModelConfigurationBean)decoder.readObject();        
            decoder.close();
           }
        catch(Exception e)
        {throw new RuntimeException("Unable to decode XML file",e);}
        //TODO: use the configuration bean to build the model by using the paths       
        Vector<HealthPowerUpModel> healthPowerUpModelList=new Vector<HealthPowerUpModel>();
        if(glmcb.getHealthPowerUpModelListPath()!=null && 
                !glmcb.getHealthPowerUpModelListPath().equals(""))
            {Vector<HealthPowerUpModelBean> beanList=null;
             //decode the file to get the beans
             try{bis=new BufferedInputStream(GameLevelModel.class.getResourceAsStream(
                     glmcb.getHealthPowerUpModelListPath()));
                 XMLDecoder decoder = new XMLDecoder(bis);
                 beanList=(Vector<HealthPowerUpModelBean>)decoder.readObject();        
                 decoder.close();
                } 
             catch(Exception e)
             {throw new RuntimeException("Unable to decode XML file",e);}          
             //convert the beans into useful objects
             for(HealthPowerUpModelBean bean:beanList)
                 {HealthPowerUpModel hpum=bean.getWrappedObject();
                  //apply the internal clock on each item
                  hpum.setInternalClock(clock);
                  healthPowerUpModelList.add(hpum);      
                 }
            }
        byte[] collisionMap=new byte[256*256];
        if(glmcb.getStructureDataPath()!=null)
            {DataInputStream in=new DataInputStream(new BufferedInputStream(GameLevelModel.class.getResourceAsStream(
                    glmcb.getStructureDataPath())));
             try{in.read(collisionMap,0,256*256);
                 //build the bot list here
                 in.close();
                }
             catch(IOException e)
             {throw new RuntimeException("Unable to read the collision map",e);}            
            }
        if(glmcb.getAnimatedObjectListPath()!=null)
            {
             
            }
        return(new GameLevelModel(glmcb.getIdentifier(),healthPowerUpModelList,null,collisionMap));
    }

    final long getIdentifier(){
        return(identifier);
    }

    final void setIdentifier(long identifier){
        this.identifier=identifier;
    }

    final List<HealthPowerUpModel> getHealthPowerUpModelList(){
        return(healthPowerUpModelList);
    }

    final void setHealthPowerUpModelList(
            List<HealthPowerUpModel> healthPowerUpModelList){
        this.healthPowerUpModelList=healthPowerUpModelList;
    }

    final FloatBuffer getStructureCoordinatesBuffer(){
        return(structureCoordinatesBuffer);
    }

    final void setStructureCoordinatesBuffer(FloatBuffer structureCoordinatesBuffer){
        this.structureCoordinatesBuffer=structureCoordinatesBuffer;
    }

    final byte[] getCollisionMap(){
        return(collisionMap);
    }

    final void setCollisionMap(byte[] collisionMap){
        this.collisionMap=collisionMap;
    }

    final List<Object3DModel> getAnimatedObjectList(){
        return(animatedObjectList);
    }

    final void setAnimatedObjectList(List<Object3DModel> animatedObjectList){
        this.animatedObjectList=animatedObjectList;
    }
}
