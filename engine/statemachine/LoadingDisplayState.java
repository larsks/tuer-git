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
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.ui.text.BasicText;

import engine.sound.SoundManager;
import engine.taskmanagement.TaskManagementProgressionNode;
import engine.taskmanagement.TaskManager;

public final class LoadingDisplayState extends ScenegraphState{
    
    
    private final TaskManagementProgressionNode taskNode;
    
    private Runnable levelInitializationTask;
    
    private final TaskManager taskManager;

    
    public LoadingDisplayState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toGameAction,final SoundManager soundManager,final TaskManager taskManager){
        super(soundManager);
        this.taskManager=taskManager;
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera(),taskManager);
        final Camera cam=canvas.getCanvasRenderer().getCamera();
        taskNode.setTranslation(0,-cam.getHeight()/2.5,0);
        getRoot().attachChild(taskNode);
        // execute tasks
        taskNode.addController(new SpatialController<Spatial>(){
        	
        	boolean oneSkipDone=false;
        	
        	boolean firstTaskOk=false;
        	
            @Override
            public final void update(final double time,final Spatial caller){
            	//perform the long task only at the second update to display the task node correctly
            	if(!oneSkipDone)
            		oneSkipDone=true;
            	else
            	    {if(taskManager.getTaskCount()>0)
            		     {taskManager.executeFirstTask();
            		      if(!firstTaskOk)
            		          {firstTaskOk=true;
            		           //the first task is used to enqueue all loading tasks
            		           taskNode.reset();
            		          }
            		     }
            	     else
                         toGameAction.perform(null,null,-1);
            	    }
            }
        });
        //FIXME: add a mechanism to update the text
        final BasicText levelTextLabel=BasicText.createDefaultTextLabel("Level","Level 0: The museum");
        levelTextLabel.setTranslation(cam.getWidth()/2,cam.getHeight()/2,0);
        getRoot().attachChild(levelTextLabel);
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
                 {taskManager.enqueueTask(levelInitializationTask);
                  taskNode.reset();
                 }
            }
    }
}
