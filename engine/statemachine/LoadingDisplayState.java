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

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.TextureManager;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManagementProgressionNode;
import engine.taskmanagement.TaskManager;

public final class LoadingDisplayState extends ScenegraphState{
    
    
    private final TaskManagementProgressionNode taskNode;
    
    private GameStateInitializationRunnable levelInitializationTask;
    
    private final TaskManager taskManager;
    
    private final Box box;
    
    private final Texture[] textures;
    
    private final String[] texturesPaths;
    
    private final BMText levelTextLabel;

    
    public LoadingDisplayState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toGameAction,final SoundManager soundManager,final TaskManager taskManager){
        super(soundManager);
        this.taskManager=taskManager;
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera(),taskManager);
        final Camera cam=canvas.getCanvasRenderer().getCamera();
        taskNode.setTranslation(0,-cam.getHeight()/2.5,0);
        getRoot().attachChild(taskNode);
        texturesPaths=new String[]{"communism.png","venimus_vidimus_vicimus.png"};
        textures=new Texture[texturesPaths.length];
        // execute tasks
        taskNode.addController(new SpatialController<Spatial>(){
        	
        	private boolean oneSkipDone=false;
        	
        	private boolean firstTaskOk=false;
        	
        	private int textureIndex=0;
        	
        	private int taskCount=0;
        	
            @Override
            public final void update(final double time,final Spatial caller){
            	//updates the texture
       	        final TextureState textureState=(TextureState)box.getLocalRenderState(StateType.Texture);
       	        textureState.setTexture(textures[textureIndex]);
            	//performs the long task only at the second update to display the task node correctly
            	if(!oneSkipDone)
            		{oneSkipDone=true;
            		 levelTextLabel.setText("Level " + levelInitializationTask.getLevelIndex());
            		}
            	else
            	    {if(taskManager.getTaskCount()>0)
            		     {taskManager.executeFirstTask();
            		      if(!firstTaskOk)
            		          {firstTaskOk=true;
            		           //the first task is used to enqueue all loading tasks
            		           taskNode.reset();
            		           taskCount=taskManager.getTaskCount();
            		          }
            		      //updates the texture index
            		      if(taskManager.getTaskCount()>0)
            		    	  textureIndex=(int)Math.floor(((taskCount-taskManager.getTaskCount())/(double)taskCount)*textures.length);
            		      else
            		    	  textureIndex=textures.length-1;
            		     }
            	     else
                         {toGameAction.perform(null,null,-1);
                          oneSkipDone=false;
                          firstTaskOk=false;
                          textureIndex=0;
                         }            	    
            	    }            	
            }
        });
        box=new Box("Level Loading Display Box",Vector3.ZERO,5,5,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-15));
        getRoot().attachChild(box);
        levelTextLabel=new BMText("Level Index Text","",ScenegraphStateMachine.getFontsList().get(0),BMText.Align.Center,BMText.Justify.Center);
        levelTextLabel.setTranslation(levelTextLabel.getTranslation().add(0,3.3,0,null));
        getRoot().attachChild(levelTextLabel);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    @Override
    public final void init(){
    	//puts a texture onto the box
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        box.setRenderState(ts);
        //loads all textures displayed while loading a level
        for(int textureIndex=0;textureIndex<texturesPaths.length;textureIndex++)
        	textures[textureIndex]=TextureManager.load(texturesPaths[textureIndex],Texture.MinificationFilter.Trilinear,true);
    }
    
    public final void setLevelInitializationTask(final GameStateInitializationRunnable levelInitializationTask){
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
