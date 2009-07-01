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
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import bean.NodeIdentifier;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;

public final class FullLevel implements Serializable{

    
    private static final long serialVersionUID = 1L;

    static{Utils.forceHandlingOfTransientModifiersForXMLSerialization(FullLevel.class);}
    
    private Vector3f initialPlayerPosition;
    
    private NodeIdentifier[] nodeIdentifiers;
    
    private HashMap<Vector3f,String> entityLocationTable;
    
    private transient Level levelNode;
    
    
    public FullLevel(){}


    public final Vector3f getInitialPlayerPosition(){
        return(initialPlayerPosition);
    }

    public final void setInitialPlayerPosition(Vector3f initialPlayerPosition){
        this.initialPlayerPosition=initialPlayerPosition;
    }

    public final NodeIdentifier[] getNodeIdentifiers(){
        return(nodeIdentifiers);
    }

    public final void setNodeIdentifiers(NodeIdentifier[] nodeIdentifiers){
        this.nodeIdentifiers=nodeIdentifiers;
    }

    public final HashMap<Vector3f,String> getEntityLocationTable(){
        return(entityLocationTable);
    }

    public final void setEntityLocationTable(HashMap<Vector3f,String> entityLocationTable){
        this.entityLocationTable=entityLocationTable;
    }
    
    public final Level getLevelNode(FullWorld world){
        if(levelNode==null)
            {int levelIndex=nodeIdentifiers[0].getLevelID();
             try{levelNode=new Level(levelIndex,nodeIdentifiers);} 
             catch(IOException ioe)
             {throw new RuntimeException("The creation of the level has failed!",ioe);}
             Node entityNode;
             EntityParameters entityParam;
             Vector3f translation,scale;
             Quaternion rotation;
             TextureState ts;
             URL textureURL;
             for(Entry<Vector3f,String> entry:entityLocationTable.entrySet())
                 {//reset transform parameters to neutral values
                  translation=new Vector3f(0.0f,0.0f,0.0f);
                  scale=new Vector3f(1.0f,1.0f,1.0f);
                  rotation=new Quaternion().fromAngles(0.0f,0.0f,0.0f);
                  entityParam=world.getEntityParameterTable().get(entry.getValue());
                  if(entityParam!=null)
                      {if(entityParam.getRotation()!=null)
                           rotation.set(entityParam.getRotation());
                       if(entityParam.getScale()!=null)
                           scale.set(entityParam.getScale());
                       if(entityParam.getTranslation()!=null)
                           translation.set(entityParam.getTranslation());
                       if(entityParam.getAlternativeTexturePath()!=null&&!entityParam.getAlternativeTexturePath().equals(""))
                           {textureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,entityParam.getAlternativeTexturePath());
                            if(textureURL==null)
                                textureURL=FullLevel.class.getResource(entityParam.getAlternativeTexturePath());
                            ts=DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
                            ts.setEnabled(true);
                            ts.setTexture(TextureManager.loadTexture(textureURL,
                                   Texture.MinificationFilter.BilinearNoMipMaps,
                                   Texture.MagnificationFilter.Bilinear));
                           }
                       else
                           ts=null;       
                       if(entityParam.getArtificialIntelligenceClassName()!=null)
                           {//TODO: use the AI
                            
                           }
                      }
                  else
                      ts=null;
                  translation.addLocal(entry.getKey());
                  entityNode=NodeFactory.getInstance().getNode(entry.getValue(),rotation,scale,translation);
                  entityNode.setName(entry.getValue());
                  if(ts!=null)
                      entityNode.setRenderState(ts);
                  levelNode.attachDescendant(entityNode);
                 }
            }       
        return(levelNode);
    }
}
