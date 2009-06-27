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
import java.net.URL;

import bean.ILevelModelBean;
import bean.NodeIdentifier;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;
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
            
            Node pistol3Node=NodeFactory.getInstance().getNode("/jbin/pistol3.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.03f,0.03f,0.03f),new Vector3f(114.5f,0.0f,220.0f));
            pistol3Node.setName("pistol3");
            levelNode.attachDescendant(pistol3Node);
            
            Node smachNode=NodeFactory.getInstance().getNode("/jbin/smach.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.2f,0.2f,0.2f),new Vector3f(114.0f,0.0f,220.0f));
            smachNode.setName("smach");
            levelNode.attachDescendant(smachNode);
            
            Node uziNode=NodeFactory.getInstance().getNode("/jbin/uzi.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.2f,0.2f,0.2f),new Vector3f(113.5f,0.0f,220.0f));
            uziNode.setName("uzi");
            levelNode.attachDescendant(uziNode);
            
            /*Node ak47Node=NodeFactory.getInstance().getNode("/jbin/AK47.jbin",new Quaternion().fromAngles(0.0f,0.0f,0.0f),new Vector3f(0.1f,0.1f,0.1f),new Vector3f(114.25f,0.0f,220.0f));
            ak47Node.setName("ak47");
            System.out.println("world bound: "+ak47Node.getWorldBound());
            levelNode.attachDescendant(ak47Node);*/
            
            Node laserNode=NodeFactory.getInstance().getNode("/jbin/laser.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.03f,0.03f,0.03f),new Vector3f(114.75f,0.0f,220.0f));
            laserNode.setName("laser");
            levelNode.attachDescendant(laserNode);
            //System.out.println("world bound: "+laserNode.getWorldBound());
            /*Node creatureNode=NodeFactory.getInstance().getNode("/jbin/creature.jbin",new Quaternion().fromAngles(0.0f,0.0f,0.0f),new Vector3f(1.0f,1.0f,1.0f),new Vector3f(117f,0.0f,222.0f));
            creatureNode.setName("creature");
            levelNode.attachDescendant(creatureNode);*/
            
            Node gigerAlienNode=NodeFactory.getInstance().getNode("/jbin/giger_alien.jbin",new Quaternion().fromAngles(0.0f,0.0f,0.0f),new Vector3f(0.3f,0.3f,0.3f),new Vector3f(117f,-0.5f,220.0f));
            gigerAlienNode.setName("giger alien");
            levelNode.attachDescendant(gigerAlienNode);
            
            Node copNode=NodeFactory.getInstance().getNode("/jbin/cop.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.5f,0.5f,0.5f),new Vector3f(116.0f,0.0f,220.0f));
            copNode.setName("cop");
            levelNode.attachDescendant(copNode);
            
            Node agentNode=NodeFactory.getInstance().getNode("/jbin/agent.jbin",new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f),new Vector3f(0.018f,0.018f,0.018f),new Vector3f(118.0f,-0.07f,220.0f));
            agentNode.setName("agent");
            //System.out.println("world bound: "+agentNode.getWorldBound());
            URL agentTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"agent.png");
            TextureState ts=DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            ts.setEnabled(true);
            ts.setTexture(TextureManager.loadTexture(agentTextureURL,
                    Texture.MinificationFilter.BilinearNoMipMaps,
                    Texture.MagnificationFilter.Bilinear));
            agentNode.setRenderState(ts);
            levelNode.attachDescendant(agentNode);
            
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
