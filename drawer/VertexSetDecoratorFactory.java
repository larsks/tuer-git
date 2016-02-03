package drawer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import com.jogamp.common.nio.Buffers;

final class VertexSetDecoratorFactory{
    
    
    static final IStaticVertexSet newVertexSet(
            AbstractStaticVertexSetFactory staticVertexSetFactory,FloatBuffer buffer,
            int mode){
        IStaticVertexSet staticVertexSet;
        if(buffer.capacity()/VertexSet.primitiveCount>VertexSetFactory.GL_MAX_ELEMENTS_VERTICES)
            {List<FloatBuffer> subBufferList = getSubBufferList(buffer,mode);
             List<IStaticVertexSet> subStaticVertexList=new ArrayList<IStaticVertexSet>(subBufferList.size());
             for(FloatBuffer subBuffer:subBufferList)
                 subStaticVertexList.add(staticVertexSetFactory.newVertexSet(subBuffer,mode));            
             staticVertexSet=new StaticVertexSetDecorator(subStaticVertexList,buffer);
             //staticVertexSet=staticVertexSetFactory.newVertexSet(buffer,mode);
            }
        else
            staticVertexSet=staticVertexSetFactory.newVertexSet(buffer,mode);
        return(staticVertexSet);
    }
    
    private static final List<FloatBuffer> getSubBufferList(FloatBuffer buffer,int mode){
        int maxElementsVertices=VertexSetFactory.GL_MAX_ELEMENTS_VERTICES;
        int interBufferDuplicateElementsCount;
        boolean copyFirstElementIntoLastElement;
        switch(mode)
            {case GL.GL_TRIANGLES:
                 {maxElementsVertices=maxElementsVertices-(maxElementsVertices%3);
                  interBufferDuplicateElementsCount=0;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL2.GL_QUADS:
                 {maxElementsVertices=maxElementsVertices-(maxElementsVertices%4);
                  interBufferDuplicateElementsCount=0;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL2.GL_QUAD_STRIP:
                 {maxElementsVertices=maxElementsVertices-(maxElementsVertices%2);
                  interBufferDuplicateElementsCount=2;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }                        
             case GL2.GL_POLYGON:
                 {interBufferDuplicateElementsCount=1;
                  //it needs the first element for the last sub-buffer too
                  copyFirstElementIntoLastElement=true;
                  break;
                 }
             case GL.GL_POINTS: 
                 {interBufferDuplicateElementsCount=0;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL.GL_LINES:
                 {maxElementsVertices=maxElementsVertices-(maxElementsVertices%2);
                  interBufferDuplicateElementsCount=0;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL.GL_LINE_STRIP:
                 {interBufferDuplicateElementsCount=1;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL.GL_TRIANGLE_FAN:
                 {interBufferDuplicateElementsCount=1;
                  copyFirstElementIntoLastElement=false;
                  /*TODO
                   * The first element has to be copied into the start of each
                   * sub-buffer
                   */
                  break;
                 }
             case GL.GL_TRIANGLE_STRIP:
                 {interBufferDuplicateElementsCount=2;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
             case GL.GL_LINE_LOOP:
                 {interBufferDuplicateElementsCount=1;
                  //it needs the first element for the end of the last sub-buffer too
                  copyFirstElementIntoLastElement=true;
                  //change the mode to GL_LINE_STRIP (dirty)
                  mode=GL.GL_LINE_STRIP;
                  break;
                 }
             default:
                 {interBufferDuplicateElementsCount=0;
                  copyFirstElementIntoLastElement=false;
                  break;
                 }
            }
        //compute the maximum size of each sub-buffer
        int maxSize=maxElementsVertices*VertexSet.primitiveCount;
        //compute the count of sub-buffers needed to store the data
        int subBufferListSize;
        final int endOffset=(copyFirstElementIntoLastElement)?VertexSet.primitiveCount:0;
        final int duplicateOffset=interBufferDuplicateElementsCount*VertexSet.primitiveCount;
        if(interBufferDuplicateElementsCount==0)        
            subBufferListSize=(int)Math.ceil((buffer.capacity()+endOffset)/(double)maxSize);
        else
            //take into account inter-buffer duplicate data
            subBufferListSize=1+(int)Math.ceil((buffer.capacity()+endOffset-maxSize)/(double)(maxSize-duplicateOffset));       
        List<FloatBuffer> subBufferList=new ArrayList<FloatBuffer>(subBufferListSize);
        //compute the size of the last sub-buffer
        int lastSize=maxSize-(subBufferListSize*maxSize-buffer.capacity());
        FloatBuffer subBuffer,previousSubBuffer;
        int offset,currentSubBufferSize;       
        float[] duplicateData=new float[duplicateOffset];
        float[] endData=new float[endOffset];
        for(int i=0;i<subBufferListSize;i++)
            {//the last sub-buffer might be smaller than the others
             if(i==subBufferListSize-1)
                 currentSubBufferSize=lastSize;
             else   
                 currentSubBufferSize=maxSize;
             //allocate the sub-buffer
             subBuffer=Buffers.newDirectFloatBuffer(currentSubBufferSize);
             //some data may have to be duplicated except for the first sub-buffer
             if(i!=0&&duplicateOffset!=0)
                 {offset=duplicateOffset;
                  //get the previous sub-buffer
                  previousSubBuffer=subBufferList.get(i);          
                  //get its n last element(s)
                  previousSubBuffer.position(previousSubBuffer.capacity()-duplicateOffset);
                  previousSubBuffer.get(duplicateData);
                  //rewind the previous sub-buffer
                  previousSubBuffer.rewind();   
                  //put them into the new buffer
                  subBuffer.put(duplicateData);                                  
                 }
             else
                 offset=0;
             for(int j=0;j<currentSubBufferSize-offset;j++)
                 subBuffer.put(buffer.get());  
             if(i==subBufferListSize-1&&copyFirstElementIntoLastElement)
                 {int oldPos=subBuffer.position();                 
                  //get the first sub-buffer
                  FloatBuffer firstSubBuffer=subBufferList.get(0);
                  //get the first element
                  firstSubBuffer.get(endData);
                  //rewind the first sub-buffer
                  firstSubBuffer.rewind();
                  //reuse the old position of the new buffer, it might have changed
                  subBuffer.position(oldPos); 
                  //put this element into the new sub-buffer
                  subBuffer.put(endData);
                 }
             subBuffer.rewind();
             subBufferList.add(subBuffer);
            }
        buffer.rewind();
        return(subBufferList);
    }
    
    static final IDynamicVertexSet newVertexSet(
            AbstractDynamicVertexSetFactory dynamicVertexSetFactory,FloatBuffer buffer,
            int mode){
        IDynamicVertexSet dynamicVertexSet;
        if(buffer.capacity()/VertexSet.primitiveCount>VertexSetFactory.GL_MAX_ELEMENTS_VERTICES)
            {List<FloatBuffer> subBufferList = getSubBufferList(buffer,mode);
             List<IDynamicVertexSet> subDynamicVertexList=new ArrayList<IDynamicVertexSet>(subBufferList.size());
             for(FloatBuffer subBuffer:subBufferList)
                 subDynamicVertexList.add(dynamicVertexSetFactory.newVertexSet(subBuffer,mode));
             dynamicVertexSet=new DynamicVertexSetDecorator(subDynamicVertexList,buffer);
             //dynamicVertexSet=dynamicVertexSetFactory.newVertexSet(buffer,mode);
            }
        else
            dynamicVertexSet=dynamicVertexSetFactory.newVertexSet(buffer,mode);
        return(dynamicVertexSet);
    }
}
