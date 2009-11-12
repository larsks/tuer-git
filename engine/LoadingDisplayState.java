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

import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
//import com.ardor3d.ui.text.BMText;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

final class LoadingDisplayState extends State{
    
    
    private final TaskManagementProgressionNode taskNode;
    
    private Runnable levelInitializationTask;

    
    LoadingDisplayState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toGameAction){
        super();
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera());
        taskNode.setTranslation(0,-canvas.getHeight()/2.5,0);
        /*final BMText textNode=new BMText("loadingGameNode","Feature not yet implemented!",Ardor3DGameServiceProvider.getFontsList().get(0),BMText.Align.Center,BMText.Justify.Center);
        getRoot().attachChild(textNode);*/
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
    
    final void setLevelInitializationTask(final Runnable levelInitializationTask){
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
