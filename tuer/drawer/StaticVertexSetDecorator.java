package drawer;

import java.nio.FloatBuffer;
import java.util.List;

final class StaticVertexSetDecorator implements IStaticVertexSet {

    
    private List<IStaticVertexSet> subStaticVertexList;
    
    private FloatBuffer fullBuffer;
    
    
    StaticVertexSetDecorator(List<IStaticVertexSet> subStaticVertexList,FloatBuffer fullBuffer){
        this.subStaticVertexList=subStaticVertexList;
        this.fullBuffer=fullBuffer;
    }
    
    
    @Override
    public final void draw(){
        for(IStaticVertexSet staticVertexSet:subStaticVertexList)
            staticVertexSet.draw();             
    }

    @Override
    public final FloatBuffer getBuffer(){
        return(fullBuffer);
    }

}
