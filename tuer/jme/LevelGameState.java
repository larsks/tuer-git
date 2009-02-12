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
    
    private static final String levelIDPrefix="level";
    
    private static final String networkIDPrefix="NID";
    
    private static final String cellIDPrefix="CID";
    
    private static final int unknownID=-1;
    
    
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
        try{//TODO: create a Level instance and add it into the children of the root
            URL levelDataDirectoryURL=LevelGameState.class.getResource("/jbin/");
            File levelDataDirectory=new File(levelDataDirectoryURL.toURI());
            FileFilter cellsModelsFilter=new LevelJBINModelsFileFilter(levelIndex,true,false,false);
            HashMap<Integer,List<Spatial>> cellsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> cellsList;
            int[] parsingResult;
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
                 parsingResult=parseIdentifier(model.getName());
                 if((cellsList=cellsListsTable.get(parsingResult[1]))==null)
                     {cellsList=new ArrayList<Spatial>();
                      cellsListsTable.put(Integer.valueOf(parsingResult[1]),cellsList);
                     }
                 cellsList.add(model);
                 //attach it to the root node of the state
                 //FIXME: remove it, it was the naive way of handling the level
                 levelState.rootNode.attachChild(model);                
                 levelState.rootNode.updateRenderState();
                }   
            //UNCOMMENT IT WHEN IT IS READY
            /*
            HashMap<Integer,List<Spatial>> portalsListsTable=new HashMap<Integer,List<Spatial>>();
            List<Spatial> portalsList;
            FileFilter portalsModelsFilter=new LevelJBINModelsFileFilter(levelIndex,false,true,false);            
            for(File f:levelDataDirectory.listFiles(portalsModelsFilter))
                {model=(Spatial)BinaryImporter.getInstance().load(f);
                 model.setModelBound(new BoundingBox());
                 model.updateModelBound();
                 parsingResult=parseIdentifier(model.getName());
                 if((portalsList=portalsListsTable.get(parsingResult[1]))==null)
                     {portalsList=new ArrayList<Spatial>();
                      portalsListsTable.put(Integer.valueOf(parsingResult[1]),portalsList);
                     }
                 portalsList.add(model);
                }           
            Network networkNode;
            Integer networkIndex;
            int cellIndex;
            //for each network (graph)
            for(Map.Entry<Integer,List<Spatial>> cellEntry:cellsListsTable.entrySet())
                {networkIndex=cellEntry.getKey();
                 //create a node per network
                 networkNode=new Network(levelIndex,networkIndex.intValue());
                 //for each cell (node)
                 for(Spatial cellModel:cellEntry.getValue())
                     {parsingResult=parseIdentifier(cellModel.getName());
                      //create a node instance of the class Cell (JME)
                      //add this node into its list of children
                      cellIndex=parsingResult[2];
                      networkNode.attachChild(new Cell(levelIndex,networkIndex,cellIndex,cellModel));                     
                     }
                 //add each node that represents the "root" of a graph into
                 //the list of children of the root node
                 //TODO: add it to the Level instance
                 levelState.rootNode.attachChild(networkNode);
                }
            for(Map.Entry<Integer,List<Spatial>> portalEntry:portalsListsTable.entrySet())
                {networkIndex=portalEntry.getKey();
                 //look for the network
                 networkNode=null;
                 //TODO: use the Level instance instead of the root node
                 for(Spatial spatial:levelState.rootNode.getChildren())
                     if(((Network)spatial).isIdentifiedBy(levelIndex,networkIndex.intValue()))
                         {networkNode=(Network)spatial;
                          break;
                         }
                 //TODO: parse the model name to get the CIDs of the linked cells
                 //TODO: create a node instance of the class Portal (JME)
                 //      whose name is CID<cid>CID<cid>
                 //TODO: look for those cells
                 //TODO: add them into this portal
                 //TODO: add this portal into them
                }*/
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
    
    @Deprecated
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
    }
    
    private static final int[] parseIdentifier(String identifier){
        int indexOfLevelIDTag=identifier.indexOf(levelIDPrefix);
        int indexOfNetworkIDTag=identifier.indexOf(networkIDPrefix);
        int indexOfFirstCellIDTag=identifier.indexOf(cellIDPrefix);
        int indexOflastCellIDTag=indexOfFirstCellIDTag!=-1?identifier.indexOf(cellIDPrefix,indexOfFirstCellIDTag+cellIDPrefix.length()):-1;
        int levelIndex,networkIndex,firstCellIndex,lastCellIndex;
        if(indexOfLevelIDTag==-1)
            {levelIndex=unknownID;
             if(indexOfNetworkIDTag==-1)
                 {networkIndex=unknownID;
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           lastCellIndex=unknownID;
                       else
                           lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                      }
                  else
                      {if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
             else
                 {if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
            }
        else
            {if(indexOfNetworkIDTag==-1)
                 {networkIndex=unknownID;
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {levelIndex=Integer.parseInt(identifier.substring(indexOfLevelIDTag+levelIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {levelIndex=Integer.parseInt(identifier.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {levelIndex=Integer.parseInt(identifier.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
             else
                 {levelIndex=Integer.parseInt(identifier.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOfNetworkIDTag));
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {networkIndex=Integer.parseInt(identifier.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(identifier.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(identifier.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                     
                 }
            }
        return(new int[]{levelIndex,networkIndex,firstCellIndex,lastCellIndex});
    }

    private static final class LevelJBINModelsFileFilter implements FileFilter{
        
        
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
            result=file.isFile()&&filename.endsWith(".jbin")&&filename.startsWith(levelIDPrefix+index);
            if(result&&(!includesPortals||!includesCells||!includesNonCellsAndPortals))
                {//Detects the tag used by the cells and the portals
                 int firstIndexOfCIDTag=filename.indexOf(cellIDPrefix);
                 //If it is in the filename
                 if(firstIndexOfCIDTag!=-1)
                     {//If the tag occurs once, then it is a cell, otherwise it is a portal
                      if(firstIndexOfCIDTag==filename.lastIndexOf(cellIDPrefix))
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
            return(result);
        }       
    }
}
