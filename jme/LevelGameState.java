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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.NodeIdentifier;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
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
            levelState.rootNode.attachChild(levelNode);
            URL levelDataDirectoryURL=LevelGameState.class.getResource("/jbin/");
            File levelDataDirectory=new File(levelDataDirectoryURL.toURI());
            FileFilter cellsModelsFilter=new LevelJBINModelsFileFilter(levelIndex,true,false,false);
            HashMap<Integer,List<Spatial>> cellsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> cellsList;
            NodeIdentifier nodeID;
            //load the models from the files
            for(File f:levelDataDirectory.listFiles(cellsModelsFilter))
                {model=(Spatial)BinaryImporter.getInstance().load(f);
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
            FileFilter portalsModelsFilter=new LevelJBINModelsFileFilter(levelIndex,false,true,false);            
            for(File f:levelDataDirectory.listFiles(portalsModelsFilter))
                {model=(Spatial)BinaryImporter.getInstance().load(f);
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
            //create the nodes that represent the cells and the networks
            //for each network (graph)
            for(Map.Entry<Integer,List<Spatial>> cellEntry:cellsListsTable.entrySet())
                {networkIndex=cellEntry.getKey().intValue();
                 //create a node per network
                 networkNode=new Network(levelIndex,networkIndex);
                 //for each cell (node)
                 for(Spatial cellModel:cellEntry.getValue())
                     {nodeID=NodeIdentifier.getInstance(cellModel.getName());
                      //create a node instance of the class Cell (JME)
                      //add this node into its list of children
                      networkNode.attachChild(new Cell(levelIndex,networkIndex,nodeID.getCellID(),cellModel));
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
                      //add this portal into them
                      c1.addPortal(portalNode);
                      c2.addPortal(portalNode);
                     }
                 
                }
            levelState.rootNode.updateRenderState();
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();} 
        catch(URISyntaxException urise)
        {urise.printStackTrace();}
        /*System.out.println("vertex count="+model.getVertexCount());
        System.out.println("free memory = "+Runtime.getRuntime().freeMemory());
        System.out.println("total memory = "+Runtime.getRuntime().totalMemory());
        System.out.println("max memory = "+Runtime.getRuntime().maxMemory());*/
        //return a true level game state
        return(levelState);
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        input.update(tpf);
    }
    
    @Override
    public final void render(final float tpf){
        Camera cam=DisplaySystem.getDisplaySystem().getRenderer().getCamera();
        Vector3f playerLocation=cam.getLocation();
        Level levelNode;
        Network networkNode;
        Cell cellNode,currentPlayerCellNode;
        /*TODO
          if(previousPlayerCellNode!=null)
              force the culling (CullHint.Always) of all nodes that were 
              visible by starting from this previous node
              perform a breadth-first search to find the player
              by starting from this previous node
          else
              force the culling (CullHint.Always) of all nodes
              perform a breadth-first search to find the player
          if(currentPlayerCellNode!=null)
              perform a breadth-first search to find the visible cells (CullHint.Never)
         */     
        //if the location has not changed, do not search the player again
        if(previousPlayerCellNode!=null)
            {if(((TriMesh)previousPlayerCellNode.getChild(0)).getModelBound().contains(playerLocation))                
                 currentPlayerCellNode=previousPlayerCellNode;
             else
                 currentPlayerCellNode=null;
            }
        else
            currentPlayerCellNode=null;
        if(currentPlayerCellNode==null)
            {for(Spatial level:rootNode.getChildren())
                 {levelNode=(Level)level;
                  for(Spatial network:levelNode.getChildren())
                      {networkNode=(Network)network;
                       if(currentPlayerCellNode==null)
                           for(Spatial cell:networkNode.getChildren())
                               {cellNode=(Cell)cell;
                                //locate the player
                                if(currentPlayerCellNode==null&&((TriMesh)cellNode.getChild(0)).getModelBound().contains(playerLocation))
                                    {currentPlayerCellNode=cellNode;   
                                     cellNode.setCullHint(CullHint.Never);
                                     networkNode.setCullHint(CullHint.Never);
                                     levelNode.setCullHint(CullHint.Never);
                                    }
                                else
                                    cellNode.setCullHint(CullHint.Always);
                            }
                     if(currentPlayerCellNode==null||networkNode.getNetworkID()!=currentPlayerCellNode.getNetworkID())
                         networkNode.setCullHint(CullHint.Always);
                    }
                 if(currentPlayerCellNode==null||levelNode.getLevelID()!=currentPlayerCellNode.getLevelID())
                     levelNode.setCullHint(CullHint.Always);
                }
             previousPlayerCellNode=currentPlayerCellNode;
            }
        super.render(tpf);
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }

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
    }
}
