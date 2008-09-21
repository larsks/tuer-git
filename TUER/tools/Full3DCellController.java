package tools;

import java.nio.FloatBuffer;

import com.sun.opengl.util.BufferUtil;

public final class Full3DCellController{
    
    
    private FloatBuffer internalBuffer;
    
    
    public Full3DCellController(Full3DCell full3DCellModel){
        this.internalBuffer=BufferUtil.newFloatBuffer(
                (full3DCellModel.getBottomWalls().size()+
                 full3DCellModel.getTopWalls().size()+
                 full3DCellModel.getLeftWalls().size()+
                 full3DCellModel.getRightWalls().size()+
                 full3DCellModel.getCeilWalls().size()+
                 full3DCellModel.getFloorWalls().size())*5);//5 because T2_V3
        for(float[] wall:full3DCellModel.getBottomWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:full3DCellModel.getTopWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:full3DCellModel.getLeftWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:full3DCellModel.getRightWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:full3DCellModel.getCeilWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:full3DCellModel.getFloorWalls())
            this.internalBuffer.put(wall);
        this.internalBuffer.rewind();
    }


    public final FloatBuffer getInternalBuffer(){
        return(internalBuffer);
    }
}
