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

import java.util.concurrent.Callable;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.GameTaskQueueManager;

final class MainMenuState extends State{
    
    
    private final UIButton[] buttons;
    
    private final AwtMouseManager awtMouseManager;
    
    
    MainMenuState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,
                  final AwtMouseManager awtMouseManager,
                  final TriggerAction exitAction,final TriggerAction toLoadingDisplayAction){
        super();
        this.awtMouseManager=awtMouseManager;
        final UIFrame frame=new UIFrame("Main Menu");
        final UIPanel panel=new UIPanel(new RowLayout(false));
        panel.setForegroundColor(ColorRGBA.DARK_GRAY);
        final UIButton startButton=new UIButton("Start");
        final UIButton creditsButton=new UIButton("Credits");
        final UIButton exitButton=new UIButton("Exit");
        exitButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                exitAction.perform(canvas,null,-1);
            }
        });
        buttons=new UIButton[]{startButton,creditsButton,exitButton};
        for(UIButton button:buttons)
            panel.add(button);
        frame.setContentPanel(panel);
        frame.updateMinimumSizeFromContents();
        frame.layout();
        frame.pack();
        frame.setUseStandin(false);
        frame.setOpacity(1f);
        frame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
        frame.setName("Main Menu");
        frame.setDecorated(false);
        final UIHud hud=new UIHud();
        hud.setupInput(canvas,physicalLayer,getLogicalLayer());
        hud.add(frame);
        getRoot().addController(new SpatialController<Node>(){
            @Override
            public final void update(final double time,final Node caller){               
                hud.getLogicalLayer().checkTriggers(time);
                GameTaskQueueManager.getManager().render(new Callable<Void>(){
                    @Override
                    public Void call() throws Exception{
                        canvas.getCanvasRenderer().getRenderer().draw(hud);
                        return(null);
                    }
                });
            }
        });
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);       
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 awtMouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
             else
                 awtMouseManager.setGrabbed(GrabbedState.GRABBED);
            }
    }
}
