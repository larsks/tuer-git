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

package main;

import java.nio.FloatBuffer;
import javax.media.opengl.GL;

public class VertexSetSeeker implements IVertexSetProvider{


    private IVertexSetProvider delegate=null;
    
    private final static VertexSetSeeker instance=new VertexSetSeeker();
    
    
    private VertexSetSeeker(){}
    
    
    public static VertexSetSeeker getInstance(){
        return(instance);
    }

    public void bindVertexSetProvider(IVertexSetProvider provider){
         delegate=provider;		
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,float[] array,int mode){
        return(delegate.getIStaticVertexSetInstance(gl,array,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,FloatBuffer floatBuffer,int mode){
        return(delegate.getIStaticVertexSetInstance(gl,floatBuffer,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(GL gl,IVertexSet vertexSet,int mode){
        return(delegate.getIStaticVertexSetInstance(gl,vertexSet,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,float[] array,int mode){
        return(delegate.getIDynamicVertexSetInstance(gl,array,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,FloatBuffer floatBuffer,int mode){
        return(delegate.getIDynamicVertexSetInstance(gl,floatBuffer,mode));
    }
    
    public IDynamicVertexSet getIDynamicVertexSetInstance(GL gl,IVertexSet vertexSet,int mode){
        return(delegate.getIDynamicVertexSetInstance(gl,vertexSet,mode));
    }

}
