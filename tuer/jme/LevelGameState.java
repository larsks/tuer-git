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
import java.util.List;
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
import com.jme.scene.Spatial.CullHint;
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
    
    public LevelGameState(int levelIndex,Camera cam,JMEGameServiceProvider gameServiceProvider){
        //naming convention: the level's name is composed of the prefix "LID" followed by its index
        super("LID"+levelIndex);
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
        LevelGameState levelState=new LevelGameState(levelIndex,cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        try{Level levelNode=new Level(levelIndex,levelState.nodeIdentifiers);
            //load the weapon
            Spatial weaponModel=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/pistol.jbin"));
            weaponModel.setName("pistol");
            //the weapon is too big...
            weaponModel.setLocalScale(1.0f/1000.0f);
            Quaternion q=new Quaternion();
            q.fromAngles(FastMath.PI/2.0f,0.0f,-FastMath.PI/4.0f);
            weaponModel.setLocalRotation(q);
            weaponModel.setLocalTranslation(new Vector3f(115.0f,0.0f,220.0f));
            weaponModel.setModelBound(new BoundingBox());
            weaponModel.updateModelBound();
            weaponModel.updateRenderState();
            weaponModel.updateGeometricState(0.0f,true);
            levelNode.attachDescendant(weaponModel);           
            weaponModel=(Spatial)BinaryImporter.getInstance().load(LevelGameState.class.getResource("/jbin/pistol2.jbin"));
            weaponModel.setName("pistol2");
            //the weapon is too big...
            weaponModel.setLocalScale(0.02f);
            Quaternion q2=new Quaternion();
            q2.fromAngles(0.0f,-FastMath.PI/2.0f,0.0f);
            weaponModel.setLocalRotation(q2);
            weaponModel.setLocalTranslation(new Vector3f(115.25f,0.0f,220.0f));
            weaponModel.setModelBound(new BoundingBox());
            weaponModel.updateModelBound();
            weaponModel.updateRenderState();
            weaponModel.updateGeometricState(0.0f,true);
            levelNode.attachDescendant(weaponModel);
            //TODO: use it
            /*Node pistolNode=NodeFactory.getInstance().getNode("/jbin/pistol.jbin");
            pistolNode.setName("pistol");
            //the weapon is too big...
            pistolNode.setLocalScale(1.0f/1000.0f);
            Quaternion q=new Quaternion();
            q.fromAngles(FastMath.PI/2.0f,0.0f,-FastMath.PI/4.0f);
            pistolNode.setLocalRotation(q);
            final Vector3f pistolLocation=new Vector3f(115.0f,0.0f,220.0f);
            pistolNode.setLocalTranslation(pistolLocation);
            levelNode.attachDescendant(pistolNode);*/
            levelState.rootNode.attachChild(levelNode);
            levelState.rootNode.updateRenderState();
            levelState.rootNode.updateGeometricState(0.0f,true);            
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();} 
        //return a level game state
        return(levelState);
    }
    
    @Override
    public final void update(final float tpf){
        super.update(tpf);
        //TODO: save the previous location
        input.update(tpf);
        //TODO: save the next location
        //TODO: test collisions
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
}
