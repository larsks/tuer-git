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
import com.ardor3d.util.export.binary.BinaryImporter;

import engine.input.ExtendedFirstPersonControl;

final class GameState extends State{
    
    
    private int levelIndex;
    
    private final NativeCanvas canvas;
    
    private double previousFrustumNear;
    
    private double previousFrustumFar;
    
    private final Vector3 previousCamLocation;
    
    private final Vector3 currentCamLocation;

    
    GameState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction){
        super();
        this.canvas=canvas;
        this.previousCamLocation=new Vector3(canvas.getCanvasRenderer().getCamera().getLocation());
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
        /*getRoot().addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time, Spatial caller){
                System.out.println("FPS: "+(time>0?1/time:0));
            }           
        });*/
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
             pistol2Node.setTranslation(114.5,0,219);
             pistol2Node.setScale(0.02);
             pistol2Node.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
             getRoot().attachChild(pistol2Node);
             final Node pistol3Node=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol3.abin"));
             pistol3Node.setTranslation(115.5,0,219);
             pistol3Node.setScale(0.02);
             getRoot().attachChild(pistol3Node);
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
