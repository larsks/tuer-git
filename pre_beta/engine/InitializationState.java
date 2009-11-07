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
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import engine.Ardor3DGameServiceProvider.Step;

final class InitializationState extends State{
  
    
    InitializationState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction){
        super();
        final Box box=new Box(Step.INITIALIZATION.toString()+"Box",Vector3.ZERO,5,5,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-15));       
        LinkedHashMap<Double,Double> timeWindowsTable=new LinkedHashMap<Double,Double>();
        // the rotation lasts 6 seconds
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(10));
        // set it to rotate
        box.addController(new UniformlyVariableRotationController(0,25,0,new Vector3(0,1,0),timeWindowsTable));
        // puts a texture onto it
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(Step.INITIALIZATION.toString().toLowerCase()+".png",Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression,true));
        box.setRenderState(ts);
        getRoot().attachChild(box);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
}
