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

import com.sun.opengl.util.BufferUtil;
import javax.media.opengl.GL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexSetFactory implements IVertexSetProvider{


    private AbstractDynamicVertexSetFactory dynamicVertexSetFactory=null;
    
    private AbstractStaticVertexSetFactory staticVertexSetFactory=null;
    
    private GL gl=null;
    
    private final static VertexSetFactory instance=new VertexSetFactory();
    
    static int GL_MAX_ELEMENTS_VERTICES=3000;
    
    
    private VertexSetFactory(){}
    
    
    public static VertexSetFactory getInstance(){
        return(instance);
    }
    
    public void bindVertexSetProvider(IVertexSetProvider provider){
        
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,float[] array,int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             staticVertexSetFactory=null;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory(gl);
        return(staticVertexSetFactory.newVertexSet(array,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,IVertexSet vertexSet,int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             staticVertexSetFactory=null;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory(gl);
        return(staticVertexSetFactory.newVertexSet(vertexSet,mode));
    }
       
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,float[] array,int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             dynamicVertexSetFactory=null;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory(gl);
        return(dynamicVertexSetFactory.newVertexSet(array,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,FloatBuffer floatBuffer,int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             dynamicVertexSetFactory=null;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory(gl);
        return(dynamicVertexSetFactory.newVertexSet(floatBuffer,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,IVertexSet vertexSet,int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             dynamicVertexSetFactory=null;
            }
        if(dynamicVertexSetFactory==null)
            dynamicVertexSetFactory=new DynamicVertexSetFactory(gl);
        return(dynamicVertexSetFactory.newVertexSet(vertexSet,mode));
    }
    
    private void updateMaxElementsVertices(){
        IntBuffer buffer=BufferUtil.newIntBuffer(1);	
        this.gl.glGetIntegerv(GL.GL_MAX_ELEMENTS_VERTICES,buffer);
        buffer.position(0);
        GL_MAX_ELEMENTS_VERTICES=buffer.get();
    }


    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,final FloatBuffer floatBuffer,final int mode){
        if(this.gl!=gl)
            {this.gl=gl;
             updateMaxElementsVertices();
             staticVertexSetFactory=null;
            }
        if(staticVertexSetFactory==null)
            staticVertexSetFactory=new StaticVertexSetFactory(gl);      
        //prevents the program from creating a too big VBO       
        return(VertexSetDecoratorFactory.newVertexSet(staticVertexSetFactory,floatBuffer,mode));
    }
}
