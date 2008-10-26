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
    public IStaticVertexSet getIStaticVertexSetInstance(float[] array,int mode){
        return(delegate.getIStaticVertexSetInstance(array,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(FloatBuffer floatBuffer,int mode){
        return(delegate.getIStaticVertexSetInstance(floatBuffer,mode));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(IVertexSet vertexSet,int mode){
        return(delegate.getIStaticVertexSetInstance(vertexSet,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(float[] array,int mode){
        return(delegate.getIDynamicVertexSetInstance(array,mode));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(FloatBuffer floatBuffer,int mode){
        return(delegate.getIDynamicVertexSetInstance(floatBuffer,mode));
    }
    
    public IDynamicVertexSet getIDynamicVertexSetInstance(IVertexSet vertexSet,int mode){
        return(delegate.getIDynamicVertexSetInstance(vertexSet,mode));
    }

}
