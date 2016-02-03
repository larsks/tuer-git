/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package drawer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.common.nio.Buffers;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexSetFactory implements IVertexSetProvider{


    private AbstractDynamicVertexSetFactory dynamicVertexSetFactory=null;
    
    private AbstractStaticVertexSetFactory staticVertexSetFactory=null;
    
    private boolean maxElementsVerticesKnown;
    
    private final static VertexSetFactory instance=new VertexSetFactory();
    
    static int GL_MAX_ELEMENTS_VERTICES=3000;
    
    
    private VertexSetFactory(){
        maxElementsVerticesKnown=false;
    }
    
    
    public static VertexSetFactory getInstance(){
        return(instance);
    }
    
    public void bindVertexSetProvider(IVertexSetProvider provider){
        
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(float[] array,int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory();
        return(staticVertexSetFactory.newVertexSet(array,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(IVertexSet vertexSet,int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory();
        return(staticVertexSetFactory.newVertexSet(vertexSet,mode));
    }
       
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(float[] array,int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory();
        return(dynamicVertexSetFactory.newVertexSet(array,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(FloatBuffer floatBuffer,int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory();
        return(dynamicVertexSetFactory.newVertexSet(floatBuffer,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(IVertexSet vertexSet,int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory();
        return(dynamicVertexSetFactory.newVertexSet(vertexSet,mode));
    }
    
    private void updateMaxElementsVertices(){
        IntBuffer buffer=Buffers.newDirectIntBuffer(1);	
        GLContext.getCurrentGL().glGetIntegerv(GL2.GL_MAX_ELEMENTS_VERTICES,buffer);
        buffer.position(0);
        int internalGlMaxElementsVertices=buffer.get();
        //the fucking driver for ATI Xpress 200 returns -1 under Linux!!!!
        //some very old graphic chips return the biggest integer
        if(internalGlMaxElementsVertices>1 && internalGlMaxElementsVertices<Integer.MAX_VALUE)
            GL_MAX_ELEMENTS_VERTICES=internalGlMaxElementsVertices;        
    }


    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(final FloatBuffer floatBuffer,final int mode){
        if(!maxElementsVerticesKnown)
            {updateMaxElementsVertices();
             maxElementsVerticesKnown=true;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory();      
        //prevents the program from creating a too big VBO       
        return(VertexSetDecoratorFactory.newVertexSet(staticVertexSetFactory,floatBuffer,mode));
    }
}
