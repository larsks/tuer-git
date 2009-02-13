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
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    
    public LevelGameState(String name,Camera cam,JMEGameServiceProvider gameServiceProvider){
        super(name);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        //FIXME: dirty thing to set the first position
        cam.setLocation(new Vector3f(115.0f,0.0f,223.0f));
        previousTime=System.currentTimeMillis();
    }

    public static final GameState getInstance(int levelIndex,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState("",cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        Spatial model;       
        try{Level levelNode=new Level(levelIndex);
            URL levelDataDirectoryURL=LevelGameState.class.getResource("/jbin/");
            File levelDataDirectory=new File(levelDataDirectoryURL.toURI());
            FileFilter cellsModelsFilter=new LevelJBINModelsFileFilter(levelIndex,true,false,false);
            HashMap<Integer,List<Spatial>> cellsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> cellsList;
            NodeIdentifier nodeID;
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
                 //attach it to the root node of the state
                 //FIXME: remove it, it was the naive way of handling the level
                 levelState.rootNode.attachChild(model);                
                 //levelState.rootNode.updateRenderState();
                 levelState.rootNode.attachChild(levelNode);
                }   
            //this part of the source code has not been tested and might be buggy
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
        /*TODO
         * locate the network and the cell containing the player
         * for each child node (network)
         *     set the cull hint of all its children at "always"
         *     if it contains the player
         *         perform a BFS visit to set the cull hint of 
         *         all its visible children at "never"
         */
        super.render(tpf);
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }
    
    /*@Deprecated
    @SuppressWarnings("unused")
    private static final int[] parseModelName(String modelname){
        int indexOfNIDTag=modelname.indexOf(networkIDPrefix);
        int levelIndex=Integer.parseInt(modelname.substring(levelIDPrefix.length(),indexOfNIDTag));
        int indexOfFirstCellIDTag=modelname.indexOf(cellIDPrefix);
        int indexOflastCellIDTag=modelname.lastIndexOf(cellIDPrefix);
        int networkIndex=Integer.parseInt(modelname.substring(indexOfNIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
        int firstCellIndex;
        int[] result;
        if(indexOfFirstCellIDTag<indexOflastCellIDTag)
            {firstCellIndex=Integer.parseInt(modelname.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
             int lastCellIndex=Integer.parseInt(modelname.substring(indexOflastCellIDTag+cellIDPrefix.length(),modelname.length()));
             result=new int[]{levelIndex,networkIndex,firstCellIndex,lastCellIndex};
            }
        else
            {firstCellIndex=Integer.parseInt(modelname.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),modelname.length()));
             result=new int[]{levelIndex,networkIndex,firstCellIndex};
            }
        return(result);
    }*/

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
