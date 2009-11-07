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

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedHashMap;
import sound.Sample;
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

final class IntroductionState extends State{
    
    
    private Sample music;

    
    IntroductionState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toMainMenuAction){
        super();
        final Box box=new Box(Step.INTRODUCTION.toString()+"Box",Vector3.ZERO,12,9,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-75));       
        // puts a texture onto it
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(Step.INTRODUCTION.toString().toLowerCase()+".png",Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression,true));
        box.setRenderState(ts);        
        //configure the spread effect
        final Point spreadCenter=new Point(205,265);     
        HashMap<Color,Color> colorSubstitutionTable=new HashMap<Color,Color>();
        colorSubstitutionTable.put(Color.BLUE,Color.RED);
        MovementEquation equation=new UniformlyVariableMovementEquation(0,10000,0);
        //set a controller that modifies the image
        box.addController(new CircularSpreadTextureUpdaterController(Step.INTRODUCTION.toString().toLowerCase()+".png",equation,colorSubstitutionTable,spreadCenter,canvas.getCanvasRenderer().getRenderer()));
        //set a controller that moves the image
        LinkedHashMap<Double,Double> timeWindowsTable=new LinkedHashMap<Double,Double>();
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(6));
        box.addController(new UniformlyVariableRectilinearTranslationController(0,10,-75,new Vector3(0,0,1),timeWindowsTable));       
        getRoot().attachChild(box);
        final InputTrigger toMainMenuTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN),toMainMenuAction);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger,toMainMenuTrigger};
        // bind the physical layer to the logical layer
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
        // load the music
        try{music=new Sample(getClass().getResource("/sounds/internationale.ogg"));
            music.open();
           }
        catch(Exception e)
        {e.printStackTrace();}
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 music.play();
             else
                 music.stop();
            }
    }
}
