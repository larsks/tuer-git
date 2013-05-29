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

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;

/**
 * State for the in-game pause menu
 * 
 * @author Julien Gouesse
 *
 */
public class PauseMenuState extends ScenegraphState{
	
	private final NativeCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    private final TransitionTriggerAction<ScenegraphState,String> toGameTriggerAction;
    
    private final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
	
	public PauseMenuState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final MouseManager mouseManager,
			              final TransitionTriggerAction<ScenegraphState,String> toGameTriggerAction){
		super();
		this.canvas=canvas;
		this.physicalLayer=physicalLayer;
		this.mouseManager=mouseManager;
		this.toGameTriggerAction=toGameTriggerAction;
		initialMenuPanel=createInitialMenuPanel();
		//creates the main frame
        mainFrame=createMainFrame();
        //creates the head-up display
        final UIHud hud=createHud();
        hud.add(mainFrame);
        getRoot().attachChild(hud);
	}
	
	private final UIPanel createInitialMenuPanel(){
		final UIPanel initialMenuPanel=new UIPanel(new RowLayout(false));
		final UIButton resumeButton=new UIButton("Resume");
		resumeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onResumeButtonActionPerformed(ae);
            }
        });
		final UIButton abortButton=new UIButton("Abort");
		abortButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	//TODO
            }
        });
		final UIButton exitButton=new UIButton("Exit");
		exitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	//TODO
            }
        });
		initialMenuPanel.add(resumeButton);
		initialMenuPanel.add(abortButton);
		initialMenuPanel.add(exitButton);
		return(initialMenuPanel);
	}
	
	private void onResumeButtonActionPerformed(final ActionEvent ae){
		toGameTriggerAction.perform(null,null,-1);
    }
	
	private final UIHud createHud(){
        final UIHud hud=new UIHud();
        hud.setupInput(canvas,physicalLayer,getLogicalLayer());        
        getRoot().addController(new SpatialController<Node>(){
            @Override
            public final void update(final double time,final Node caller){               
                //updates the triggers of the hud
                hud.getLogicalLayer().checkTriggers(time);
            }
        });
        return(hud);
    }
    
    private final UIFrame createMainFrame(){
        final UIFrame mainFrame=new UIFrame("Pause Menu");
        mainFrame.setUseStandin(false);
        mainFrame.setOpacity(1f);
        mainFrame.setName("Pause Menu");
        mainFrame.setDecorated(false);
        return(mainFrame);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  //FIXME if the end user arrives here after pressing ESC, rather show the exit confirm panel
                  //shows the initial menu
                  showPanelInMainFrame(initialMenuPanel);
                 }
             else
                 mouseManager.setGrabbed(GrabbedState.GRABBED);
            }
    }
    
    final void showPanelInMainFrame(final UIPanel panel){
        mainFrame.setContentPanel(panel);
        mainFrame.updateMinimumSizeFromContents();
        mainFrame.layout();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
    }
}
