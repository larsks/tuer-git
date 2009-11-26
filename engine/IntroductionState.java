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
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.TextureManager;

import engine.Ardor3DGameServiceProvider.Step;

public final class IntroductionState extends State{
    
    
    private static final String soundSamplePath="/sounds/introduction.ogg";
    
    private static final String textureFilename="introduction.png";
    
    private String sourcename;
    
    private final Box box;

    
    public IntroductionState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final TriggerAction toMainMenuAction){
        super();
        box=new Box(Step.INTRODUCTION.toString()+"Box",Vector3.ZERO,12,9,5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0,0,-75));
        //configure the spread effect
        final Point spreadCenter=new Point(205,265);     
        HashMap<Color,Color> colorSubstitutionTable=new HashMap<Color,Color>();
        colorSubstitutionTable.put(Color.BLUE,Color.RED);
        MovementEquation equation=new UniformlyVariableMovementEquation(0,10000,0);
        //set a controller that modifies the image
        box.addController(new CircularSpreadTextureUpdaterController(Step.INTRODUCTION.toString().toLowerCase()+".png",equation,colorSubstitutionTable,spreadCenter,canvas.getCanvasRenderer().getRenderer(),canvas.getCanvasRenderer().getRenderContext()));
        //set a controller that moves the image
        LinkedHashMap<Double,Double> timeWindowsTable=new LinkedHashMap<Double,Double>();
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(6));
        box.addController(new UniformlyVariableRectilinearTranslationController(0,10,-75,new Vector3(0,0,1),timeWindowsTable));       
        getRoot().attachChild(box);       
        // show the game title as text
        final BMText textNode=new BMText("gameTitleNode","TUER",Ardor3DGameServiceProvider.getFontsList().get(1),BMText.Align.Center,BMText.Justify.Center);
        textNode.setFontScale(6);
        textNode.setTextColor(ColorRGBA.BLACK);
        textNode.setTranslation(0,0,-75);
        timeWindowsTable=new LinkedHashMap<Double,Double>();
        timeWindowsTable.put(Double.valueOf(0),Double.valueOf(8));
        textNode.addController(new UniformlyVariableRectilinearTranslationController(0,10,-75,new Vector3(0,0,1),timeWindowsTable));
        getRoot().attachChild(textNode);
        // add the triggers
        final InputTrigger toMainMenuTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN),toMainMenuAction);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger,toMainMenuTrigger};
        // bind the physical layer to the logical layer
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    @Override
    public final void init(){
        // load the music
        final URL sampleUrl=IntroductionState.class.getResource(soundSamplePath);
        if(sampleUrl!=null)
            sourcename=SoundManager.getInstance().preloadSoundSample(sampleUrl,true);
        else
            sourcename=null;
        // puts a texture onto the box
        final TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(textureFilename,Texture.MinificationFilter.Trilinear,Format.GuessNoCompression,true));
        box.setRenderState(ts);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {if(sourcename!=null)
                      SoundManager.getInstance().play(sourcename);
                 }
             else
                 {if(sourcename!=null)
                      SoundManager.getInstance().stop(sourcename);
                 }
            }
    }
}