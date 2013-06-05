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
import com.ardor3d.extension.ui.UILabel;
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
import engine.sound.SoundManager;

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
    
    private final TransitionTriggerAction<ScenegraphState,String> toGameOverTriggerAction;
    
    private final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerAction;
    
    private final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel confirmAbortMenuPanel;
    
    private final UIPanel confirmExitMenuPanel;
    
    private boolean openedForExitConfirm=false;
	
	public PauseMenuState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final MouseManager mouseManager,
			              final TransitionTriggerAction<ScenegraphState,String> toGameTriggerAction,
			              final TransitionTriggerAction<ScenegraphState,String> toGameOverTriggerAction,
			              final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerAction,final SoundManager soundManager){
		super(soundManager);
		this.canvas=canvas;
		this.physicalLayer=physicalLayer;
		this.mouseManager=mouseManager;
		this.toGameTriggerAction=toGameTriggerAction;
		this.toGameOverTriggerAction=toGameOverTriggerAction;
		this.toUnloadingDisplayTriggerAction=toUnloadingDisplayTriggerAction;
		initialMenuPanel=createInitialMenuPanel();
		confirmAbortMenuPanel=createConfirmAbortMenuPanel();
		confirmExitMenuPanel=createConfirmExitMenuPanel();
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
		//disables it temporarily until this feature really works
		abortButton.setEnabled(false);
		abortButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onAbortButtonActionPerformed(ae);
            }
        });
		final UIButton exitButton=new UIButton("Exit");
		exitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onExitButtonActionPerformed(ae);
            }
        });
		initialMenuPanel.add(resumeButton);
		initialMenuPanel.add(abortButton);
		initialMenuPanel.add(exitButton);
		return(initialMenuPanel);
	}
	
	private final UIPanel createConfirmAbortMenuPanel(){
		final UIPanel confirmAbortMenuPanel=new UIPanel(new RowLayout(false));
		final UILabel confirmLabel=new UILabel("Confirm abort?");
		final UIButton yesButton=new UIButton("Yes");
		yesButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onYesAbortButtonActionPerformed(ae);
            }
        });
		final UIButton noButton=new UIButton("No");
		noButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onNoAbortButtonActionPerformed(ae);
            }
        });
		confirmAbortMenuPanel.add(confirmLabel);
		confirmAbortMenuPanel.add(yesButton);
		confirmAbortMenuPanel.add(noButton);
		return(confirmAbortMenuPanel);
	}
	
	private final UIPanel createConfirmExitMenuPanel(){
		final UIPanel confirmExitMenuPanel=new UIPanel(new RowLayout(false));
		final UILabel confirmLabel=new UILabel("Confirm exit?");
		final UIButton yesButton=new UIButton("Yes");
		yesButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onYesExitButtonActionPerformed(ae);
            }
        });
		final UIButton noButton=new UIButton("No");
		noButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onNoExitButtonActionPerformed(ae);
            }
        });
		confirmExitMenuPanel.add(confirmLabel);
		confirmExitMenuPanel.add(yesButton);
		confirmExitMenuPanel.add(noButton);
		return(confirmExitMenuPanel);
	}
	
	private void onResumeButtonActionPerformed(final ActionEvent ae){
		toGameTriggerAction.perform(null,null,-1);
    }
	
	private void onAbortButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(confirmAbortMenuPanel);
    }
	
	private void onExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(confirmExitMenuPanel);
    }
	
	private void onYesAbortButtonActionPerformed(final ActionEvent ae){
		toGameOverTriggerAction.perform(null,null,-1);
	}
	
	private void onNoAbortButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(initialMenuPanel);
	}
	
	private void onYesExitButtonActionPerformed(final ActionEvent ae){
		toUnloadingDisplayTriggerAction.perform(null,null,-1);
	}
	
	private void onNoExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(initialMenuPanel);
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
    
    public void setOpenedForExitConfirm(final boolean openedForExitConfirm){
    	this.openedForExitConfirm=openedForExitConfirm;
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  //if the end user arrives here after pressing ESC, it shows the exit confirm panel
                  if(openedForExitConfirm)
                      {showPanelInMainFrame(confirmExitMenuPanel);
                	   openedForExitConfirm=false;
                      }
                  else
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
