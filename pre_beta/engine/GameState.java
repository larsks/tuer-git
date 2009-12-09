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
package engine;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.export.binary.BinaryImporter;
import engine.input.ExtendedFirstPersonControl;

final class GameState extends State{
    
    
    private int levelIndex;
    
    private final NativeCanvas canvas;
    
    private double previousFrustumNear;
    
    private double previousFrustumFar;
    
    private final Vector3 previousCamLocation;
    
    private final Vector3 currentCamLocation;
    
    private final CameraNode playerNode;
    
    private final BasicText fpsTextLabel;

    
    GameState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction){
        super();
        this.canvas=canvas;
        Camera cam=canvas.getCanvasRenderer().getCamera();
        this.previousCamLocation=new Vector3(cam.getLocation());
        this.currentCamLocation=new Vector3();
        final Vector3 worldUp=new Vector3(0,1,0);              
        // drag only at false to remove the need of pressing a button to move
        ExtendedFirstPersonControl fpsc=ExtendedFirstPersonControl.setupTriggers(getLogicalLayer(),worldUp,false);
        fpsc.setMoveSpeed(fpsc.getMoveSpeed()/10);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
        fpsTextLabel=BasicText.createDefaultTextLabel("FPS display","");
        fpsTextLabel.setTranslation(new Vector3(0,20,0));
        fpsTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
                fpsTextLabel.setText(Math.round(time>0?1/time:0)+" FPS");
            }           
        });
        // configure the collision system
        /*CollisionTreeManager.getInstance().setTreeType(CollisionTree.Type.AABB);
        collisionResults=new BoundingCollisionResults();
        PickingUtil.findCollisions(spatial,scene,collisionResults);
        for(int i=0;i<collisionResults.getNumber();i++)
            {collisionResults.getCollisionData(i);
             //handle the collision
            }
        collisionResults.clear();*/
        // create a node that follows the camera
        playerNode=new CameraNode("player",cam);
        playerNode.addController(new SpatialController<Spatial>(){
            @Override
            public void update(double timeSinceLastCall, Spatial caller) {
                // sync the camera node with the camera
                playerNode.updateFromCamera();
            }           
        });
    }
    
    
    final void setLevelIndex(final int levelIndex){
        this.levelIndex=levelIndex;
    }
    
    @Override
    public final void init(){
        // Remove all previously attached children
        getRoot().detachAllChildren();
        //FIXME: it should not be hard-coded
        currentCamLocation.set(115,0,223);
        //attach the player itself
        getRoot().attachChild(playerNode);
        //attach the FPS display node
        getRoot().attachChild(fpsTextLabel);
        // Load level model
        try {final Node levelNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/LID"+levelIndex+".abin"));
             CullState cullState=new CullState();
             cullState.setEnabled(true);
             cullState.setCullFace(CullState.Face.Back);
             levelNode.setRenderState(cullState);
             getRoot().attachChild(levelNode);
             
             final Node uziNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/uzi.abin"));
             uziNode.setTranslation(111.5,0,219);
             uziNode.setScale(0.2);           
             getRoot().attachChild(uziNode);
             final Node smachNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/smach.abin"));
             smachNode.setTranslation(112.5,0,219);
             smachNode.setScale(0.2);
             getRoot().attachChild(smachNode);
             final Node pistolNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol.abin"));
             pistolNode.setTranslation(113.5,0,219);
             pistolNode.setScale(0.001);
             pistolNode.setRotation(new Quaternion().fromEulerAngles(Math.PI/2,-Math.PI/4,Math.PI/2));
             getRoot().attachChild(pistolNode);
             final Node pistol2Node=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol2.abin"));
             //remove the bullet as it is not necessary now
             ((Node)pistol2Node.getChild(0)).detachChildAt(2);
             pistol2Node.setTranslation(114.5,0,219);
             pistol2Node.setScale(0.02);
             pistol2Node.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
             getRoot().attachChild(pistol2Node);
             final Node pistol3Node=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol3.abin"));
             pistol3Node.setTranslation(115.5,0,219);
             pistol3Node.setScale(0.02);
             getRoot().attachChild(pistol3Node);
             final Node laserNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/laser.abin"));
             laserNode.setTranslation(116.5,0,219);
             laserNode.setScale(0.02);
             getRoot().attachChild(laserNode);
             final Node copNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/cop.abin"));
             copNode.setTranslation(117.5,0,219);
             copNode.setScale(0.5);
             copNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(0,1,0)));
             getRoot().attachChild(copNode);
             final Node alienNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/giger_alien.abin"));
             alienNode.setTranslation(118.5,-0.5,219);
             alienNode.setScale(0.3);
             getRoot().attachChild(alienNode);
            }
        catch(final Exception ex)
        {ex.printStackTrace();}
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             final Camera cam=canvas.getCanvasRenderer().getCamera();
             if(enabled)
                 {previousFrustumNear=cam.getFrustumNear();
                  previousFrustumFar=cam.getFrustumFar();
                  previousCamLocation.set(cam.getLocation());
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),0.3,300);
                  cam.setLocation(currentCamLocation);
                 }
             else
                 {currentCamLocation.set(cam.getLocation());                  
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),previousFrustumNear,previousFrustumFar);
                  cam.setLocation(previousCamLocation);
                 }
            }
    }
}
