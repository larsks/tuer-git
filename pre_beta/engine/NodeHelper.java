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
package engine;

import java.util.ArrayList;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.DataMode;

/*public */final class NodeHelper{

	static final void setGLAccessMode(Spatial spatial,DataMode glAccessMode){
		spatial.getSceneHints().setDataMode(glAccessMode);
	    if(spatial instanceof Node)
	        {for(Spatial child:((Node)spatial).getChildren())
	             setGLAccessMode(child,glAccessMode);
	        }
	    else
	        if(spatial instanceof Mesh)
	            {((Mesh)spatial).getMeshData().getNormalCoords().setNeedsRefresh(true);
	             ((Mesh)spatial).getMeshData().getTextureCoords().get(0).setNeedsRefresh(true);
	             ((Mesh)spatial).getMeshData().getVertexCoords().setNeedsRefresh(true);
	             if(((Mesh)spatial).getMeshData().getColorCoords()!=null)
	                 ((Mesh)spatial).getMeshData().getColorCoords().setNeedsRefresh(true);
	             if(((Mesh)spatial).getMeshData().getFogCoords()!=null)
	                 ((Mesh)spatial).getMeshData().getFogCoords().setNeedsRefresh(true);
	             if(((Mesh)spatial).getMeshData().getTangentCoords()!=null)
	                 ((Mesh)spatial).getMeshData().getTangentCoords().setNeedsRefresh(true);
	             if(((Mesh)spatial).getMeshData().getInterleavedData()!=null)
	                 ((Mesh)spatial).getMeshData().getInterleavedData().setNeedsRefresh(true);
	            }
	}
	
	static final void setModelBound(Spatial spatial,Class<? extends BoundingVolume> boundingClass){
		if(spatial instanceof Node)
            {for(Spatial child:((Node)spatial).getChildren())
            	 setModelBound(child,boundingClass);
            }
        else
            if(spatial instanceof Mesh)
				try{((Mesh)spatial).setModelBound(boundingClass.newInstance());} 
		        catch(InstantiationException e)
		        {e.printStackTrace();} 
		        catch(IllegalAccessException e)
		        {e.printStackTrace();}
	}
	
	static final void detachChildren(Node node,ArrayList<? extends Spatial> childrenList){
		for(Spatial child:childrenList)
			node.detachChild(child);
	}
}
