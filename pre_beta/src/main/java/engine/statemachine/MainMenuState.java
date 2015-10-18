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
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.ui.text.BMText;

import engine.data.ProfileData;
import engine.data.common.MatchType;
import engine.data.common.MatchTypeFactory;
import engine.input.ActionMap;
import engine.input.MouseAndKeyboardSettings;
import engine.misc.FontStore;
import engine.misc.LocalizedMessageProvider;
import engine.misc.SettingsProvider;
import engine.sound.SoundManager;

public final class MainMenuState extends ScenegraphState{
	
	private final LocalizedMessageProvider localizedMessageProvider;
	
	private final MatchTypeFactory matchTypeFactory;
    
    private final String noLimitMsg;
    
    private final String defaultMsg;
    
    private final String customMsg;
    
    final NativeCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel startMenuPanel;
    
    final UIPanel optionsMenuPanel;
    
    private final UIPanel confirmExitMenuPanel;
    
    private final DisplaySettingsPanel displaySettingsMenuPanel;
    
    private final UIPanel soundSettingsMenuPanel;
    
    private final UIPanel desktopShortcutsMenuPanel;
    
    private final ControlsPanel controlsPanel;
    
    private final UIPanel profilePanel;
    
    private final UIPanel readmePanel;
    
    private final UIPanel storyModePanel;
    
    private final UIPanel arenaModePanel;
    
    private final Runnable launchRunnable;
    
    private final Runnable uninstallRunnable;
    
    private final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction;
    
    private final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction;
    
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
     * @param gameLongName full name of the game
     * @param gameRecommendedDownloadUrl recommended URL to download the game
     * @param readmeContent "read me" content (may be null)
     * @param fontStore store that contains fonts
     * @param toggleScreenModeAction action allowing to modify the windowing mode
     * @param defaultActionMap default action map, which should not be modified, used to reset the custom action map to its default value
     * @param customActionMap custom action map, which can be modified
     * @param defaultMouseAndKeyboardSettings default mouse and keyboard settings, which should not be modified, used to reset the custom ones to their default values
     * @param customMouseAndKeyboardSettings custom mouse and keyboard settings, which can be modified
     * @param profileData data of the profile
     * @param localizedMessageProvider provider of localized messages
     */
    public MainMenuState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,
                  final MouseManager mouseManager,
                  final TransitionTriggerAction<ScenegraphState,String> toExitGameTriggerAction,final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction,
                  final SoundManager soundManager,final Runnable launchRunnable,
                  final Runnable uninstallRunnable,final String gameLongName,final String gameRecommendedDownloadUrl,final String readmeContent,
      			  final FontStore fontStore,final TriggerAction toggleScreenModeAction,final ActionMap defaultActionMap,
      			  final ActionMap customActionMap,final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings,
      			  final MouseAndKeyboardSettings customMouseAndKeyboardSettings,
      			  final ProfileData profileData,final LocalizedMessageProvider localizedMessageProvider,final SettingsProvider settingsProvider){
        super(soundManager);
        this.localizedMessageProvider=localizedMessageProvider;
        this.noLimitMsg=localizedMessageProvider.getString("NO_LIMIT");
        this.defaultMsg=localizedMessageProvider.getString("DEFAULT");
        this.customMsg=localizedMessageProvider.getString("CUSTOM");
        //TODO move this factory into another location
        this.matchTypeFactory=initMatchTypeFactory();
        this.launchRunnable=launchRunnable;
        this.uninstallRunnable=uninstallRunnable;
        this.toExitGameTriggerAction=toExitGameTriggerAction;
        this.canvas=canvas;
        this.physicalLayer=physicalLayer;
        this.mouseManager=mouseManager;
        //creates the panels
        if(customActionMap!=null)
            controlsPanel=new ControlsPanel(this,defaultActionMap,customActionMap,defaultMouseAndKeyboardSettings,customMouseAndKeyboardSettings,localizedMessageProvider);
        else
        	controlsPanel=null;
        profilePanel=createProfilePanel();
        if(readmeContent!=null)
            readmePanel=createReadmePanel(readmeContent);
        else
        	readmePanel=null;
        displaySettingsMenuPanel=new DisplaySettingsPanel(this,toggleScreenModeAction,localizedMessageProvider,settingsProvider);
        soundSettingsMenuPanel=createSoundSettingsMenuPanel(soundManager);
        desktopShortcutsMenuPanel=createDesktopShortcutsMenuPanel();
        initialMenuPanel=createInitialMenuPanel(toExitGameTriggerAction);
        optionsMenuPanel=createOptionsMenuPanel();
        confirmExitMenuPanel=createConfirmExitMenuPanel();
        this.toLoadingDisplayAction=toLoadingDisplayAction;
        startMenuPanel=createStartMenuPanel(profileData);
        storyModePanel=createStoryModePanel(toLoadingDisplayAction);
        arenaModePanel=createArenaModePanel();
        //creates the main frame
        mainFrame=createMainFrame();
        //creates the head-up display
        final UIHud hud=createHud();        
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        //adds some text
        final String recommendedUrlText=localizedMessageProvider.getString("RECOMMENDED_DOWNLOAD_URL")+": "+gameRecommendedDownloadUrl;
        final BMText gameTitleTextNode=new BMText("gameTitleNode",gameLongName,fontStore.getFontsList().get(1),BMText.Align.Center,BMText.Justify.Center);
        gameTitleTextNode.setFontScale(2);
        gameTitleTextNode.setTextColor(ColorRGBA.RED);
        gameTitleTextNode.setTranslation(gameTitleTextNode.getTranslation().add(0,3.3,0,null));
        getRoot().attachChild(gameTitleTextNode);
        final BMText recommendedUrlTextNode=new BMText("recommendedUrlNode",recommendedUrlText,fontStore.getFontsList().get(2),BMText.Align.Center,BMText.Justify.Center);
        recommendedUrlTextNode.setFontScale(1);
        recommendedUrlTextNode.setTextColor(ColorRGBA.GREEN);
        recommendedUrlTextNode.setTranslation(recommendedUrlTextNode.getTranslation().add(0,3.0,0,null));
        getRoot().attachChild(recommendedUrlTextNode);
        //setups the keyboard triggers
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),toExitGameTriggerAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    private MatchTypeFactory initMatchTypeFactory(){
    	final MatchTypeFactory matchTypeFactory=new MatchTypeFactory();
    	//FIXME compute the number of spaces required to center the text
        matchTypeFactory.addNewMatchType("DEATHMATCH",localizedMessageProvider.getString("DEATHMATCH"),"  "+localizedMessageProvider.getString("GET_BEST_SCORE")+"  ");
        matchTypeFactory.addNewMatchType("CAPTURE_THE_FLAG",localizedMessageProvider.getString("CAPTURE_THE_FLAG"),localizedMessageProvider.getString("CAPTURE_MOST_FLAGS"));
        matchTypeFactory.addNewMatchType("HOLD_THE_BAG",localizedMessageProvider.getString("HOLD_THE_BAG")," "+localizedMessageProvider.getString("HOLD_IT_THE_MOST")+" ");
        return(matchTypeFactory);
    }
    
    private final UIPanel createStoryModePanel(final TransitionTriggerAction<ScenegraphState,String> toLoadingDisplayAction){
        final UIPanel storyModePanel=new UIPanel(new RowLayout(false));
        //FIXME stop hardcoding the level identifiers, use the level factory
        final LevelUIButton level0Button=new LevelUIButton("Tutorial","0");
        level0Button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onLevelButtonActionPerformed(ae,"0");
            }
        });
        final LevelUIButton level1Button=new LevelUIButton("Museum","1");
        level1Button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onLevelButtonActionPerformed(ae,"1");
            }
        });
        final LevelUIButton level2Button=new LevelUIButton("Outdoor","2");
        level2Button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onLevelButtonActionPerformed(ae,"2");
            }
        });
        final LevelUIButton level3Button=new LevelUIButton("Bagnolet","3");
        level3Button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onLevelButtonActionPerformed(ae,"3");
            }
        });
        final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        storyModePanel.add(level0Button);
        storyModePanel.add(level1Button);
        storyModePanel.add(level2Button);
        storyModePanel.add(level3Button);
        storyModePanel.add(backButton);
        return(storyModePanel);
    }
    
    private static final class LevelUIButton extends UIButton{
    	
    	private final String levelIdentifier;
    	
    	private LevelUIButton(final String text,final String levelIdentifier){
    		super(text);
    		this.levelIdentifier=levelIdentifier;
    	}
    }
    
    private void updateStoryModePanel(final ProfileData profileData){
    	for(Spatial child:this.storyModePanel.getChildren())
    		if(child instanceof LevelUIButton)
    	        {final LevelUIButton levelButton=(LevelUIButton)child;
    	         final boolean enabled=profileData.containsUnlockedLevelIdentifier(levelButton.levelIdentifier);
    	         levelButton.setEnabled(enabled);
    	        }
    }
    
    private void onLevelButtonActionPerformed(final ActionEvent ae,final String levelIdentifier){
    	toLoadingDisplayAction.arguments.setNextLevelIdentifier(levelIdentifier);
    	toLoadingDisplayAction.perform(null,null,-1);
    }
    
    private final UIPanel createArenaModePanel(){
    	final UIPanel arenaModePanel=new UIPanel(new RowLayout(false));
    	
    	final UIPanel matchTypePanel=new UIPanel(new RowLayout(true));
    	matchTypePanel.add(new UILabel(localizedMessageProvider.getString("MATCH_TYPE")));
    	final Object[] subModes=getUnlockedMatchTypes();
    	final DefaultComboBoxModel subModesModel=new DefaultComboBoxModel(subModes);
    	updateMatchTypeModel(subModesModel);
    	final UIComboBox subModeCombo=new UIComboBox(subModesModel);
    	subModeCombo.setSelectedIndex(0);
    	matchTypePanel.add(subModeCombo);
    	
    	final UIPanel playersPanel=new UIPanel(new RowLayout(true));
    	playersPanel.add(new UILabel(localizedMessageProvider.getString("PLAYERS")));
    	final Object[] playersSettingsSuggestions=getAvailablePlayersSettingsFromUnlockedPlayers();
    	final DefaultComboBoxModel playersSettingsModel=new DefaultComboBoxModel(playersSettingsSuggestions);
    	final UIComboBox playersCombo=new UIComboBox(playersSettingsModel);
    	playersCombo.setSelectedIndex(0);
    	//TODO update the real players settings
    	playersCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue){
				//TODO update the real players settings
				if(newValue==customMsg)
			        {//TODO open the GUI
				     
			        }
			}
    	});
    	playersPanel.add(playersCombo);
    	
    	final UIPanel weaponsPanel=new UIPanel(new RowLayout(true));
    	weaponsPanel.add(new UILabel(localizedMessageProvider.getString("WEAPONS")));
    	final Object[] weaponsSettingsSuggestions=getAvailableWeaponsSettingsFromUnlockedWeapons();
    	final DefaultComboBoxModel weaponsSettingsModel=new DefaultComboBoxModel(weaponsSettingsSuggestions);
    	final UIComboBox weaponsCombo=new UIComboBox(weaponsSettingsModel);
    	weaponsCombo.setSelectedIndex(0);
    	//TODO update the real weapons settings
    	weaponsCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue){
				//TODO update the real weapons settings
				if(newValue==customMsg)
				    {//TODO open the GUI
					 
				    }
			}
    	});
    	weaponsPanel.add(weaponsCombo);
    	
    	final UIPanel victoryPanel=new UIPanel(new RowLayout(true));
    	victoryPanel.add(new UILabel(localizedMessageProvider.getString("VICTORY")));
    	final Object[] victorySuggestions=new Object[]{noLimitMsg,"1","2","3","4","5","10","15","20","25","30","35","40","45","50"};
    	final DefaultComboBoxModel victoryModel=new DefaultComboBoxModel(victorySuggestions);
    	updateVictoryModel(victoryModel,(MatchType)subModeCombo.getSelectedValue());
    	final UIComboBox victoryCombo=new UIComboBox(victoryModel);
    	victoryCombo.setSelectedIndex(5);
    	victoryPanel.add(victoryCombo);
    	
    	final UIPanel timePanel=new UIPanel(new RowLayout(true));
    	timePanel.add(new UILabel(localizedMessageProvider.getString("TIME")));
    	final Object[] timeSuggestions=new Object[]{noLimitMsg,"1","2","3","4","5","6","7","8","9","10","15","20","30"};
    	final DefaultComboBoxModel timeModel=new DefaultComboBoxModel(timeSuggestions);
    	final UIComboBox timeCombo=new UIComboBox(timeModel);
    	timeCombo.setSelectedIndex(0);
    	timePanel.add(timeCombo);
    	
    	final UIPanel arenasPanel=new UIPanel(new RowLayout(true));
    	arenasPanel.add(new UILabel(localizedMessageProvider.getString("ARENA")));
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
    	
    	final UIButton beginMatchButton=new UIButton(localizedMessageProvider.getString("BEGIN_MATCH"));
    	beginMatchButton.addActionListener(new ActionListener(){           
            @Override
            @SuppressWarnings("unused")
            public void actionPerformed(ActionEvent event){
            	final MatchType matchType=(MatchType)subModeCombo.getSelectedValue();
            	final int victoryLimit;
            	final String victoryValue=(String)victoryCombo.getSelectedValue();
            	if(victoryValue.equals(noLimitMsg))
            		victoryLimit=-1;
            	else
            		victoryLimit=Integer.parseInt(victoryValue);
            	final int timeLimit;
            	final String timeValue=(String)timeCombo.getSelectedValue();
            	if(timeValue.equals(noLimitMsg))
            		timeLimit=-1;
            	else
            		timeLimit=Integer.parseInt(timeValue);
            	final String arenaName=(String)arenasCombo.getSelectedValue();
                //TODO retrieve the selected settings for players and weapons
            	//TODO begin the match
            }
        });
    	beginMatchButton.setEnabled(false);
    	
    	final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
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
    	    	      view=matchType.getNoLimitObjectiveDescriptionLabel();
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
	    	        	  view=matchType.getNoLimitObjectiveDescriptionLabel();
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
	    	            	  view=matchType.getNoLimitObjectiveDescriptionLabel();
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
    	return(new Object[]{defaultMsg,"Only knives",customMsg});
    }
    
    private Object[] getAvailablePlayersSettingsFromUnlockedPlayers(){
    	//FIXME return players settings composed of unlocked players
    	return(new Object[]{defaultMsg,customMsg});
    }
    
    private final UIPanel createProfilePanel(){
    	final UIPanel profilePanel=new UIPanel(new RowLayout(false));
    	final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        profilePanel.add(new UILabel(localizedMessageProvider.getString("FEATURE_NOT_YET_IMPLEMENTED")));
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
                  //TODO check which levels are available
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
        final UIButton startButton=new UIButton(localizedMessageProvider.getString("START"));
        startButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(startMenuPanel);
            }
        });
        final UIButton optionsButton=new UIButton(localizedMessageProvider.getString("OPTIONS"));
        optionsButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        });
        final UIButton exitButton=new UIButton(localizedMessageProvider.getString("EXIT"));
        exitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onExitButtonActionPerformed(ae);
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
    	    {desktopShortcutsButton=new UIButton(localizedMessageProvider.getString("SHORTCUTS"));
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
    	    {displaySettingsButton=new UIButton(localizedMessageProvider.getString("DISPLAY"));
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
	        {soundSettingsButton=new UIButton(localizedMessageProvider.getString("SOUND"));
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
            {controlsButton=new UIButton(localizedMessageProvider.getString("CONTROLS"));
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
            {profileButton=new UIButton(localizedMessageProvider.getString("PROFILE"));
             profileButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(profilePanel);
                 }
             });
            }
        else
        	profileButton=null;
        final UIButton readmeButton;
        if(readmePanel!=null)
            {readmeButton=new UIButton(localizedMessageProvider.getString("READ_ME"));
             readmeButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                     showPanelInMainFrame(readmePanel);
                 }
             });
            }
        else
        	readmeButton=null;
        final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
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
        if(readmeButton!=null)
        	optionsMenuPanel.add(readmeButton);
        optionsMenuPanel.add(backButton);
    	return(optionsMenuPanel);
    }
    
    private final UIPanel createConfirmExitMenuPanel(){
		final UIPanel confirmExitMenuPanel=new UIPanel(new RowLayout(false));
		final UILabel confirmLabel=new UILabel(localizedMessageProvider.getString("CONFIRM_EXIT"));
		final UIButton yesButton=new UIButton(localizedMessageProvider.getString("YES"));
		yesButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
            	onYesExitButtonActionPerformed(ae);
            }
        });
		final UIButton noButton=new UIButton(localizedMessageProvider.getString("NO"));
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
    
    private void onExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(confirmExitMenuPanel);
    }
    
    private void onYesExitButtonActionPerformed(final ActionEvent ae){
    	toExitGameTriggerAction.perform(null,null,-1);
	}
	
	private void onNoExitButtonActionPerformed(final ActionEvent ae){
		showPanelInMainFrame(initialMenuPanel);
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
    	     final UIButton toggleSoundButton=new UIButton(localizedMessageProvider.getString("SWITCH_SOUND_ON_OFF"));
    	     toggleSoundButton.addActionListener(new ActionListener(){           
                 @Override
                 public void actionPerformed(ActionEvent event){
                	 toggleSoundManagerAction.perform(canvas,null,Double.NaN);
                 }
             });
    	     final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
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
                 {final UIButton addDesktopShortcutButton=new UIButton(localizedMessageProvider.getString("ADD_A_DESKTOP_SHORTCUT_TO_LAUNCH_THE_GAME"));
                  addDesktopShortcutButton.addActionListener(new ActionListener(){
                      @Override
                      public void actionPerformed(ActionEvent event){
           	              launchRunnable.run();
                      }
                  });
                  desktopShortcutsMenuPanel.add(addDesktopShortcutButton);
                 }
             if(uninstallRunnable!=null)
                 {final UIButton addUninstallDesktopShortcutButton=new UIButton(localizedMessageProvider.getString("ADD_A_DESKTOP_SHORTCUT_TO_UNINSTALL_THE_GAME"));
                  addUninstallDesktopShortcutButton.addActionListener(new ActionListener(){
                      @Override
                      public void actionPerformed(ActionEvent event){
           	              uninstallRunnable.run();
                      }
                  });
                  desktopShortcutsMenuPanel.add(addUninstallDesktopShortcutButton);
                 }
             final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
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
    
    private final UIPanel createStartMenuPanel(final ProfileData profileData){
        final UIPanel startMenuPanel=new UIPanel(new RowLayout(false));
        final UIButton storyModeButton=new UIButton(localizedMessageProvider.getString("STORY_MODE"));
        storyModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	updateStoryModePanel(profileData);
                showPanelInMainFrame(storyModePanel);
            }
        });
        final UIButton arenaModeButton=new UIButton(localizedMessageProvider.getString("ARENA_MODE"));
        arenaModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(arenaModePanel);
            }
        });
        //FIXME enable this button when the arena mode is ready
        arenaModeButton.setEnabled(false);
        final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
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
     * Creates a panel that displays a text
     * 
     * @param textualContent text displaying in this panel
     * @param backButtonActionListener listener used when pressing the "Back" button
     * @return
     */
    private final UIPanel createTextualPanel(final String textualContent,final ActionListener backButtonActionListener){
    	final UILabel label=new UILabel(textualContent);
        final UIPanel textualPanel=new UIPanel(new RowLayout(false));
        textualPanel.add(label);
        final UIButton backButton=new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(backButtonActionListener);
        textualPanel.add(backButton);
        return(textualPanel);
    }
    
    /**
     * Creates a panel that displays a text and goes back to the options menu panel when pressing the "Back" button
     * 
     * @param textualContent textualContent text displaying in this panel
     * @return
     */
    private final UIPanel createTextualPanelWithBackButtonShowingOptionsMenuPanel(final String textualContent){
    	final ActionListener backButtonActionListener=new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(optionsMenuPanel);
            }
        };
        final UIPanel textualPanel=createTextualPanel(textualContent,backButtonActionListener);
        return(textualPanel);
    }
    
    /**
     * Creates a panel that displays the "read me" content
     * 
     * @param readmeContent "read me" content (cannot be null)
     * @return
     */
    private final UIPanel createReadmePanel(final String readmeContent){
        final UIPanel readmePanel=createTextualPanelWithBackButtonShowingOptionsMenuPanel(readmeContent);
        return(readmePanel);
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
