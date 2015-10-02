/**
 * Copyright (c) 2006-2015 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.statemachine;

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
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import engine.movement.UniformlyVariableRotationController;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManagementProgressionNode;
import engine.taskmanagement.TaskManager;

public final class InitializationState extends ScenegraphState{   
  
    
    private final Box box;
    
    private final TaskManagementProgressionNode taskNode;
    
    private final Camera cam;
    
    
    public InitializationState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction,final TriggerAction toIntroAction,final SoundManager soundManager,final TaskManager taskManager){
        super(soundManager);
        taskNode=new TaskManagementProgressionNode(canvas.getCanvasRenderer().getCamera(),taskManager);
        cam=canvas.getCanvasRenderer().getCamera();
        box=new Box("Initialization Box",Vector3.ZERO,5,5,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-15));       
        LinkedHashMap<Double,Double> timeWindowsTable=new LinkedHashMap<>();
        // the rotation lasts 6 seconds
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(10));
        // set it to rotate
        box.addController(new UniformlyVariableRotationController(0,25,0,new Vector3(0,1,0),timeWindowsTable));
        // execute tasks
        box.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(final double time,final Spatial caller){
                taskManager.executeFirstTask();
            }
        });
        getRoot().attachChild(box);
        getRoot().attachChild(taskNode);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),toExitGameTriggerAction);
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
        ts.setTexture(TextureManager.load("initialization.png",Texture.MinificationFilter.Trilinear,true));
        box.setRenderState(ts);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {taskNode.reset();
                  //updates the position of the task node (the resolution might have been modified)
                  final int x=(cam.getWidth()-taskNode.getBounds().getWidth())/2;
                  final int y=(cam.getHeight()/20);
                  taskNode.setTranslation(x,y,0);
                  taskNode.updateGeometricState(0);
                 }
            }
    }
}
