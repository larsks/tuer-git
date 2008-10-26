package drawer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

final class DynamicVertexSetDecorator implements IDynamicVertexSet{

    
    private List<IDynamicVertexSet> subDynamicVertexList;
    
    private FloatBuffer fullBuffer;
    
    
    DynamicVertexSetDecorator(List<IDynamicVertexSet> subDynamicVertexList,FloatBuffer fullBuffer){
        this.subDynamicVertexList=subDynamicVertexList;
        this.fullBuffer=fullBuffer;
    }
    
    
    @Override
    public final void draw(){
        for(IDynamicVertexSet dynamicVertexSet:subDynamicVertexList)
            dynamicVertexSet.draw();
    }

    @Override
    public final FloatBuffer getBuffer(){
        return(fullBuffer);
    }


    @Override
    public final void draw(int start, int count) {
        //TODO: implement it!!!
        
    }


    @Override
    public final void drawByPiece(IntBuffer first, IntBuffer count, int limit) {
        //TODO: implement it!!!        
    }


    @Override
    public final float get(int index) {
        return(fullBuffer.get(index));
    }


    @Override
    public final float[] get() {
        return(fullBuffer.array());
    }


    @Override
    public final void multiDraw(FloatBuffer translation, FloatBuffer rotation,
            int limit, boolean relative) {       
        //TODO: implement it!!!
    }


    @Override
    public final void multiDraw(FloatBuffer matrix, int limit, boolean relative) {
        //TODO: implement it!!!       
    }


    @Override
    public final void multiDraw(FloatBuffer translation, FloatBuffer rotation,
            IntBuffer first, IntBuffer count, int limit, boolean relative) {
        //TODO: implement it!!!
    }


    @Override
    public final void put(int index, float value) {
        fullBuffer.put(index,value);
        /*call glBufferData for VBO???*/        
    }


    @Override
    public final void put(float[] value, int offset, int length) {
        fullBuffer.position(offset);
        for(int i=0;i<length;i++)
            fullBuffer.put(value[i]);
        fullBuffer.position(0);
        /*call glBufferData for VBO???*/       
    }


    @Override
    /*"value" position incremented during the treatment*/
    public final void put(FloatBuffer value, int offset, int length) {
        fullBuffer.position(offset);
        fullBuffer.put(value);
        fullBuffer.position(0);
        /*call glBufferData for VBO???*/       
    }


    @Override
    public final void set(float[] value) {
        fullBuffer.position(0);
        fullBuffer.put(value);
        fullBuffer.position(0);
        /*call glBufferData for VBO???*/
        
    }


    @Override
    public final void set(FloatBuffer value) {
        fullBuffer.position(0);
        fullBuffer.put(value);
        fullBuffer.position(0);
        /*call glBufferData for VBO???*/        
    }
}
