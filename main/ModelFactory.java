package main;

import java.nio.FloatBuffer;
import java.util.List;

abstract class ModelFactory {

    
    private List<FloatBuffer> coordinatesBuffersList;
    
    private List<AnimationInfo> animationList;   
    

    final List<FloatBuffer> getCoordinatesBuffersList() {
        return(coordinatesBuffersList);
    }

    final List<AnimationInfo> getAnimationList() {
        return(animationList);
    }

    final void setCoordinatesBuffersList(List<FloatBuffer> coordinatesBuffersList) {
        this.coordinatesBuffersList = coordinatesBuffersList;
    }

    final void setAnimationList(List<AnimationInfo> animationList) {
        this.animationList = animationList;
    }
}
