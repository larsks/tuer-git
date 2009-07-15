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

import java.util.ArrayList;
import java.util.List;
import bean.NodeIdentifier;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

/**
 * Set of walls representing a single room and linked to other rooms
 * by portals
 * @author Julien Gouesse
 *
 */
public final class Cell extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    private List<Portal> portalsList;
     
    
    Cell(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,null);
    }
    
    /**
     * build a cell
     * @param levelID identifier of the level
     * @param networkID identifier of the network
     * @param cellID identifier of the cell
     * @param model set of walls
     */
    public Cell(int levelID,int networkID,int cellID,Spatial model){
        super(levelID,networkID,cellID);       
        portalsList=new ArrayList<Portal>();
        if(model!=null)
            if(model instanceof Geometry)
                attachChild(new InternalCellElement((Geometry)model,false));
            else
                {Node modelNode=(Node)model;
                 if(modelNode.getChildren()!=null)
                     {List<Spatial> modelChildren=new ArrayList<Spatial>();
                      modelChildren.addAll(modelNode.getChildren());
                      //this line below avoids a ConcurrentModificationException
                      modelNode.detachAllChildren();
                      for(Spatial modelChild:modelChildren)
                          attachChild(new InternalCellElement((Geometry)modelChild,false));
                     }                
                }
        //set a bounding box to the cell
        updateWorldBound();
        //hide it by default (don't do it earlier because it caused a regression)
        setCullHint(CullHint.Always);
    }
    
    @Override
    public final void updateWorldBound(){
        if((lockedMode & Spatial.LOCKED_BOUNDS)==0||children!=null) 
            {BoundingVolume worldBound = null;
             InternalCellElement cellElement;
             for(Spatial child:children)
                 {if(child!=null)
                      {cellElement=(InternalCellElement)child;
                       if(!cellElement.isShared())
                           {if(worldBound!=null)
                                {// merge current world bound with child world bound
                                 worldBound.mergeLocal(child.getWorldBound());
                                } 
                            else 
                                {// set world bound to first non-null child world bound
                                 if(child.getWorldBound()!=null) 
                                     worldBound=child.getWorldBound().clone(this.worldBound);                             
                                }
                           }
                      }
                }
             this.worldBound = worldBound;
            }
    }
    
    final void addPortal(Portal portal){
        portalsList.add(portal);
    }
    
    final int getPortalCount(){
        return(portalsList.size());
    }
    
    final Portal getPortalAt(int index){
        return(portalsList.get(index));
    }
    
    final boolean contains(Vector3f point){
        boolean result;
        if(children!=null&&children.size()>0)
            result=getWorldBound().contains(point);
        else
            result=false;
        return(result);
    }
    
    /**
     * A cell has no controller and does not update 
     * its children (its parent updates its children), 
     * this method does nothing 
     * @param time
     */
    @Override
    public final void updateWorldData(float time){}
    
    @Override
    public final boolean hasCollision(Spatial spatial, boolean checkTriangles){
        //BoundingVolume wallsBound=((TriMesh)((InternalCellElement)getChild(0)).getChild(0)).getModelBound();
        BoundingVolume wallsBound=getWorldBound();
        boolean result;
        if(wallsBound.intersects(spatial.getWorldBound()))
            {result=false;
             //the spatial is on the limit or inside the cell
             //perform deeper checks, look at its children
             //skip the first internal element as it has been checked before
             InternalCellElement cellElement;
             for(int childIndex=0;childIndex<children.size();childIndex++)
                 {cellElement=(InternalCellElement)getChild(childIndex);
                  //only use shared objects (all objects except walls)
                  if(cellElement.isShared()&&spatial.hasCollision(cellElement,checkTriangles))
                      {result=true;
                       break;
                      }
                 }
             //FIXME check collisions with walls
             //if the spatial is not completely inside the cell
             /*BoundingVolume mergedBoundingVolume=wallsBound.merge(spatial.getWorldBound());
             if(!result && mergedBoundingVolume.getVolume()>wallsBound.getVolume())
                 {boolean portalFound=false;
                  for(Portal portal:portalsList)
                      if(portal.hasCollision(spatial,checkTriangles))
                          {portalFound=true;
                           BoundingBox portalBound=(BoundingBox)portal.getWorldBound();
                           Vector3f portalExtent=portalBound.getExtent(null);
                           float minDimensionValue=Float.MAX_VALUE;
                           int minValueIndex=-1;
                           for(int i=0;i<3;i++)
                               if(portalBound.getExtent(null).get(i)<=minDimensionValue)
                                   {minDimensionValue=portalExtent.get(i);
                                    minValueIndex=i;
                                   }
                           BoundingBox mergedBox=(BoundingBox)portalBound.merge(spatial.getWorldBound());                          
                           Vector3f mergedBoxExtent=mergedBox.getExtent(null);
                           for(int i=0;i<3;i++)
                               //if one dimension of the merged bounding volume except 
                               //the smallest one is bigger than those of the portal
                               if(i!=minValueIndex && 
                                  mergedBoxExtent.get(i)>portalExtent.get(i))
                                   {result=true;
                                    break;
                                   }
                           break;
                          }
                  if(!portalFound)
                      result=true;
                 }*/
            }
        else
            result=false;
        return(result);
    }
}
