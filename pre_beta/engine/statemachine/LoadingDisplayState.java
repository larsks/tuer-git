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
package engine.statemachine;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
//import com.ardor3d.ui.text.BMText;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

import engine.sound.SoundManager;
import engine.taskmanagement.TaskManagementProgressionNode;
import engine.taskmanagement.TaskManager;

public final class LoadingDisplayState extends ScenegraphState{
    
    
    private final TaskManagementProgressionNode taskNode;
    
    private Runnable levelInitializationTask;

    
    public LoadingDisplayState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toGameAction,final SoundManager soundManager){
        super(soundManager);
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera());
        taskNode.setTranslation(0,-canvas.getCanvasRenderer().getCamera().getHeight()/2.5,0);
        getRoot().attachChild(taskNode);
        // execute tasks
        taskNode.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(final double time,final Spatial caller){
                TaskManager.getInstance().executeFirstTask();
                toGameAction.perform(null,null,-1);
            }
        });
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    public final void setLevelInitializationTask(final Runnable levelInitializationTask){
        this.levelInitializationTask=levelInitializationTask;
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {TaskManager.getInstance().enqueueTask(levelInitializationTask);
                  taskNode.reset();
                 }
            }
    }
}
