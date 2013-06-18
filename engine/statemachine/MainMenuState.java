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
import engine.data.common.MatchType;
import engine.data.common.MatchTypeFactory;
import engine.input.ActionMap;
import engine.input.MouseAndKeyboardSettings;
import engine.misc.FontStore;
import engine.sound.SoundManager;

public final class MainMenuState extends ScenegraphState{
	
	private final MatchTypeFactory matchTypeFactory;
    
    private static final String NO_LIMIT="No limit";
    
    private static final String DEFAULT="Default";
    
    private static final String CUSTOM="Custom";
    
    final NativeCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel startMenuPanel;
    
    final UIPanel optionsMenuPanel;
    
    private final DisplaySettingsPanel displaySettingsMenuPanel;
    
    private final UIPanel soundSettingsMenuPanel;
    
    private final UIPanel desktopShortcutsMenuPanel;
    
    private final ControlsPanel controlsPanel;
    
    private final UIPanel profilePanel;
    
    private final UIPanel creditsPanel;
    
    private final UIPanel storyModePanel;
    
    private final UIPanel arenaModePanel;
    
    private final Runnable launchRunnable;
    
    private final Runnable uninstallRunnable;
    
    private final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction;
    
    /**
     * Constructor
     * 
     * @param canvas canvas used to display the menu
     * @param physicalLayer physical layer for triggers
     * @param mouseManager mouse manager
     * @param toExitGameTriggerAction action used when exit the menu
     * @param toLoadingDisplayAction action used to switch to the loading display
     * @param soundManager sound manager
     * @param launchRunnable runnable used to create a desktop shortcut to launch the game (may be null)
     * @param uninstallRunnable runnable used to create a desktop shortcut to uninstall the game (may be null)
     * @param creditsContent credits content (may be null)
     * @param fontStore store that contains fonts
     * @param toggleScreenModeAction action allowing to modify the windowing mode
     * @param defaultActionMap default action map, which should not be modified, used to reset the custom action map to its default value
     * @param customActionMap custom action map, which can be modified
     * @param defaultMouseAndKeyboardSettings default mouse and keyboard settings, which should not be modified, used to reset the custom ones to their default values
     * @param customMouseAndKeyboardSettings custom mouse and keyboard settings, which can be modified
     */
    public MainMenuState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,
                  final MouseManager mouseManager,
                  final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction,final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction,
                  final SoundManager soundManager,final Runnable launchRunnable,
                  final Runnable uninstallRunnable,final String creditsContent,
      			  final FontStore fontStore,final TriggerAction toggleScreenModeAction,final ActionMap defaultActionMap,
      			  final ActionMap customActionMap,final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings,
      			  final MouseAndKeyboardSettings customMouseAndKeyboardSettings){
        super(soundManager);
        matchTypeFactory=new MatchTypeFactory();
        matchTypeFactory.addNewMatchType("DEATHMATCH","Deathmatch");
        matchTypeFactory.addNewMatchType("CAPTURE_THE_FLAG","Capture the flag");
        matchTypeFactory.addNewMatchType("HOLD_THE_BAG","Hold the bag");
        this.launchRunnable=launchRunnable;
        this.uninstallRunnable=uninstallRunnable;
        this.canvas=canvas;
        this.physicalLayer=physicalLayer;
        this.mouseManager=mouseManager;
        //creates the panels
        if(customActionMap!=null)
            controlsPanel=new ControlsPanel(this,defaultActionMap,customActionMap,defaultMouseAndKeyboardSettings,customMouseAndKeyboardSettings);
        else
        	controlsPanel=null;
        profilePanel=createProfilePanel();
        if(creditsContent!=null)
            creditsPanel=createCreditsPanel(creditsContent);
        else
        	creditsPanel=null;
        displaySettingsMenuPanel=new DisplaySettingsPanel(this,toggleScreenModeAction);
        soundSettingsMenuPanel=createSoundSettingsMenuPanel(soundManager);
        desktopShortcutsMenuPanel=createDesktopShortcutsMenuPanel();
        initialMenuPanel=createInitialMenuPanel(toExitGameTriggerAction);
        optionsMenuPanel=createOptionsMenuPanel();
        this.toLoadingDisplayAction=toLoadingDisplayAction;
        startMenuPanel=createStartMenuPanel();
        storyModePanel=createStoryModePanel(toLoadingDisplayAction);
        arenaModePanel=createArenaModePanel();
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
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),toExitGameTriggerAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    private final UIPanel createStoryModePanel(final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction){
        final UIPanel storyModePanel=new UIPanel(new RowLayout(false));
        final UIButton level0Button=new UIButton("Level 0");
        level0Button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onLevelButtonActionPerformed(ae);
            }
        });
        final UIButton perfTestLevelButton=new UIButton("Performance Test");
        perfTestLevelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onPerfTestLevelButtonActionPerformed(ae);
            }
        });
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        storyModePanel.add(level0Button);
        storyModePanel.add(perfTestLevelButton);
        storyModePanel.add(backButton);
        return(storyModePanel);
    }
    
    private void onLevelButtonActionPerformed(final ActionEvent ae){
    	((int[])toLoadingDisplayAction.arguments.getArgument(0))[0]=0;
    	toLoadingDisplayAction.perform(null,null,-1);
    }
    
    private void onPerfTestLevelButtonActionPerformed(final ActionEvent ae){
    	((int[])toLoadingDisplayAction.arguments.getArgument(0))[0]=1;
    	toLoadingDisplayAction.perform(null,null,-1);
    }
    
    private final UIPanel createArenaModePanel(){
    	final UIPanel arenaModePanel=new UIPanel(new RowLayout(false));
    	
    	final UIPanel matchTypePanel=new UIPanel(new RowLayout(true));
    	matchTypePanel.add(new UILabel("Match type"));
    	final Object[] subModes=getUnlockedMatchTypes();
    	final DefaultComboBoxModel subModesModel=new DefaultComboBoxModel(subModes);
    	updateMatchTypeModel(subModesModel);
    	final UIComboBox subModeCombo=new UIComboBox(subModesModel);
    	subModeCombo.setSelectedIndex(0);
    	matchTypePanel.add(subModeCombo);
    	
    	final UIPanel playersPanel=new UIPanel(new RowLayout(true));
    	playersPanel.add(new UILabel("Players"));
    	final Object[] playersSettingsSuggestions=getAvailablePlayersSettingsFromUnlockedPlayers();
    	final DefaultComboBoxModel playersSettingsModel=new DefaultComboBoxModel(playersSettingsSuggestions);
    	final UIComboBox playersCombo=new UIComboBox(playersSettingsModel);
    	playersCombo.setSelectedIndex(0);
    	//TODO update the real players settings
    	playersCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue){
				//TODO update the real players settings
				if(newValue==CUSTOM)
			        {//TODO open the GUI
				     
			        }
			}
    	});
    	playersPanel.add(playersCombo);
    	
    	final UIPanel weaponsPanel=new UIPanel(new RowLayout(true));
    	weaponsPanel.add(new UILabel("Weapons"));
    	final Object[] weaponsSettingsSuggestions=getAvailableWeaponsSettingsFromUnlockedWeapons();
    	final DefaultComboBoxModel weaponsSettingsModel=new DefaultComboBoxModel(weaponsSettingsSuggestions);
    	final UIComboBox weaponsCombo=new UIComboBox(weaponsSettingsModel);
    	weaponsCombo.setSelectedIndex(0);
    	//TODO update the real weapons settings
    	weaponsCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue){
				//TODO update the real weapons settings
				if(newValue==CUSTOM)
				    {//TODO open the GUI
					 
				    }
			}
    	});
    	weaponsPanel.add(weaponsCombo);
    	
    	final UIPanel victoryPanel=new UIPanel(new RowLayout(true));
    	victoryPanel.add(new UILabel("Victory"));
    	final Object[] victorySuggestions=new Object[]{NO_LIMIT,"1","2","3","4","5","10","15","20","25","30","35","40","45","50"};
    	final DefaultComboBoxModel victoryModel=new DefaultComboBoxModel(victorySuggestions);
    	updateVictoryModel(victoryModel,(MatchType)subModeCombo.getSelectedValue());
    	final UIComboBox victoryCombo=new UIComboBox(victoryModel);
    	victoryCombo.setSelectedIndex(5);
    	victoryPanel.add(victoryCombo);
    	
    	final UIPanel timePanel=new UIPanel(new RowLayout(true));
    	timePanel.add(new UILabel("Time"));
    	final Object[] timeSuggestions=new Object[]{NO_LIMIT,"1","2","3","4","5","6","7","8","9","10","15","20","30"};
    	final DefaultComboBoxModel timeModel=new DefaultComboBoxModel(timeSuggestions);
    	final UIComboBox timeCombo=new UIComboBox(timeModel);
    	timeCombo.setSelectedIndex(0);
    	timePanel.add(timeCombo);
    	
    	final UIPanel arenasPanel=new UIPanel(new RowLayout(true));
    	arenasPanel.add(new UILabel("Arena"));
    	final Object[] arenas=getUnlockedArenas();
    	final DefaultComboBoxModel arenasModel=new DefaultComboBoxModel(arenas);
    	final UIComboBox arenasCombo=new UIComboBox(arenasModel);
    	arenasCombo.setSelectedIndex(0);
    	arenasPanel.add(arenasCombo);
    	
    	subModeCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue){
				updateVictoryModel(victoryModel,(MatchType)newValue);
				//forces the update of the combo
				victoryCombo.setSelectedIndex(victoryCombo.getSelectedIndex());
				victoryCombo.fireComponentDirty();
			}
		});
    	
    	final UIButton beginMatchButton=new UIButton("Begin match");
    	beginMatchButton.addActionListener(new ActionListener(){           
            @Override
            @SuppressWarnings("unused")
            public void actionPerformed(ActionEvent event){
            	final MatchType matchType=(MatchType)subModeCombo.getSelectedValue();
            	final int victoryLimit;
            	final String victoryValue=(String)victoryCombo.getSelectedValue();
            	if(victoryValue.equals(NO_LIMIT))
            		victoryLimit=-1;
            	else
            		victoryLimit=Integer.parseInt(victoryValue);
            	final int timeLimit;
            	final String timeValue=(String)timeCombo.getSelectedValue();
            	if(timeValue.equals(NO_LIMIT))
            		timeLimit=-1;
            	else
            		timeLimit=Integer.parseInt(timeValue);
            	final String arenaName=(String)arenasCombo.getSelectedValue();
                //TODO retrieve the selected settings for players and weapons
            	//TODO begin the match
            }
        });
    	beginMatchButton.setEnabled(false);
    	
    	final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        arenaModePanel.add(matchTypePanel);
        arenaModePanel.add(playersPanel);
        arenaModePanel.add(weaponsPanel);
        arenaModePanel.add(victoryPanel);
        arenaModePanel.add(timePanel);
        arenaModePanel.add(arenasPanel);
        arenaModePanel.add(beginMatchButton);
        arenaModePanel.add(backButton);
    	return(arenaModePanel);
    }
    
    private final void updateMatchTypeModel(final DefaultComboBoxModel matchTypeModel){
    	for(int matchTypeIndex=0;matchTypeIndex<matchTypeFactory.getSize();matchTypeIndex++)
    		{final MatchType matchType=matchTypeFactory.get(matchTypeIndex);
    		 final String view=matchTypeFactory.getFormattedStringForCombo(matchType);
    		 matchTypeModel.setViewAt(matchTypeIndex,view);
    		}
    }
    
    private final void updateVictoryModel(final DefaultComboBoxModel victoryModel,final MatchType matchType){
    	if(matchType==matchTypeFactory.get("DEATHMATCH"))
    	    {for(int elementIndex=0;elementIndex<victoryModel.size();elementIndex++)
    	         {final String value=(String)victoryModel.getValueAt(elementIndex);
    	          final String view;
    	          if(elementIndex==0)
    	    	      view="  Get best score  ";
    	          else
    	    	      view="   Reach score "+value+"  ";
    	          victoryModel.setViewAt(elementIndex,view);
    	         }
    	    }
    	else
    	    if(matchType==matchTypeFactory.get("CAPTURE_THE_FLAG"))
    	    	{for(int elementIndex=0;elementIndex<victoryModel.size();elementIndex++)
   	    	         {final String value=(String)victoryModel.getValueAt(elementIndex);
	    	          final String view;
	    	          if(elementIndex==0)
	    		          view="Capture most flags";
	    	          else
	    		          view="Capture "+value+" flag"+((elementIndex==1)?"":"s"+"    ");
	    	          victoryModel.setViewAt(elementIndex,view);
	    	         }
    	        }
    	    else
    	        if(matchType==matchTypeFactory.get("HOLD_THE_BAG"))
    	    	    {for(int elementIndex=0;elementIndex<victoryModel.size();elementIndex++)
  	    	             {final String value=(String)victoryModel.getValueAt(elementIndex);
	    	              final String view;
	    	              if(elementIndex==0)
	    		              view=" Hold it the most ";
	    	              else
	    		              view=" Hold it "+value+" minute"+((elementIndex==1)?"":"s"+" ");
	    	              victoryModel.setViewAt(elementIndex,view);
	    	             }
    			    }
    }
    
    private Object[] getUnlockedMatchTypes(){
    	final Object[] unlockedMatchTypes=new Object[matchTypeFactory.getSize()];
    	for(int matchTypeIndex=0;matchTypeIndex<matchTypeFactory.getSize();matchTypeIndex++)
    		unlockedMatchTypes[matchTypeIndex]=matchTypeFactory.get(matchTypeIndex);
    	//FIXME return unlocked match types
    	return(unlockedMatchTypes);
    }
    
    private Object[] getUnlockedArenas(){
    	//FIXME return unlocked arenas
    	return(new Object[]{"Museum","Jail"});
    }
    
    private Object[] getAvailableWeaponsSettingsFromUnlockedWeapons(){
    	//FIXME return weapons settings composed of unlocked weapons
    	return(new Object[]{DEFAULT,"Only knives",CUSTOM});
    }
    
    private Object[] getAvailablePlayersSettingsFromUnlockedPlayers(){
    	//FIXME return players settings composed of unlocked players
    	return(new Object[]{DEFAULT,CUSTOM});
    }
    
    private final UIPanel createProfilePanel(){
    	final UIPanel profilePanel=new UIPanel(new RowLayout(false));
    	final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        profilePanel.add(new UILabel("Feature not yet implemented!"));
        profilePanel.add(backButton);
    	return(profilePanel);
    }

    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  /**
                   * FIXME the profile might have be modified, update 
                   * all parts of the GUI that depend on the unlocked 
                   * items in the profile
                   */
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
        final UIButton profileButton;
        if(profilePanel!=null)
            {profileButton=new UIButton("Profile");
             profileButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(profilePanel);
                 }
             });
            }
        else
        	profileButton=null;
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
        if(profileButton!=null)
        	optionsMenuPanel.add(profileButton);
        if(creditsButton!=null)
        	optionsMenuPanel.add(creditsButton);
        optionsMenuPanel.add(backButton);
    	return(optionsMenuPanel);
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
    
    private final UIPanel createStartMenuPanel(){
        final UIPanel startMenuPanel=new UIPanel(new RowLayout(false));
        final UIButton storyModeButton=new UIButton("Story mode");
        storyModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(storyModePanel);
            }
        });
        final UIButton arenaModeButton=new UIButton("Arena mode");
        arenaModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(arenaModePanel);
            }
        });
        //FIXME enable this button when the arena mode is ready
        arenaModeButton.setEnabled(false);
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(initialMenuPanel);
            }
        });
        startMenuPanel.add(storyModeButton);
        startMenuPanel.add(arenaModeButton);
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
    
    final void showPanelInMainFrame(final UIPanel panel){
        mainFrame.setContentPanel(panel);
        mainFrame.updateMinimumSizeFromContents();
        mainFrame.layout();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
    }
}
