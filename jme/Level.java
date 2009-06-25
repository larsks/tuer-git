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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.VBOInfo;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;

import bean.NodeIdentifier;

/**
 * 
 * @author Julien Gouesse
 * TODO: 
 *       - move some operations from LevelGameState to Level
 *       (render, hasCollisions)
 */
public final class Level extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    Level(){
        this(NodeIdentifier.unknownID);
    }
    
    public Level(int levelID){
        super(levelID);
    }
    
    @Deprecated
    public Level(int levelID,NodeIdentifier[] nodeIdentifiers)throws IOException{
        //hide it by default
        this.setCullHint(CullHint.Always);
        //levelState.rootNode.attachChild(levelNode);
        HashMap<Integer,List<Spatial>> cellsListsTable=new HashMap<Integer,List<Spatial>>();
        List<Spatial> cellsList;
        //load the models
        String cellModelFilename;
        Spatial model;
        for(NodeIdentifier nodeID:nodeIdentifiers)
            //check if the node is a cell
            if(nodeID.getSecondaryCellID()==NodeIdentifier.unknownID)
            {cellModelFilename=nodeID.toString()+".jbin";
             model=(Spatial)BinaryImporter.getInstance().load(Level.class.getResource("/jbin/"+cellModelFilename));
             model.setModelBound(new BoundingBox());
             model.updateModelBound();
             //Activate back face culling
             model.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createCullState());
             ((CullState)model.getRenderState(RenderState.StateType.Cull)).setCullFace(CullState.Face.Back);
             model.updateRenderState();
             //Use VBO if the required extension is available
             ((TriMesh)model).setVBOInfo(new VBOInfo(DisplaySystem.getDisplaySystem().getRenderer().supportsVBO()));
             model.lock();
             if((cellsList=cellsListsTable.get(nodeID.getNetworkID()))==null)
                 {cellsList=new ArrayList<Spatial>();
                  cellsListsTable.put(Integer.valueOf(nodeID.getNetworkID()),cellsList);
                 }
             cellsList.add(model);         
            }           
        //load the portals from the files
        HashMap<Integer,List<Spatial>> portalsListsTable=new HashMap<Integer,List<Spatial>>();
        List<Spatial> portalsList;
        String portalModelFilename;
        for(NodeIdentifier nodeID:nodeIdentifiers)
            //check if it is a portal
            if(nodeID.getSecondaryCellID()!=NodeIdentifier.unknownID)
            {portalModelFilename=nodeID.toString()+".jbin";
             model=(Spatial)BinaryImporter.getInstance().load(Level.class.getResource("/jbin/"+portalModelFilename));
             model.setModelBound(new BoundingBox());
             model.updateModelBound();
             if((portalsList=portalsListsTable.get(nodeID.getNetworkID()))==null)
                 {portalsList=new ArrayList<Spatial>();
                  portalsListsTable.put(Integer.valueOf(nodeID.getNetworkID()),portalsList);
                 }
             portalsList.add(model);
            }
        NodeIdentifier nodeID;
        Network networkNode;
        int networkIndex;
        Cell cellNode;
        //create the nodes that represent the cells and the networks
        //for each network (graph)
        for(Map.Entry<Integer,List<Spatial>> cellEntry:cellsListsTable.entrySet())
            {networkIndex=cellEntry.getKey().intValue();
             //create a node per network
             networkNode=new Network(levelID,networkIndex);
             //hide it by default
             networkNode.setCullHint(CullHint.Always);
             //for each cell (node)
             for(Spatial cellModel:cellEntry.getValue())
                 {nodeID=NodeIdentifier.getInstance(cellModel.getName());
                  //create a node instance of the class Cell (JME)                
                  cellNode=new Cell(levelID,networkIndex,nodeID.getCellID(),cellModel);
                  //hide it by default
                  cellNode.setCullHint(CullHint.Always);
                  //add this node into its list of children
                  networkNode.attachChild(cellNode);
                 }
             //add each node that represents the "root" of a graph into
             //the list of children of the level node
             this.attachChild(networkNode);
            }
        //create the nodes that represent the portals
        Portal portalNode;
        Cell c1,c2;
        for(Map.Entry<Integer,List<Spatial>> portalEntry:portalsListsTable.entrySet())
            {networkIndex=portalEntry.getKey().intValue();
             //look for the network
             networkNode=null;
             for(Spatial spatial:this.getChildren())
                 if(((Network)spatial).isIdentifiedBy(levelID,networkIndex))
                     {networkNode=(Network)spatial;
                      break;
                     }
             for(Spatial portalModel:portalEntry.getValue())
                 {//get the CIDs of the linked cells
                  nodeID=NodeIdentifier.getInstance(portalModel.getName());
                  //look for those cells
                  c1=null;
                  for(Spatial cell:networkNode.getChildren())
                      if(((Cell)cell).isIdentifiedBy(levelID,networkIndex,nodeID.getCellID()))
                          {c1=(Cell)cell;
                           break;
                          }
                  c2=null;
                  for(Spatial cell:networkNode.getChildren())
                      if(((Cell)cell).isIdentifiedBy(levelID,networkIndex,nodeID.getSecondaryCellID()))
                          {c2=(Cell)cell;
                           break;
                          }
                  //create a node instance of the class Portal (JME) 
                  portalNode=new Portal(levelID,networkIndex,nodeID.getCellID(),nodeID.getSecondaryCellID(),c1,c2,portalModel);
                  //hide by default
                  portalNode.setCullHint(CullHint.Always);
                  //add this portal into them
                  c1.addPortal(portalNode);
                  c2.addPortal(portalNode);
                 }                
            }
    }
    
    @Override
    public final int attachChild(Spatial child){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChild(child));
    }
    
    @Override
    public final int attachChildAt(Spatial child, int index){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChildAt(child,index));
    }
    
    /**
     * attaches a spatial to this node indirectly by attaching
     * it to some of its grand-child depending on its location
     * @param spatial object inserted into the scene graph
     */
    public final void attachDescendant(Spatial spatial){
        new DescendantController(this,spatial);
    }
    
    final Cell locate(Vector3f position,Cell previousLocation){
        Cell location=null;
        int previousNetworkIndex=previousLocation!=null?previousLocation.getNetworkID():0;
        int networkCount=getChildren()!=null?getChildren().size():0;
        Network networkNode;
        for(int networkIndex=previousNetworkIndex,j=0;j<networkCount&&location==null;j++,networkIndex=(networkIndex+1)%networkCount)
            {networkNode=(Network)getChild(networkIndex);
             if(networkIndex==previousNetworkIndex && previousLocation!=null)
                 location=networkNode.locate(position,previousLocation);
             else
                 location=networkNode.locate(position);
            }
        return(location);
    }
    
    final List<IdentifiedNode> getVisibleNodesList(Cell currentLocation){
        List<IdentifiedNode> visibleNodesList=new ArrayList<IdentifiedNode>();
        if(currentLocation!=null)
            {//FIXME: we should check if the level is in the view frustum
             //add the level node
             visibleNodesList.add(this);
             //add the network node
             Network networkNode=(Network)currentLocation.getParent();
             visibleNodesList.add(networkNode);            
             //look for other visible cells in the network
             visibleNodesList.addAll(networkNode.getVisibleNodesList(currentLocation));
            }
        return(visibleNodesList);
    }
    
    final List<Network.FrustumParameters> getFrustumParametersList(Cell currentLocation){
        List<Network.FrustumParameters> frustumParametersList=new ArrayList<Network.FrustumParameters>();
        if(currentLocation!=null)
            frustumParametersList.addAll(((Network)currentLocation.getParent()).getFrustumParametersList());
        return(frustumParametersList);
    }
    
    final List<Cell> getContainingNodesList(Spatial spatial,Cell previousLocation){
        List<Cell> containingNodesList=new ArrayList<Cell>();
        int previousNetworkIndex=previousLocation!=null?previousLocation.getNetworkID():0;
        int networkCount=getChildren()!=null?getChildren().size():0;
        Network networkNode;
        for(int networkIndex=previousNetworkIndex,j=0;j<networkCount&&containingNodesList.isEmpty();j++,networkIndex=(networkIndex+1)%networkCount)
            {networkNode=(Network)getChild(networkIndex);
             containingNodesList.addAll(networkNode.getContainingNodesList(spatial));
            }
        return(containingNodesList);
    }
    
    private static final class DescendantController extends Controller{

        
        private static final long serialVersionUID=1L;
        
        private Level level;
        
        private Spatial monitored3DObject;
        
        private List<Cell> containingCellsList;
        
        private Cell previousLocation;
        
        private HashMap<Cell,InternalCellElement> cloneMap;
        
        private InternalCellElementPool clonePool;

        
        private DescendantController(Level level,Spatial spatial){
            this.monitored3DObject=spatial;
            this.containingCellsList=new ArrayList<Cell>();
            this.level=level;
            this.previousLocation=null;
            this.cloneMap=new HashMap<Cell, InternalCellElement>();
            this.clonePool=new InternalCellElementPool(spatial,this);
            //force an initial update to ensure this spatial has a parent
            this.update(0.0f);
        }
        
        @Override
        public final void update(float time){
            List<Cell> currentContainingCellsList=level.getContainingNodesList(monitored3DObject,previousLocation);
            InternalCellElement cellElement;
            if(!currentContainingCellsList.isEmpty())
                previousLocation=currentContainingCellsList.get(0);
            else
                throw new UnsupportedOperationException("an object cannot be updated anymore when it goes outside the scene graph");
            for(Cell cell:containingCellsList)
                if(!currentContainingCellsList.contains(cell))
                    {cellElement=cloneMap.remove(cell);
                     cell.detachChild(cellElement);
                     clonePool.releaseInstance(cellElement);
                    }
            for(Cell cell:currentContainingCellsList)
                if(!containingCellsList.contains(cell))
                    {cellElement=clonePool.getFreshInstance();
                     cloneMap.put(cell,cellElement);
                     cell.attachChild(cellElement);
                    }
            //update the list of containing cells
            containingCellsList.clear();
            containingCellsList.addAll(currentContainingCellsList);
        }      
    }
    
    private static final class InternalCellElementPool{
        
        
        private Spatial spatial;
        
        private List<InternalCellElement> usedCellElementList;
        
        private List<InternalCellElement> unusedCellElementList;
        
        private DescendantController controller;
        
        
        private InternalCellElementPool(Spatial spatial,DescendantController controller){
            this.spatial=spatial;
            this.usedCellElementList=new ArrayList<InternalCellElement>();
            this.unusedCellElementList=new ArrayList<InternalCellElement>();
            this.controller=controller;
        }
        
        
        private void releaseInstance(InternalCellElement cellElement){
            int index=usedCellElementList.indexOf(cellElement);
            if(index!=-1)
                unusedCellElementList.add(usedCellElementList.remove(index));
            else
                throw new IllegalArgumentException("cannot release an instance not obtained from this pool");
        }
        
        private InternalCellElement getFreshInstance(){
            InternalCellElement freshInstance;
            if(unusedCellElementList.isEmpty())
                {if(spatial instanceof Geometry)
                     freshInstance=new InternalCellElement((Geometry)spatial,true);
                    
                 else
                     freshInstance=new InternalCellElement((Node)spatial,true);
                 freshInstance.addController(controller);
                }
            else
                freshInstance=unusedCellElementList.remove(0);
            usedCellElementList.add(freshInstance);
            return(freshInstance);
        }
    }
}