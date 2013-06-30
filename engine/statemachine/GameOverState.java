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
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.ui.text.BMText;

import engine.misc.FontStore;
import engine.sound.SoundManager;

/**
 * State representing the end of a game, caused by the death of the player, his victory or his abandonment
 * 
 * @author Julien Gouesse
 *
 */
public class GameOverState extends ScenegraphState{
	
private final NativeCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    private final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerActionForExit;
    
    private final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerActionForMainMenu;
    
    private final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel confirmExitMenuPanel;

	public GameOverState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final MouseManager mouseManager,final SoundManager soundManager,
			final FontStore fontStore,
			final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerActionForExit,
			final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerActionForMainMenu,
			final TransitionTriggerAction<ScenegraphState,String> toUnloadingDisplayTriggerActionForLoadingDisplay){
		super(soundManager);
		this.canvas=canvas;
		this.physicalLayer=physicalLayer;
		this.mouseManager=mouseManager;
		this.toUnloadingDisplayTriggerActionForExit=toUnloadingDisplayTriggerActionForExit;
		this.toUnloadingDisplayTriggerActionForMainMenu=toUnloadingDisplayTriggerActionForMainMenu;
		initialMenuPanel=createInitialMenuPanel();
		confirmExitMenuPanel=createConfirmExitMenuPanel();
		//creates the main frame
        mainFrame=createMainFrame();
        //creates the head-up display
        final UIHud hud=createHud();
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        //adds some text
        final BMText textNode=new BMText("gameOverNode","Game over",fontStore.getFontsList().get(1),BMText.Align.Center,BMText.Justify.Center);
        textNode.setFontScale(10);
        textNode.setTextColor(ColorRGBA.RED);
        textNode.setTranslation(textNode.getTranslation().add(0,3.3,0,null));
        getRoot().attachChild(textNode);
	}
	
	private final UIPanel createInitialMenuPanel(){
		final UIPanel initialMenuPanel=new UIPanel(new RowLayout(false));
		final UIButton nextButton=new UIButton("Next");
		nextButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onNextButtonActionPerformed(ae);
            }
        });
		final UIButton retryButton=new UIButton("Retry");
		retryButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onRetryButtonActionPerformed(ae);
            }
        });
		final UIButton mainMenuButton=new UIButton("Main Menu");
		mainMenuButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onMainMenuButtonActionPerformed(ae);
            }
        });
		final UIButton exitButton=new UIButton("Exit");
		exitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onExitButtonActionPerformed(ae);
            }
        });
		//disables them temporarily until these features really work
		nextButton.setEnabled(false);
		retryButton.setEnabled(false);
		initialMenuPanel.add(nextButton);
		initialMenuPanel.add(retryButton);
		initialMenuPanel.add(mainMenuButton);
		initialMenuPanel.add(exitButton);
		return(initialMenuPanel);
	}
	
	private void onNextButtonActionPerformed(final ActionEvent ae){
		//TODO
    }
	
	private void onRetryButtonActionPerformed(final ActionEvent ae){
		//TODO
    }
	
	private void onMainMenuButtonActionPerformed(final ActionEvent ae){
		toUnloadingDisplayTriggerActionForMainMenu.perform(null,null,-1);
    }
	
	private void onExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(confirmExitMenuPanel);
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
	
	private void onYesExitButtonActionPerformed(final ActionEvent ae){
		toUnloadingDisplayTriggerActionForExit.perform(null,null,-1);
	}
	
	private void onNoExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(initialMenuPanel);
	}
	
	private final UIFrame createMainFrame(){
        final UIFrame mainFrame=new UIFrame("Game Over");
        mainFrame.setUseStandin(false);
        mainFrame.setOpacity(1f);
        mainFrame.setName("Pause Menu");
        mainFrame.setDecorated(false);
        return(mainFrame);
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
	
	@Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  //TODO update the available items depending on the previous state and the figures
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
