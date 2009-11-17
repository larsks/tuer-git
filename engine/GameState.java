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
