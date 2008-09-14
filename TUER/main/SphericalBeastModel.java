package main;

import java.nio.FloatBuffer;
import java.util.List;

final class SphericalBeastModel extends EnemyModel {

    
    private static final int initialHealth=60;
    
    
    SphericalBeastModel(float x, float y, float z,
            List<FloatBuffer> coordinatesBuffersList,
            List<AnimationInfo> animationList, float horizontalDirection,
            float verticalDirection, Clock internalClock,int health,
            int maximumHealth,int scopeAngle,int scopeDepth){
        super(x,y,z,SphericalBeastModelFactory.getInstance().getCoordinatesBuffersList(),
                SphericalBeastModelFactory.getInstance().getAnimationList(),
                horizontalDirection,verticalDirection,internalClock,initialHealth,
                maximumHealth,scopeAngle,scopeDepth);
    }
}
