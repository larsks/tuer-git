package main;

import java.nio.FloatBuffer;
import java.util.List;

//model side
//TODO: create a collectable container that extends this class (naming mechanism?)
/**
 * this class represents an object that can be atomically collected
 * by ONE collector and ONCE only, otherwise use a collectable container
 */
/*abstract*/ class Collectable extends Collidable {
    
    private Collector collector;
    
    /**
     * name displayed after the collector picked up the object
     */
    private String afterCollectName;
    
    
    Collectable(float x,float y,float z,List<FloatBuffer> coordinatesBuffersList,
            List<AnimationInfo> animationList,float horizontalDirection,
            float verticalDirection,Clock internalClock,String afterCollectName){
        super(x,y,z,coordinatesBuffersList,animationList,horizontalDirection,
                verticalDirection,internalClock);
        this.collector=null;
        this.afterCollectName=afterCollectName;
    }
    
    /**
     * updates the collector's state while collecting this
     * collectable object
     * @param collector
     * @return indicates whether the object can be consumed
     */
    /*abstract*/ boolean performCollect(Collector collector){
        return(false);        
    }
    
    final Collector getCollector(){
        return(collector);
    }
    
    final String getAfterCollectName(){
        return(afterCollectName);
    }
    
    //only for the XML encoding and decoding
    final void setAfterCollectName(String afterCollectName){
        this.afterCollectName=afterCollectName;
    }
    
    /**
     * 
     * @param collector
     * @return indicates whether the object can be consumed
     */
    boolean updatesAsBeingCollectedBy(Collector collector){
        if(performCollect(collector))
            {//identify the owner of the object
             this.collector=collector;
             //force this collectable object to disappear if required
             this.dispose();
             return(true);
            }
        else
            return(false);        
    }
}
