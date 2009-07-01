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

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.CameraNode;
import com.jme.scene.TriMesh;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Box;
import com.jme.system.DisplaySystem;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState{


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    private CameraNode playerNode;

    
    public LevelGameState(int levelIndex,Camera cam,JMEGameServiceProvider gameServiceProvider){
        //naming convention: the level's name is composed of the prefix "LID" followed by its index
        super("LID"+levelIndex);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        String fullLevelFilename="/xml/"+name+".xml";
        FullLevel fullLevel=(FullLevel)Utils.decodeObjectInXMLFile(fullLevelFilename);
        String fullWorldFilename="/xml/WID0.xml";     
        FullWorld fullWorld=(FullWorld)Utils.decodeObjectInXMLFile(fullWorldFilename);
        Level levelNode=fullLevel.getLevelNode(fullWorld);
        //setup the camera
        cam.setLocation(fullLevel.getInitialPlayerPosition());
        cam.update();       
        playerNode=new CameraNode("player",cam);
        playerNode.updateFromCamera();
        //Box playerBox=new Box("player box",new Vector3f(-0.25f,-0.25f,-0.25f),new Vector3f(0.25f,0.25f,0.25f));
        Box playerBox=new Box("player box",/*cam.getLocation()*/new Vector3f(),0.25f,0.25f,0.25f);
        playerNode.attachChild(playerBox);
        playerNode.setModelBound(new BoundingBox(/*cam.getLocation(),0.25f,0.25f,0.25f*/));
        playerNode.updateModelBound();          
        playerNode.updateWorldBound();
        previousTime=System.currentTimeMillis();
        rootNode.attachChild(levelNode);
        rootNode.updateRenderState();
        rootNode.updateGeometricState(0.0f,true);
        rootNode.setCullHint(CullHint.Never);
    }

    public static final GameState getInstance(int levelIndex,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState(levelIndex,cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");           
        return(levelState);
    }
    
    @Override
    public final void update(final float tpf){
        super.update(tpf);       
        //TODO: save the previous location
        input.update(tpf);
        //TODO: save the next location
        //TODO: test collisions
        playerNode.updateFromCamera();
        playerNode.updateGeometricState(tpf,true);
        //workaround necessary only for bounding sphere and bounding box
        //playerNode.getWorldBound().getCenter().set(DisplaySystem.getDisplaySystem().getRenderer().getCamera().getLocation());
        System.out.println("collision: "+((Level)rootNode.getChild(0)).hasCollision(playerNode,false));
    }
    
    @Override
    public final void render(final float tpf){      
        super.render(tpf);       
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }
}
