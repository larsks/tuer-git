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
import bean.NodeIdentifier;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.VBOInfo;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    private Cell previousPlayerCellNode;
    
    private static final String[] cellsModelsFilenames=new String[]
    {
            "level0NID0CID26.jbin",
            "level0NID0CID85.jbin",
            "level0NID1CID2.jbin",
            "level0NID0CID135.jbin",
            "level0NID0CID12.jbin",
            "level0NID0CID52.jbin",
            "level0NID0CID53.jbin",
            "level0NID0CID106.jbin",
            "level0NID0CID45.jbin",
            "level0NID0CID18.jbin",
            "level0NID0CID13.jbin",
            "level0NID0CID69.jbin",
            "level0NID0CID8.jbin",
            "level0NID0CID59.jbin",
            "level0NID0CID93.jbin",
            "level0NID0CID10.jbin",
            "level0NID0CID35.jbin",
            "level0NID0CID44.jbin",
            "level0NID0CID117.jbin",
            "level0NID0CID127.jbin",
            "level0NID0CID138.jbin",
            "level0NID0CID129.jbin",
            "level0NID0CID89.jbin",
            "level0NID0CID54.jbin",
            "level0NID0CID70.jbin",
            "level0NID0CID152.jbin",
            "level0NID0CID130.jbin",
            "level0NID0CID108.jbin",
            "level0NID0CID72.jbin",
            "level0NID0CID90.jbin",
            "level0NID0CID131.jbin",
            "level0NID0CID14.jbin",
            "level0NID0CID134.jbin",
            "level0NID0CID51.jbin",
            "level0NID0CID133.jbin",
            "level0NID0CID71.jbin",
            "level0NID0CID123.jbin",
            "level0NID0CID64.jbin",
            "level0NID0CID120.jbin",
            "level0NID0CID2.jbin",
            "level0NID0CID96.jbin",
            "level0NID0CID22.jbin",
            "level0NID0CID139.jbin",
            "level0NID0CID4.jbin",
            "level0NID0CID62.jbin",
            "level0NID0CID113.jbin",
            "level0NID0CID34.jbin",
            "level0NID1CID4.jbin",
            "level0NID0CID3.jbin",
            "level0NID0CID92.jbin",
            "level0NID0CID58.jbin",
            "level0NID0CID103.jbin",
            "level0NID0CID0.jbin",
            "level0NID0CID142.jbin",
            "level0NID0CID100.jbin",
            "level0NID0CID114.jbin",
            "level0NID1CID1.jbin",
            "level0NID0CID16.jbin",
            "level0NID0CID19.jbin",
            "level0NID0CID84.jbin",
            "level0NID0CID150.jbin",
            "level0NID0CID118.jbin",
            "level0NID0CID6.jbin",
            "level0NID0CID7.jbin",
            "level0NID0CID151.jbin",
            "level0NID0CID78.jbin",
            "level0NID0CID42.jbin",
            "level0NID0CID81.jbin",
            "level0NID0CID74.jbin",
            "level0NID0CID101.jbin",
            "level0NID0CID125.jbin",
            "level0NID0CID155.jbin",
            "level0NID0CID40.jbin",
            "level0NID0CID154.jbin",
            "level0NID0CID104.jbin",
            "level0NID0CID80.jbin",
            "level0NID0CID145.jbin",
            "level0NID0CID5.jbin",
            "level0NID0CID124.jbin",
            "level0NID0CID121.jbin",
            "level0NID0CID144.jbin",
            "level0NID0CID116.jbin",
            "level0NID0CID65.jbin",
            "level0NID0CID136.jbin",
            "level0NID0CID43.jbin",
            "level0NID0CID128.jbin",
            "level0NID0CID75.jbin",
            "level0NID0CID48.jbin",
            "level0NID0CID20.jbin",
            "level0NID0CID107.jbin",
            "level0NID0CID36.jbin",
            "level0NID0CID149.jbin",
            "level0NID0CID105.jbin",
            "level0NID0CID56.jbin",
            "level0NID0CID98.jbin",
            "level0NID0CID77.jbin",
            "level0NID0CID37.jbin",
            "level0NID0CID94.jbin",
            "level0NID0CID21.jbin",
            "level0NID0CID33.jbin",
            "level0NID0CID30.jbin",
            "level0NID0CID91.jbin",
            "level0NID0CID115.jbin",
            "level0NID0CID87.jbin",
            "level0NID0CID39.jbin",
            "level0NID0CID109.jbin",
            "level0NID0CID148.jbin",
            "level0NID0CID99.jbin",
            "level0NID0CID32.jbin",
            "level0NID0CID73.jbin",
            "level0NID0CID66.jbin",
            "level0NID1CID3.jbin",
            "level0NID0CID112.jbin",
            "level0NID0CID146.jbin",
            "level0NID0CID46.jbin",
            "level0NID0CID76.jbin",
            "level0NID0CID88.jbin",
            "level0NID0CID119.jbin",
            "level0NID0CID153.jbin",
            "level0NID0CID9.jbin",
            "level0NID0CID95.jbin",
            "level0NID0CID41.jbin",
            "level0NID0CID25.jbin",
            "level0NID0CID141.jbin",
            "level0NID0CID102.jbin",
            "level0NID0CID111.jbin",
            "level0NID0CID49.jbin",
            "level0NID0CID110.jbin",
            "level0NID0CID156.jbin",
            "level0NID0CID97.jbin",
            "level0NID0CID67.jbin",
            "level0NID0CID27.jbin",
            "level0NID0CID47.jbin",
            "level0NID0CID17.jbin",
            "level0NID0CID1.jbin",
            "level0NID0CID24.jbin",
            "level0NID0CID147.jbin",
            "level0NID0CID132.jbin",
            "level0NID0CID86.jbin",
            "level0NID0CID23.jbin",
            "level0NID0CID126.jbin",
            "level0NID0CID55.jbin",
            "level0NID0CID137.jbin",
            "level0NID0CID57.jbin",
            "level0NID0CID11.jbin",
            "level0NID0CID83.jbin",
            "level0NID0CID15.jbin",
            "level0NID0CID28.jbin",
            "level0NID0CID143.jbin",
            "level0NID0CID63.jbin",
            "level0NID0CID60.jbin",
            "level0NID0CID50.jbin",
            "level0NID0CID31.jbin",
            "level0NID1CID0.jbin",
            "level0NID0CID122.jbin",
            "level0NID0CID38.jbin",
            "level0NID0CID79.jbin",
            "level0NID0CID61.jbin",
            "level0NID0CID140.jbin",
            "level0NID0CID29.jbin",
            "level0NID0CID68.jbin",
            "level0NID0CID82.jbin"
    };
    
    private static String[] portalsModelsFilenames=new String[]
    {
            "level0NID0CID87CID95.jbin",
            "level0NID0CID55CID52.jbin",
            "level0NID0CID13CID10.jbin",
            "level0NID0CID7CID13.jbin",
            "level0NID0CID96CID108.jbin",
            "level0NID0CID112CID100.jbin",
            "level0NID0CID64CID68.jbin",
            "level0NID0CID136CID146.jbin",
            "level0NID0CID113CID130.jbin",
            "level0NID0CID68CID74.jbin",
            "level0NID0CID127CID139.jbin",
            "level0NID0CID64CID61.jbin",
            "level0NID0CID32CID33.jbin",
            "level0NID0CID119CID105.jbin",
            "level0NID0CID89CID96.jbin",
            "level0NID0CID65CID62.jbin",
            "level0NID0CID75CID69.jbin",
            "level0NID0CID66CID62.jbin",
            "level0NID0CID153CID149.jbin",
            "level0NID0CID87CID79.jbin",
            "level0NID0CID45CID41.jbin",
            "level0NID1CID3CID4.jbin",
            "level0NID0CID154CID149.jbin",
            "level0NID0CID134CID121.jbin",
            "level0NID0CID124CID109.jbin",
            "level0NID0CID89CID81.jbin",
            "level0NID0CID98CID91.jbin",
            "level0NID0CID83CID76.jbin",
            "level0NID0CID56CID53.jbin",
            "level0NID0CID151CID155.jbin",
            "level0NID0CID99CID111.jbin",
            "level0NID0CID3CID5.jbin",
            "level0NID0CID35CID38.jbin",
            "level0NID0CID146CID150.jbin",
            "level0NID0CID121CID107.jbin",
            "level0NID1CID1CID2.jbin",
            "level0NID0CID104CID95.jbin",
            "level0NID0CID103CID117.jbin",
            "level0NID0CID116CID133.jbin",
            "level0NID0CID117CID133.jbin",
            "level0NID0CID48CID45.jbin",
            "level0NID0CID76CID69.jbin",
            "level0NID0CID92CID99.jbin",
            "level0NID0CID37CID35.jbin",
            "level0NID0CID103CID95.jbin",
            "level0NID0CID44CID40.jbin",
            "level0NID0CID54CID51.jbin",
            "level0NID1CID0CID1.jbin",
            "level0NID0CID79CID72.jbin",
            "level0NID0CID1CID3.jbin",
            "level0NID0CID26CID27.jbin",
            "level0NID0CID39CID36.jbin",
            "level0NID0CID75CID82.jbin",
            "level0NID0CID92CID82.jbin",
            "level0NID0CID86CID94.jbin",
            "level0NID0CID136CID122.jbin",
            "level0NID0CID54CID57.jbin",
            "level0NID0CID139CID147.jbin",
            "level0NID0CID110CID126.jbin",
            "level0NID0CID106CID96.jbin",
            "level0NID0CID78CID86.jbin",
            "level0NID0CID34CID36.jbin",
            "level0NID0CID64CID69.jbin",
            "level0NID0CID107CID96.jbin",
            "level0NID0CID9CID5.jbin",
            "level0NID0CID47CID44.jbin",
            "level0NID0CID148CID152.jbin",
            "level0NID0CID60CID57.jbin",
            "level0NID0CID18CID21.jbin",
            "level0NID0CID8CID5.jbin",
            "level0NID0CID28CID29.jbin",
            "level0NID0CID31CID32.jbin",
            "level0NID0CID0CID1.jbin",
            "level0NID1CID3CID2.jbin",
            "level0NID0CID129CID112.jbin",
            "level0NID0CID63CID60.jbin",
            "level0NID0CID63CID67.jbin",
            "level0NID0CID94CID102.jbin",
            "level0NID0CID115CID102.jbin",
            "level0NID0CID25CID24.jbin",
            "level0NID0CID65CID70.jbin",
            "level0NID0CID11CID15.jbin",
            "level0NID0CID15CID18.jbin",
            "level0NID0CID88CID80.jbin",
            "level0NID0CID22CID19.jbin",
            "level0NID0CID143CID131.jbin",
            "level0NID0CID41CID38.jbin",
            "level0NID0CID85CID77.jbin",
            "level0NID0CID71CID66.jbin",
            "level0NID0CID7CID5.jbin",
            "level0NID0CID137CID126.jbin",
            "level0NID0CID140CID141.jbin",
            "level0NID0CID123CID109.jbin",
            "level0NID0CID50CID47.jbin",
            "level0NID0CID5CID11.jbin",
            "level0NID0CID144CID135.jbin",
            "level0NID0CID73CID80.jbin",
            "level0NID0CID24CID20.jbin",
            "level0NID0CID4CID6.jbin",
            "level0NID0CID128CID140.jbin",
            "level0NID0CID77CID69.jbin",
            "level0NID0CID90CID82.jbin",
            "level0NID0CID27CID28.jbin",
            "level0NID0CID153CID155.jbin",
            "level0NID0CID12CID6.jbin",
            "level0NID0CID73CID67.jbin",
            "level0NID0CID100CID113.jbin",
            "level0NID0CID51CID47.jbin",
            "level0NID0CID16CID12.jbin",
            "level0NID0CID59CID56.jbin",
            "level0NID0CID21CID22.jbin",
            "level0NID0CID143CID148.jbin",
            "level0NID0CID84CID76.jbin",
            "level0NID0CID114CID102.jbin",
            "level0NID0CID144CID149.jbin",
            "level0NID0CID29CID30.jbin",
            "level0NID0CID95CID105.jbin",
            "level0NID0CID91CID82.jbin",
            "level0NID0CID84CID88.jbin",
            "level0NID0CID132CID115.jbin",
            "level0NID0CID121CID135.jbin",
            "level0NID0CID25CID26.jbin",
            "level0NID0CID106CID120.jbin",
            "level0NID0CID116CID103.jbin",
            "level0NID0CID46CID49.jbin",
            "level0NID0CID40CID37.jbin",
            "level0NID0CID23CID19.jbin",
            "level0NID0CID72CID67.jbin",
            "level0NID0CID97CID104.jbin",
            "level0NID0CID35CID33.jbin",
            "level0NID0CID39CID43.jbin",
            "level0NID0CID85CID93.jbin",
            "level0NID0CID118CID104.jbin",
            "level0NID0CID142CID129.jbin",
            "level0NID0CID59CID62.jbin",
            "level0NID0CID48CID52.jbin",
            "level0NID0CID0CID2.jbin",
            "level0NID0CID119CID110.jbin",
            "level0NID0CID16CID19.jbin",
            "level0NID0CID58CID61.jbin",
            "level0NID0CID101CID94.jbin",
            "level0NID0CID90CID97.jbin",
            "level0NID0CID110CID98.jbin",
            "level0NID0CID17CID14.jbin",
            "level0NID0CID83CID82.jbin",
            "level0NID0CID126CID138.jbin",
            "level0NID0CID43CID46.jbin",
            "level0NID0CID122CID108.jbin",
            "level0NID0CID109CID98.jbin",
            "level0NID0CID131CID115.jbin",
            "level0NID0CID93CID100.jbin",
            "level0NID0CID81CID74.jbin",
            "level0NID0CID58CID55.jbin",
            "level0NID0CID127CID111.jbin",
            "level0NID0CID49CID53.jbin",
            "level0NID0CID10CID5.jbin",
            "level0NID0CID150CID152.jbin",
            "level0NID0CID154CID156.jbin",
            "level0NID0CID30CID31.jbin",
            "level0NID0CID125CID110.jbin",
            "level0NID0CID20CID17.jbin",
            "level0NID0CID78CID70.jbin",
            "level0NID0CID2CID4.jbin",
            "level0NID0CID147CID151.jbin",
            "level0NID0CID8CID14.jbin",
            "level0NID0CID130CID120.jbin",
            "level0NID0CID34CID33.jbin",
            "level0NID0CID141CID129.jbin",
            "level0NID0CID145CID135.jbin",
            "level0NID0CID42CID38.jbin",
            "level0NID0CID111CID128.jbin"
    };
    
    
    public LevelGameState(String name,Camera cam,JMEGameServiceProvider gameServiceProvider){
        super(name);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        //FIXME: dirty thing to set the first position
        cam.setLocation(new Vector3f(115.0f,0.0f,223.0f));
        previousTime=System.currentTimeMillis();
        previousPlayerCellNode=null;
    }

    public static final GameState getInstance(int levelIndex,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState("",cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        Spatial model;       
        try{//create the node of the level and attach it at the root node
            Level levelNode=new Level(levelIndex);
            //hide it by default
            levelNode.setCullHint(CullHint.Always);
            levelState.rootNode.attachChild(levelNode);
            HashMap<Integer,List<Spatial>> cellsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> cellsList;
            NodeIdentifier nodeID;
            //load the models
            for(String cellModelFilename:cellsModelsFilenames)
                {model=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/"+cellModelFilename));
                 model.setModelBound(new BoundingBox());
                 model.updateModelBound();
                 //Activate back face culling
                 model.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createCullState());
                 ((CullState)model.getRenderState(RenderState.StateType.Cull)).setCullFace(CullState.Face.Back);
                 model.updateRenderState();
                 //Use VBO if the required extension is available
                 ((TriMesh)model).setVBOInfo(new VBOInfo(gameServiceProvider.getConfigurationDetector().isVBOsupported()));
                 model.lock();
                 //parse its model name and put it into the good list,
                 //one list per network
                 nodeID=NodeIdentifier.getInstance(model.getName());
                 if((cellsList=cellsListsTable.get(nodeID.getNetworkID()))==null)
                     {cellsList=new ArrayList<Spatial>();
                      cellsListsTable.put(Integer.valueOf(nodeID.getNetworkID()),cellsList);
                     }
                 cellsList.add(model);         
                }
            //load the portals from the files
            HashMap<Integer,List<Spatial>> portalsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> portalsList;
            for(String portalModelFilename:portalsModelsFilenames)
                {model=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/"+portalModelFilename));
                 model.setModelBound(new BoundingBox());
                 model.updateModelBound();
                 nodeID=NodeIdentifier.getInstance(model.getName());
                 if((portalsList=portalsListsTable.get(nodeID.getNetworkID()))==null)
                     {portalsList=new ArrayList<Spatial>();
                      portalsListsTable.put(Integer.valueOf(nodeID.getNetworkID()),portalsList);
                     }
                 portalsList.add(model);
                }
            Network networkNode;
            int networkIndex;
            Cell cellNode;
            //create the nodes that represent the cells and the networks
            //for each network (graph)
            for(Map.Entry<Integer,List<Spatial>> cellEntry:cellsListsTable.entrySet())
                {networkIndex=cellEntry.getKey().intValue();
                 //create a node per network
                 networkNode=new Network(levelIndex,networkIndex);
                 //hide it by default
                 networkNode.setCullHint(CullHint.Always);
                 //for each cell (node)
                 for(Spatial cellModel:cellEntry.getValue())
                     {nodeID=NodeIdentifier.getInstance(cellModel.getName());
                      //create a node instance of the class Cell (JME)                     
                      cellNode=new Cell(levelIndex,networkIndex,nodeID.getCellID(),cellModel);
                      //hide it by default
                      cellNode.setCullHint(CullHint.Always);
                      //add this node into its list of children
                      networkNode.attachChild(cellNode);
                     }
                 //add each node that represents the "root" of a graph into
                 //the list of children of the level node
                 levelNode.attachChild(networkNode);
                }
            //create the nodes that represent the portals
            Portal portalNode;
            Cell c1,c2;
            for(Map.Entry<Integer,List<Spatial>> portalEntry:portalsListsTable.entrySet())
                {networkIndex=portalEntry.getKey().intValue();
                 //look for the network
                 networkNode=null;
                 for(Spatial spatial:levelNode.getChildren())
                     if(((Network)spatial).isIdentifiedBy(levelIndex,networkIndex))
                         {networkNode=(Network)spatial;
                          break;
                         }
                 for(Spatial portalModel:portalEntry.getValue())
                     {//get the CIDs of the linked cells
                      nodeID=NodeIdentifier.getInstance(portalModel.getName());
                      //look for those cells
                      c1=null;
                      for(Spatial cell:networkNode.getChildren())
                          if(((Cell)cell).isIdentifiedBy(levelIndex,networkIndex,nodeID.getCellID()))
                              {c1=(Cell)cell;
                               break;
                              }
                      c2=null;
                      for(Spatial cell:networkNode.getChildren())
                          if(((Cell)cell).isIdentifiedBy(levelIndex,networkIndex,nodeID.getSecondaryCellID()))
                              {c2=(Cell)cell;
                               break;
                              }
                      //create a node instance of the class Portal (JME) 
                      portalNode=new Portal(levelIndex,networkIndex,nodeID.getCellID(),nodeID.getSecondaryCellID(),c1,c2);
                      //hide by default
                      portalNode.setCullHint(CullHint.Always);
                      //add this portal into them
                      c1.addPortal(portalNode);
                      c2.addPortal(portalNode);
                     }                
                }
            levelState.rootNode.updateGeometricState(0.0f,true);
            levelState.rootNode.updateRenderState();
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();} 
        /*System.out.println("vertex count="+model.getVertexCount());
        System.out.println("free memory = "+Runtime.getRuntime().freeMemory());
        System.out.println("total memory = "+Runtime.getRuntime().totalMemory());
        System.out.println("max memory = "+Runtime.getRuntime().maxMemory());*/
        //return a true level game state
        return(levelState);
    }
    
    @Override
    public final void update(final float tpf){
        super.update(tpf);
        input.update(tpf);
    }
    
    @Override
    public final void render(final float tpf){
        Camera cam=DisplaySystem.getDisplaySystem().getRenderer().getCamera();
        Vector3f playerLocation=cam.getLocation();
        Level levelNode;
        Cell currentPlayerCellNode;
        List<IdentifiedNode> visibleNodesList=null;       
        for(Spatial level:rootNode.getChildren())
            {levelNode=(Level)level;
             if((currentPlayerCellNode=levelNode.locate(playerLocation,previousPlayerCellNode))!=null)
                 {previousPlayerCellNode=currentPlayerCellNode;
                  visibleNodesList=levelNode.getVisibleNodesList(currentPlayerCellNode);
                  break;
                 }
            }
        if(visibleNodesList!=null)
            {//explicitly show the visible nodes
             for(Node visibleNode:visibleNodesList)
                 visibleNode.setCullHint(CullHint.Never);
             super.render(tpf);
             //reset the cull hint of all visible nodes
             for(Node visibleNode:visibleNodesList)
                 visibleNode.setCullHint(CullHint.Always);
             visibleNodesList.clear();   
            }
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }

    /*
    private static final class LevelJBINModelsFileFilter implements FileFilter{
        
        
        private static final String suffix=".jbin";
        
        private int index;       
        
        private boolean includesCells;
        
        private boolean includesPortals;
        
        private boolean includesNonCellsAndPortals;
        
        
        private LevelJBINModelsFileFilter(int index,boolean includesCells,boolean includesPortals,boolean includesNonCellsAndPortals){
            this.index=index;
            this.includesCells=includesCells;
            this.includesPortals=includesPortals;
            this.includesNonCellsAndPortals=includesNonCellsAndPortals;
        }


        @Override
        public boolean accept(File file){
            String filename=file.getName();
            boolean result;
            if(filename.length()<=suffix.length())
                result=false;
            else
                {NodeIdentifier nodeID=NodeIdentifier.getInstance(filename.substring(0,filename.length()-suffix.length()));
                 result=file.isFile()&&filename.endsWith(suffix)&&nodeID.getLevelID()==index;
                 if(result&&(!includesPortals||!includesCells||!includesNonCellsAndPortals))
                     {if(nodeID.getCellID()!=NodeIdentifier.unknownID)
                          {if(nodeID.getSecondaryCellID()==NodeIdentifier.unknownID)
                               {if(!includesCells)
                                    result=false;
                               }
                           else
                               {if(!includesPortals)
                                    result=false;
                               }
                          }
                      else
                          {if(!includesNonCellsAndPortals)
                               result=false;
                          }
                     }   
                }                    
            return(result);
        }       
    }*/
}
