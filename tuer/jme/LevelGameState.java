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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import bean.ILevelModelBean;
import bean.NodeIdentifier;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.AbstractCamera;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.VBOInfo;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.geom.BufferUtils;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    private Cell previousPlayerCellNode;
    
    private NodeIdentifier[] nodeIdentifiers;   
    
    public LevelGameState(String name,Camera cam,JMEGameServiceProvider gameServiceProvider){
        super(name);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        ObjectInputStream ois=null;
        ILevelModelBean ilmb=null;
        try{ois=new ObjectInputStream(new BufferedInputStream(getClass().getResourceAsStream("/data/"+name+".data")));
            ilmb=(ILevelModelBean)ois.readObject();
            ois.close();
           }
        catch(Throwable t)
        {throw new RuntimeException("Unable to read the binary level model bean file",t);}
        nodeIdentifiers=new NodeIdentifier[ilmb.getIdentifiedNodeNames().length];
        for(int index=0;index<nodeIdentifiers.length;index++)
            nodeIdentifiers[index]=NodeIdentifier.getInstance(ilmb.getIdentifiedNodeNames()[index]);
        final float[] spawnPos=ilmb.getInitialSpawnPosition();
        cam.setLocation(new Vector3f(spawnPos[0],spawnPos[1],spawnPos[2]));
        cam.update();
        previousTime=System.currentTimeMillis();
        previousPlayerCellNode=null;
    }

    public static final GameState getInstance(int levelIndex,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState("level"+levelIndex,cam,gameServiceProvider);
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
            //load the models
            String cellModelFilename;
            for(NodeIdentifier nodeID:levelState.nodeIdentifiers)
                //check if the node is a cell
                if(nodeID.getSecondaryCellID()==NodeIdentifier.unknownID)
                {cellModelFilename=nodeID.toString()+".jbin";
                 model=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/"+cellModelFilename));
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
            for(NodeIdentifier nodeID:levelState.nodeIdentifiers)
                //check if it is a portal
                if(nodeID.getSecondaryCellID()!=NodeIdentifier.unknownID)
                {portalModelFilename=nodeID.toString()+".jbin";
                 model=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/"+portalModelFilename));
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
                      portalNode=new Portal(levelIndex,networkIndex,nodeID.getCellID(),nodeID.getSecondaryCellID(),c1,c2,portalModel);
                      //hide by default
                      portalNode.setCullHint(CullHint.Always);
                      //add this portal into them
                      c1.addPortal(portalNode);
                      c2.addPortal(portalNode);
                     }                
                }
            //load the weapon
            Spatial weaponModel=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/pistol.jbin"));
            weaponModel.setName("pistol");
            //the weapon is too big...
            weaponModel.setLocalScale(1.0f/1000.0f);
            Quaternion q=new Quaternion();
            q.fromAngles(FastMath.PI/2.0f,0.0f,-FastMath.PI/4.0f);
            weaponModel.setLocalRotation(q);
            weaponModel.setModelBound(new BoundingBox());
            weaponModel.updateModelBound();
            weaponModel.updateRenderState();   
            final Vector3f pistolLocation=new Vector3f(115.0f,0.0f,220.0f);
            //create a node with the model and at this location
            Node pistolNode=new Node("pistol");
            pistolNode.attachChild(weaponModel);
            pistolNode.setLocalTranslation(pistolLocation);
            //FIXME: rather use a controller
            pistolNode.updateGeometricState(0.0f,true);
            List<Cell> containingCellsList;
            for(Spatial level:levelState.rootNode.getChildren())
                {levelNode=(Level)level;
                 containingCellsList=levelNode.getContainingNodesList(pistolNode,null);
                 if(!containingCellsList.isEmpty())
                     {InternalCellElement sharedNode;
                      for(Cell containingCell:containingCellsList)
                          {//create a shared node
                           sharedNode=new InternalCellElement(pistolNode,true);
                           //attach it to a cell that contains it
                           containingCell.attachChild(sharedNode);
                          }
                      break;
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
        AbstractCamera cam=(AbstractCamera)DisplaySystem.getDisplaySystem().getRenderer().getCamera();
        Vector3f playerLocation=cam.getLocation();
        Level levelNode;
        Cell currentPlayerCellNode;
        List<IdentifiedNode> visibleNodesList=null;
        List<Network.FrustumParameters> frustumParametersList=null;
        for(Spatial level:rootNode.getChildren())
            {levelNode=(Level)level;
             if((currentPlayerCellNode=levelNode.locate(playerLocation,previousPlayerCellNode))!=null)
                 {previousPlayerCellNode=currentPlayerCellNode;
                  visibleNodesList=levelNode.getVisibleNodesList(currentPlayerCellNode);
                  //get the frustum parameters for debugging
                  frustumParametersList=levelNode.getFrustumParametersList(currentPlayerCellNode);                 
                  break;
                 }
            }
        if(visibleNodesList!=null)
            {//explicitly show the visible nodes
             for(Node visibleNode:visibleNodesList)
                 visibleNode.setCullHint(CullHint.Never);
             super.render(tpf);
             if(frustumParametersList!=null)
                 {Vector3f[] vertices=new Vector3f[8];
                  Vector3f[] normals=new Vector3f[8];
                  ColorRGBA[][] colors=new ColorRGBA[][]
                                               {new ColorRGBA[]{ColorRGBA.black,ColorRGBA.black,ColorRGBA.black,ColorRGBA.black,ColorRGBA.black,ColorRGBA.black,ColorRGBA.black,ColorRGBA.black},
                                                new ColorRGBA[]{ColorRGBA.red,ColorRGBA.red,ColorRGBA.red,ColorRGBA.red,ColorRGBA.red,ColorRGBA.red,ColorRGBA.red,ColorRGBA.red},
                                                new ColorRGBA[]{ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue,ColorRGBA.blue},
                                                new ColorRGBA[]{ColorRGBA.green,ColorRGBA.green,ColorRGBA.green,ColorRGBA.green,ColorRGBA.green,ColorRGBA.green,ColorRGBA.green,ColorRGBA.green}
                                               };
                  
                  Vector2f screenCoord=new Vector2f();
                  Line line;
                  line=new Line("");
                  float frustumWidth=cam.getFrustumRight()-cam.getFrustumLeft();
                  float frustumHeight=cam.getFrustumTop()-cam.getFrustumBottom();
                  int i=0;
                  for(Network.FrustumParameters frustumParam:frustumParametersList)
                      {//use getWorldCoordinates                      
                       screenCoord.x=((frustumParam.getLeft()-cam.getFrustumLeft())/frustumWidth)*cam.getWidth();
                       screenCoord.y=((frustumParam.getBottom()-cam.getFrustumBottom())/frustumHeight)*cam.getHeight();
                       vertices[0]=cam.getWorldCoordinates(screenCoord,0.0f,vertices[0]);
                       screenCoord.x=((frustumParam.getLeft()-cam.getFrustumLeft())/frustumWidth)*cam.getWidth();
                       screenCoord.y=((frustumParam.getTop()-cam.getFrustumBottom())/frustumHeight)*cam.getHeight();
                       vertices[1]=cam.getWorldCoordinates(screenCoord,0.0f,vertices[1]);
                       screenCoord.x=((frustumParam.getRight()-cam.getFrustumLeft())/frustumWidth)*cam.getWidth();
                       screenCoord.y=((frustumParam.getTop()-cam.getFrustumBottom())/frustumHeight)*cam.getHeight();
                       vertices[2]=cam.getWorldCoordinates(screenCoord,0.0f,vertices[2]);
                       screenCoord.x=((frustumParam.getRight()-cam.getFrustumLeft())/frustumWidth)*cam.getWidth();
                       screenCoord.y=((frustumParam.getBottom()-cam.getFrustumBottom())/frustumHeight)*cam.getHeight();
                       vertices[3]=cam.getWorldCoordinates(screenCoord,0.0f,vertices[3]);
                       vertices[4]=vertices[0];
                       vertices[5]=vertices[3];
                       vertices[6]=vertices[1];
                       vertices[7]=vertices[2];
                       //draw them
                       FloatBuffer vertexBuffer=BufferUtils.createFloatBuffer(vertices);
                       line.setVertexCount(vertexBuffer.limit()/3);
                       line.setVertexBuffer(vertexBuffer);
                       line.setNormalBuffer(BufferUtils.createFloatBuffer(normals));
                       line.setColorBuffer(BufferUtils.createFloatBuffer(colors[i%colors.length]));
                       line.generateIndices();
                       line.setLineWidth(5);
                       DisplaySystem.getDisplaySystem().getRenderer().draw(line);
                       i++;
                      }
                 }
             //reset the cull hint of all visible nodes
             for(Node visibleNode:visibleNodesList)
                 visibleNode.setCullHint(CullHint.Always);
             visibleNodesList.clear();   
            }
        else
            {System.out.println("no visible node");
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
