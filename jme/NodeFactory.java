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
package jme;

import java.io.IOException;
import java.util.HashMap;
import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.CloneImportExport;
import com.jme.util.export.binary.BinaryImporter;

final class NodeFactory{
    
    
    private HashMap<String,CloneImportExport> clonerTable;

    private static final NodeFactory instance=new NodeFactory();
    
    
    private NodeFactory(){
        clonerTable=new HashMap<String, CloneImportExport>();
    }
   
    
    static final NodeFactory getInstance(){
        return(instance);
    }   
    
    final Node getNode(String path,Quaternion rotation,Vector3f scale,Vector3f translation){
        CloneImportExport cloner=clonerTable.get(path);
        if(cloner==null)
            {cloner=new CloneImportExport();
             Node node=null;
             Spatial spatial=null;
             try{spatial=(Spatial)BinaryImporter.getInstance().load(NodeFactory.class.getResource(path));} 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             if(spatial!=null)
                 {if(spatial instanceof Node)
                      node=(Node)spatial;
                  else
                      {node=new Node(spatial.getName());
                       node.attachChild(spatial);
                      }
                 }
             if(node!=null)
                 {cloner.saveClone(node);
                  clonerTable.put(path,cloner);
                 }
            }
        Node clone=(Node)cloner.loadClone();
        if(rotation!=null)
            clone.setLocalRotation(rotation);
        if(scale!=null)
            clone.setLocalScale(scale);
        if(translation!=null)
            clone.setLocalTranslation(translation);
        clone.setModelBound(new BoundingBox());
        clone.updateModelBound();
        clone.updateRenderState();
        clone.updateGeometricState(0.0f,true);   
        return(clone);
    }
}
