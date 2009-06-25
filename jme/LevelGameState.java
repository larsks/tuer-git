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
import bean.ILevelModelBean;
import bean.NodeIdentifier;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    private NodeIdentifier[] nodeIdentifiers;   
    
    public LevelGameState(int levelIndex,Camera cam,JMEGameServiceProvider gameServiceProvider){
        //naming convention: the level's name is composed of the prefix "LID" followed by its index
        super("LID"+levelIndex);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        //TODO: this operation should be moved in a FullLevel class
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
        //setup the camera
        cam.setLocation(new Vector3f(spawnPos[0],spawnPos[1],spawnPos[2]));
        cam.update();
        previousTime=System.currentTimeMillis();
    }

    public static final GameState getInstance(int levelIndex,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState(levelIndex,cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        try{Level levelNode=new Level(levelIndex,levelState.nodeIdentifiers);
            //TODO: these operations should be moved in a FullLevel class
            //load the weapon
            Node pistolNode=NodeFactory.getInstance().getNode("/jbin/pistol.jbin",new Quaternion().fromAngles(FastMath.PI/2.0f,0.0f,-FastMath.PI/4.0f),new Vector3f(0.001f,0.001f,0.001f),new Vector3f(115.0f,0.0f,220.0f));
            pistolNode.setName("pistol");
            levelNode.attachDescendant(pistolNode);
            
            Node pistol2Node=NodeFactory.getInstance().getNode("/jbin/pistol2.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.02f,0.02f,0.02f),new Vector3f(115.25f,0.0f,220.0f));
            pistol2Node.setName("pistol2");
            levelNode.attachDescendant(pistol2Node);
            
            levelState.rootNode.attachChild(levelNode);
            levelState.rootNode.updateRenderState();
            levelState.rootNode.updateGeometricState(0.0f,true);
            levelState.rootNode.setCullHint(CullHint.Never);
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();}
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
        super.render(tpf);       
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }
}
