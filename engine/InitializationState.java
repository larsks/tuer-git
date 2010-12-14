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

import java.util.LinkedHashMap;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import engine.service.Ardor3DGameServiceProvider.Step;
import engine.sound.SoundManager;

public final class InitializationState extends State{   
  
    
    private final Box box;
    
    private final TaskManagementProgressionNode taskNode;
    
    
    public InitializationState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toIntroAction,final SoundManager soundManager){
        super(soundManager);
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera());
        taskNode.setTranslation(0,-canvas.getCanvasRenderer().getCamera().getHeight()/2.5,0);
        box=new Box(Step.INITIALIZATION.toString()+"Box",Vector3.ZERO,5,5,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-15));       
        LinkedHashMap<Double,Double> timeWindowsTable=new LinkedHashMap<Double,Double>();
        // the rotation lasts 6 seconds
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(10));
        // set it to rotate
        box.addController(new UniformlyVariableRotationController(0,25,0,new Vector3(0,1,0),timeWindowsTable));
        // execute tasks
        box.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(final double time,final Spatial caller){
                TaskManager.getInstance().executeFirstTask();
            }
        });
        getRoot().attachChild(box);
        getRoot().attachChild(taskNode);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger returnTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN),toIntroAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger,returnTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    
    @Override
    public final void init(){
        // puts a texture onto the box
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(Step.INITIALIZATION.toString().toLowerCase()+".png",Texture.MinificationFilter.Trilinear,true));
        box.setRenderState(ts);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 taskNode.reset();
            }
    }
}
