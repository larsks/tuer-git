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

import java.nio.FloatBuffer;
import javax.media.opengl.GL;


class StaticVertexSetFactory extends AbstractStaticVertexSetFactory{

    
    private AbstractStaticVertexSetFactory delegate;
    
    private StaticDefaultVertexSetFactory rescueDelegate;
    
    
    StaticVertexSetFactory(GL gl){       
        if((gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")
                || gl.isExtensionAvailable("GL_EXT_vertex_buffer_object"))
                && (gl.isFunctionAvailable("glBindBufferARB")
                        || gl.isFunctionAvailable("glBindBuffer"))
                        && (gl.isFunctionAvailable("glBufferDataARB")
                                || gl.isFunctionAvailable("glBufferData"))
                                && (gl.isFunctionAvailable("glDeleteBuffersARB")
                                        || gl.isFunctionAvailable("glDeleteBuffers"))
                                        && (gl.isFunctionAvailable("glGenBuffersARB")
                                                || gl.isFunctionAvailable("glGenBuffers")))
            {delegate=new StaticVertexBufferObjectFactory(gl);
             rescueDelegate=null;
            }
        else
            /*if(gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")
	    && gl.isFunctionAvailable("glLockArraysEXT")
	    && gl.isFunctionAvailable("glUnlockArraysEXT"))
	        {delegate=new CompiledVertexArrayFactory(gl);
		 rescueDelegate=null;
		}
	    else*/
            if(gl.isFunctionAvailable("glCallList")
                    && gl.isFunctionAvailable("glCallLists")
                    && gl.isFunctionAvailable("glDeleteLists")
                    && gl.isFunctionAvailable("glGenLists")
                    && gl.isFunctionAvailable("glNewList")
                    && gl.isFunctionAvailable("glEndList"))
                {delegate=new DisplayListFactory(gl);
                 rescueDelegate=new StaticDefaultVertexSetFactory(gl);
                }
            else
                {delegate=new StaticDefaultVertexSetFactory(gl);
                 rescueDelegate=null;
                }
    }
        
    
    @Override
    StaticVertexSet newVertexSet(float[] array,int mode){
        StaticVertexSet set=null;
        try {set=delegate.newVertexSet(array,mode);}
        catch(RuntimeException re)
        {if(rescueDelegate!=null)
             set=rescueDelegate.newVertexSet(array,mode);
        }
        return(set);
    }
    
    @Override
    StaticVertexSet newVertexSet(FloatBuffer floatBuffer,int mode){
        StaticVertexSet set=null;
        try {set=delegate.newVertexSet(floatBuffer,mode);}
        catch(RuntimeException re)
        {if(rescueDelegate!=null)
             set=rescueDelegate.newVertexSet(floatBuffer,mode);
        }
        return(set);
    }
    
    @Override
    StaticVertexSet newVertexSet(IVertexSet vertexSet,int mode){
        StaticVertexSet set=null;
        try {set=delegate.newVertexSet(vertexSet,mode);}
        catch(RuntimeException re)
        {if(rescueDelegate!=null)
             set=rescueDelegate.newVertexSet(vertexSet,mode);
        }
        return(set);
    }
}
