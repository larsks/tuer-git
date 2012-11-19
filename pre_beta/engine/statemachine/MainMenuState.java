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

import java.util.List;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.ui.text.BMText;
import com.jogamp.newt.Screen;
import com.jogamp.newt.ScreenMode;
import engine.misc.FontStore;
import engine.sound.SoundManager;

public final class MainMenuState extends ScenegraphState{
    
    
    private final NativeCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    private final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel startMenuPanel;
    
    private final UIPanel optionsMenuPanel;
    
    private final UIPanel displaySettingsMenuPanel;
    
    private final UIPanel soundSettingsMenuPanel;
    
    private final UIPanel desktopShortcutsMenuPanel;
    
    private final UIPanel controlsPanel;
    
    private final UIPanel creditsPanel;
    
    private final UIPanel loadGamePanel;
    
    private final UIPanel newGamePanel;
    
    private final Runnable launchRunnable;
    
    private final Runnable uninstallRunnable;
    
    /**
     * Constructor
     * 
     * @param canvas canvas used to display the menu
     * @param physicalLayer physical layer for triggers
     * @param mouseManager mouse manager
     * @param exitAction action used when exit the menu
     * @param toLoadingDisplayAction action used to switch to the loading display
     * @param soundManager sound manager
     * @param launchRunnable runnable used to create a desktop shortcut to launch the game (may be null)
     * @param uninstallRunnable runnable used to create a desktop shortcut to uninstall the game (may be null)
     * @param creditsContent credits content (may be null)
     * @param controlsContent controls content (may be null)
     * @param fontStore store that contains fonts
     * @param toggleScreenModeAction action allowing to modify the windowing mode
     */
    public MainMenuState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,
                  final MouseManager mouseManager,
                  final TriggerAction exitAction,final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction,
                  final SoundManager soundManager,final Runnable launchRunnable,
                  final Runnable uninstallRunnable,final String creditsContent,final String controlsContent,
      			  final FontStore fontStore,final TriggerAction toggleScreenModeAction){
        super(soundManager);
        this.launchRunnable=launchRunnable;
        this.uninstallRunnable=uninstallRunnable;
        this.canvas=canvas;
        this.physicalLayer=physicalLayer;
        this.mouseManager=mouseManager;
        //creates the panels
        if(controlsContent!=null)
            controlsPanel=createControlsPanel(controlsContent);
        else
        	controlsPanel=null;
        if(creditsContent!=null)
            creditsPanel=createCreditsPanel(creditsContent);
        else
        	creditsPanel=null;
        displaySettingsMenuPanel=createDisplaySettingsMenuPanel(toggleScreenModeAction);
        soundSettingsMenuPanel=createSoundSettingsMenuPanel(soundManager);
        desktopShortcutsMenuPanel=createDesktopShortcutsMenuPanel();
        initialMenuPanel=createInitialMenuPanel(exitAction);
        optionsMenuPanel=createOptionsMenuPanel();
        startMenuPanel=createStartMenuPanel(toLoadingDisplayAction);
        loadGamePanel=createLoadGamePanel(toLoadingDisplayAction);
        newGamePanel=createNewGamePanel(toLoadingDisplayAction);
        //creates the main frame
        mainFrame=createMainFrame();
        //creates the head-up display
        final UIHud hud=createHud();        
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        //adds some text
        final BMText textNode=new BMText("gameTitleNode","Truly Unusual Experience of Revolution",fontStore.getFontsList().get(1),BMText.Align.Center,BMText.Justify.Center);
        textNode.setFontScale(2);
        textNode.setTextColor(ColorRGBA.RED);
        textNode.setTranslation(textNode.getTranslation().add(0,3.3,0,null));
        getRoot().attachChild(textNode);
        //setups the keyboard triggers
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    private static final class LevelTransitionTriggerAction extends TransitionTriggerAction<ScenegraphState,String>{
    	
    	private final int levelIndex;
    	
    	private LevelTransitionTriggerAction(final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction,
    			final int levelIndex){
    		super(toLoadingDisplayAction.stateMachine,toLoadingDisplayAction.event,toLoadingDisplayAction.renderContext);
    		this.levelIndex=levelIndex;
    	}
    	
    	@Override
    	protected void doFireEvent(){
    		super.doFireEvent();
    		LoadingDisplayState loadingDisplayState=(LoadingDisplayState)stateMachine.getCurrentState();
    		loadingDisplayState.getLevelInitializationTask().setLevelIndex(levelIndex);
    	}
    }
    
    private final UIPanel createNewGamePanel(final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction){
        final UIPanel newGamePanel=new UIPanel(new RowLayout(false));
        final UIButton level0Button=new UIButton("Level 0");
        level0Button.addActionListener(new ActionListener(){
        	
        	private final LevelTransitionTriggerAction levelTransitionTriggerAction=new LevelTransitionTriggerAction(toLoadingDisplayAction,0);
        	
            @Override
            public void actionPerformed(ActionEvent event){
            	levelTransitionTriggerAction.perform(null,null,-1);
            }
        });
        final UIButton perfTestButton=new UIButton("Performance Test");
        perfTestButton.addActionListener(new ActionListener(){
        	
        	private final LevelTransitionTriggerAction levelTransitionTriggerAction=new LevelTransitionTriggerAction(toLoadingDisplayAction,1);
        	
            @Override
            public void actionPerformed(ActionEvent event){
            	levelTransitionTriggerAction.perform(null,null,-1);
            }
        });
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        newGamePanel.add(level0Button);
        newGamePanel.add(perfTestButton);
        newGamePanel.add(backButton);
        return(newGamePanel);
    }
    
    private final UIPanel createLoadGamePanel(final TriggerAction toLoadingDisplayAction){
        final UIPanel loadGamePanel=new UIPanel(new RowLayout(false));
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        loadGamePanel.add(new UILabel("Feature not yet implemented!"));
        loadGamePanel.add(backButton);
        return(loadGamePanel);
    }

    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  //shows the initial menu
                  showPanelInMainFrame(initialMenuPanel);
                 }
             else
                 mouseManager.setGrabbed(GrabbedState.GRABBED);
            }
    }
    
    private final UIPanel createInitialMenuPanel(final TriggerAction exitAction){
        final UIPanel initialMenuPanel=new UIPanel(new RowLayout(false));
        initialMenuPanel.setForegroundColor(ColorRGBA.DARK_GRAY);
        final UIButton startButton=new UIButton("Start");
        startButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        final UIButton optionsButton=new UIButton("Options");
        optionsButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        final UIButton exitButton=new UIButton("Exit");
        exitButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                exitAction.perform(null,null,-1);
            }
        });      
        initialMenuPanel.add(startButton);
        initialMenuPanel.add(optionsButton);
        initialMenuPanel.add(exitButton);
        return(initialMenuPanel);
    }
    
    private final UIPanel createOptionsMenuPanel(){
    	final UIPanel optionsMenuPanel=new UIPanel(new RowLayout(false));
    	final UIButton desktopShortcutsButton;
    	if(desktopShortcutsMenuPanel!=null)
    	    {desktopShortcutsButton=new UIButton("Shortcuts");
    	     desktopShortcutsButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event) {
					showPanelInMainFrame(desktopShortcutsMenuPanel);
				}
			 });
    	    }
    	else
    		desktopShortcutsButton=null;
    	final UIButton displaySettingsButton;
    	if(displaySettingsMenuPanel!=null)
    	    {displaySettingsButton=new UIButton("Display");
    	     displaySettingsButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(displaySettingsMenuPanel);
                 }
             });
    	    }
    	else
    		displaySettingsButton=null;
    	final UIButton soundSettingsButton;
    	if(soundSettingsMenuPanel!=null)
	        {soundSettingsButton=new UIButton("Sound");
	         soundSettingsButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(soundSettingsMenuPanel);
                 }
             });
	        }
	    else
	    	soundSettingsButton=null;
    	final UIButton controlsButton;
        if(controlsPanel!=null)
            {controlsButton=new UIButton("Controls");
             controlsButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(controlsPanel);
                 }
             });
            }
        else
        	controlsButton=null;
        final UIButton creditsButton;
        if(creditsPanel!=null)
            {creditsButton=new UIButton("Credits");
             creditsButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(creditsPanel);
                 }
             });
            }
        else
        	creditsButton=null;
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(initialMenuPanel);
            }
        });
        if(displaySettingsButton!=null)
        	optionsMenuPanel.add(displaySettingsButton);
        if(soundSettingsButton!=null)
        	optionsMenuPanel.add(soundSettingsButton);
        if(desktopShortcutsButton!=null)
        	optionsMenuPanel.add(desktopShortcutsButton);
        if(controlsButton!=null)
            optionsMenuPanel.add(controlsButton);
        if(creditsButton!=null)
        	optionsMenuPanel.add(creditsButton);
        optionsMenuPanel.add(backButton);
    	return(optionsMenuPanel);
    }
    
    private final UIPanel createDisplaySettingsMenuPanel(final TriggerAction toggleScreenModeAction){
    	final UIPanel displaySettingsMenuPanel=new UIPanel(new RowLayout(false));
    	final UILabel screenModesLabel=new UILabel("Display Mode");
    	final Screen screen=((JoglNewtWindow)canvas).getNewtWindow().getScreen();
    	final ScreenMode currentScreenMode=screen.getCurrentScreenMode();
    	final List<ScreenMode> screenModes=screen.getScreenModes();
    	int selectedScreenModeIndex=-1,screenModeIndex=0;
    	for(ScreenMode screenMode:screenModes)
    		{if(screenMode==currentScreenMode)
    	         {selectedScreenModeIndex=screenModeIndex;
    			  break;
    	         }
    		 screenModeIndex++;
    		}
    	final Object[] screenModesArray=screenModes.toArray();
    	final DefaultComboBoxModel displayModesModel=new DefaultComboBoxModel(screenModesArray);
    	final UIComboBox displayModesCombo=new UIComboBox(displayModesModel);
    	if(selectedScreenModeIndex!=-1)
    		displayModesCombo.setSelectedIndex(selectedScreenModeIndex,false);
    	displayModesCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue) {
				screen.setCurrentScreenMode((ScreenMode)newValue);
			}
		});
    	final UIButton windowingModeButton=new UIButton("Switch to windowed mode or full screen mode");
    	windowingModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	toggleScreenModeAction.perform(canvas,null,Double.NaN);
            }
        });
    	final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        displaySettingsMenuPanel.add(windowingModeButton);
        displaySettingsMenuPanel.add(screenModesLabel);
        displaySettingsMenuPanel.add(displayModesCombo);
        displaySettingsMenuPanel.add(backButton);
    	return(displaySettingsMenuPanel);
    }
    
    private static final class ToggleSoundManagerAction implements TriggerAction{
    	
    	private final SoundManager soundManager;
    	
    	private ToggleSoundManagerAction(final SoundManager soundManager){
    		this.soundManager=soundManager;
    	}
    	
    	@Override
		@MainThread
		public void perform(Canvas source,TwoInputStates inputStates,double tpf){
    		soundManager.setEnabled(!soundManager.isEnabled());
    	}
    }
    
    private final UIPanel createSoundSettingsMenuPanel(final SoundManager soundManager){
    	final UIPanel soundSettingsMenuPanel;
    	if(soundManager!=null)
    	    {final TriggerAction toggleSoundManagerAction=new ToggleSoundManagerAction(soundManager);
    		 soundSettingsMenuPanel=new UIPanel(new RowLayout(false));
    	     final UIButton toggleSoundButton=new UIButton("Switch sound on/off");
    	     toggleSoundButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                	 toggleSoundManagerAction.perform(canvas,null,Double.NaN);
                 }
             });
    	     final UIButton backButton=new UIButton("Back");
             backButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(optionsMenuPanel);
                 }
             });
             soundSettingsMenuPanel.add(toggleSoundButton);
             soundSettingsMenuPanel.add(backButton);
    	    }
    	else
    		soundSettingsMenuPanel=null;
    	return(soundSettingsMenuPanel);
    }
    
    private final UIPanel createDesktopShortcutsMenuPanel(){
    	final UIPanel desktopShortcutsMenuPanel;
    	if(launchRunnable!=null||uninstallRunnable!=null)
    	    {desktopShortcutsMenuPanel=new UIPanel(new RowLayout(false));
    	     if(launchRunnable!=null)
                 {final UIButton addDesktopShortcutButton=new UIButton("Add a desktop shortcut to launch the game");
                  addDesktopShortcutButton.addActionListener(new ActionListener(){
                      @Override
                      public void actionPerformed(ActionEvent event){
           	              launchRunnable.run();
                      }
                  });
                  desktopShortcutsMenuPanel.add(addDesktopShortcutButton);
                 }
             if(uninstallRunnable!=null)
                 {final UIButton addUninstallDesktopShortcutButton=new UIButton("Add a desktop shortcut to uninstall the game");
                  addUninstallDesktopShortcutButton.addActionListener(new ActionListener(){
                      @Override
                      public void actionPerformed(ActionEvent event){
           	              uninstallRunnable.run();
                      }
                  });
                  desktopShortcutsMenuPanel.add(addUninstallDesktopShortcutButton);
                 }
             final UIButton backButton=new UIButton("Back");
             backButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(optionsMenuPanel);
                 }
             });
             desktopShortcutsMenuPanel.add(backButton);
    	    }
    	else
    		desktopShortcutsMenuPanel=null;
    	return(desktopShortcutsMenuPanel);
    }
    
    private final UIPanel createStartMenuPanel(final TriggerAction toLoadingDisplayAction){
        final UIPanel startMenuPanel=new UIPanel(new RowLayout(false));
        final UIButton newGameButton=new UIButton("New game");
        newGameButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(newGamePanel);
            }
        });
        final UIButton loadGameButton=new UIButton("Load game");
        loadGameButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(loadGamePanel);
            }
        });
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(initialMenuPanel);
            }
        });
        startMenuPanel.add(newGameButton);
        startMenuPanel.add(loadGameButton);
        startMenuPanel.add(backButton);
        return(startMenuPanel);
    }
    
    /**
     * 
     * @param creditsContent credits content (cannot be null)
     * @return
     */
    private final UIPanel createCreditsPanel(final String creditsContent){
    	final UILabel label=new UILabel(creditsContent);
        final UIPanel textualPanel=new UIPanel(new RowLayout(false));
        textualPanel.add(label);
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        textualPanel.add(backButton);
        return(textualPanel);
    }
    
    /**
     * 
     * @param controlsContent controls content (cannot be null)
     * @return
     */
    private final UIPanel createControlsPanel(final String controlsContent){
    	final UILabel label=new UILabel(controlsContent);
        final UIPanel textualPanel=new UIPanel(new RowLayout(false));
        textualPanel.add(label);
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        textualPanel.add(backButton);
        return(textualPanel);
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
        final UIFrame mainFrame=new UIFrame("Main Menu");
        mainFrame.setUseStandin(false);
        mainFrame.setOpacity(1f);
        mainFrame.setName("Main Menu");
        mainFrame.setDecorated(false);
        return(mainFrame);
    }
    
    private final void showPanelInMainFrame(final UIPanel panel){
        mainFrame.setContentPanel(panel);
        mainFrame.updateMinimumSizeFromContents();
        mainFrame.layout();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
    }
}
