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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UIRadioButton;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.extension.ui.util.ButtonGroup;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.util.MonitorModeUtil;

public class DisplaySettingsPanel extends UIPanel{

	private final MainMenuState mainMenuState;
	
	private final TriggerAction toggleScreenModeAction;
	
	private final DefaultComboBoxModel displayModesModel;
	
	private final UIComboBox displayModesCombo;
	
	private final HashMap<Integer,List<MonitorMode>> screenModesByRotation;
	
	public DisplaySettingsPanel(final MainMenuState mainMenuState,final TriggerAction toggleScreenModeAction){
		super();
		this.mainMenuState=mainMenuState;
		this.toggleScreenModeAction=toggleScreenModeAction;
		setLayout(new RowLayout(false));
    	final UILabel screenModesLabel=new UILabel("Display Mode");
    	final MonitorDevice monitor=((JoglNewtWindow)mainMenuState.canvas).getNewtWindow().getMainMonitor();
    	final MonitorMode currentScreenMode=monitor.getCurrentMode();
    	final List<MonitorMode> screenModes=monitor.getSupportedModes();
    	screenModesByRotation=new HashMap<Integer,List<MonitorMode>>();
    	final int[] rotations=new int[]{0,90,180,270};
    	final ArrayList<Integer> availableRotations=new ArrayList<Integer>();
    	for(int rotation:rotations)
    	    {final List<MonitorMode> rotatedScreenModes=MonitorModeUtil.filterByRotation(screenModes,rotation);
    		 if(rotatedScreenModes!=null&&!rotatedScreenModes.isEmpty())
    			 {screenModesByRotation.put(Integer.valueOf(rotation),rotatedScreenModes);
    			  availableRotations.add(Integer.valueOf(rotation));
    			 }
    	    }
    	final int selectedScreenRotation=currentScreenMode.getRotation();
    	final UIPanel rotationsPanel;
    	final UIRadioButton[] rotationsButtons;
    	if(availableRotations.size()>1)
    	    {rotationsPanel=new UIPanel(new RowLayout(true));
    	     final UILabel screenRotationsLabel=new UILabel("Screen rotation");
    	     rotationsPanel.add(screenRotationsLabel);
    		 final ButtonGroup rotationsGroup=new ButtonGroup();
    		 rotationsButtons=new UIRadioButton[availableRotations.size()];
    		 int availableRotationIndex=0;
    		 for(Integer availableRotation:availableRotations)
    		     {rotationsButtons[availableRotationIndex]=new UIRadioButton(availableRotation.toString());
    		      rotationsGroup.add(rotationsButtons[availableRotationIndex]);
    		      rotationsPanel.add(rotationsButtons[availableRotationIndex]);
    		      if(availableRotation.intValue()==selectedScreenRotation)
    		    	  rotationsButtons[availableRotationIndex].setSelected(true);
    		      availableRotationIndex++;
    		     }
    	    }
    	else
    		{rotationsButtons=null;
    		 rotationsPanel=null;
    		}
    	int selectedScreenModeIndex=-1,screenModeIndex=0;
    	final List<MonitorMode> currentScreenModes=screenModesByRotation.get(Integer.valueOf(selectedScreenRotation));
    	for(MonitorMode screenMode:currentScreenModes)
    		{if(screenMode==currentScreenMode)
    	         {selectedScreenModeIndex=screenModeIndex;
    			  break;
    	         }
    		 screenModeIndex++;
    		}
    	final Object[] screenModesArray=currentScreenModes.toArray();
    	displayModesModel=new DefaultComboBoxModel(screenModesArray);
    	displayModesCombo=new UIComboBox(displayModesModel);
    	if(selectedScreenModeIndex!=-1)
    		displayModesCombo.setSelectedIndex(selectedScreenModeIndex,false);
    	displayModesCombo.addSelectionListener(new SelectionListener<UIComboBox>(){
			@Override
			public void selectionChanged(final UIComboBox component,final Object newValue) {
				onDisplayModesComboSelectionChanged(component,newValue);
			}
		});
    	if(rotationsButtons!=null)
    	    {for(UIRadioButton rotationButton:rotationsButtons)
	             {rotationButton.addActionListener(new ActionListener(){
			          @Override
			          public void actionPerformed(ActionEvent ae){
			        	  onRotationButtonActionPerformed(ae);
			          }
		          });
	             }
    	    }
    	final UIButton windowingModeButton=new UIButton("Switch to windowed mode or full screen mode");
    	windowingModeButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	onWindowingModeButtonActionPerformed(event);
            }
        });
    	final UIPanel vSyncPanel=new UIPanel(new RowLayout(true));
    	final ButtonGroup vSyncGroup=new ButtonGroup();
    	final UILabel vSyncLabel=new UILabel("Vertical synchronization");
    	final UIRadioButton enableVSyncButton=new UIRadioButton("On");
    	enableVSyncButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	onEnableVSyncButtonActionPerformed(event);
            }
        });
    	final UIRadioButton disableVSyncButton=new UIRadioButton("Off");
    	disableVSyncButton.setSelected(true);
    	disableVSyncButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	onDisableVSyncButtonActionPerformed(event);
            }
        });
    	vSyncGroup.add(enableVSyncButton);
    	vSyncGroup.add(disableVSyncButton);
    	vSyncPanel.add(vSyncLabel);
    	vSyncPanel.add(enableVSyncButton);
    	vSyncPanel.add(disableVSyncButton);
    	final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
            	onBackButtonActionPerformed(event);
            }
        });
        add(windowingModeButton);
        add(vSyncPanel);
        add(screenModesLabel);
        add(displayModesCombo);
        if(rotationsPanel!=null)
            add(rotationsPanel);
        add(backButton);
	}
	
	private void onRotationButtonActionPerformed(final ActionEvent ae){
		final Integer selectedRotation=Integer.valueOf(Integer.parseInt(((UIRadioButton)ae.getSource()).getText()));
		displayModesCombo.setSelectedIndex(-1,false);
		displayModesModel.clear();
		final MonitorDevice monitor=((JoglNewtWindow)mainMenuState.canvas).getNewtWindow().getMainMonitor();
		final MonitorMode freshCurrentScreenMode=monitor.getCurrentMode();
		for(MonitorMode rotatedScreenMode:screenModesByRotation.get(selectedRotation))
		    {displayModesModel.addItem(rotatedScreenMode);
			 if(rotatedScreenMode.getSurfaceSize().getResolution().getWidth()==freshCurrentScreenMode.getSurfaceSize().getResolution().getWidth()&&
			    rotatedScreenMode.getSurfaceSize().getResolution().getHeight()==freshCurrentScreenMode.getSurfaceSize().getResolution().getHeight()&&
				rotatedScreenMode.getRefreshRate()==monitor.getCurrentMode().getRefreshRate())
			     setScreenMode(rotatedScreenMode);
			}
		displayModesCombo.setSelectedIndex(screenModesByRotation.get(selectedRotation).indexOf(monitor.getCurrentMode()),false);
	}
	
	private void onWindowingModeButtonActionPerformed(final ActionEvent event){
		toggleScreenModeAction.perform(mainMenuState.canvas,null,Double.NaN);
	}
	
    private void onEnableVSyncButtonActionPerformed(final ActionEvent event){
    	mainMenuState.canvas.setVSyncEnabled(true);
	}
	
	private void onDisableVSyncButtonActionPerformed(final ActionEvent event){
		mainMenuState.canvas.setVSyncEnabled(false);
	}
	
	private void onBackButtonActionPerformed(final ActionEvent event){
		mainMenuState.showPanelInMainFrame(mainMenuState.optionsMenuPanel);
	}
	
	private void onDisplayModesComboSelectionChanged(final UIComboBox component,final Object newValue){
		setScreenMode((MonitorMode)newValue);
	}
	
	private void setScreenMode(final MonitorMode monitorMode){
		final MonitorDevice monitor=((JoglNewtWindow)mainMenuState.canvas).getNewtWindow().getMainMonitor();
		monitor.setCurrentMode(monitorMode);
		updateUiLocationOnCameraChange(monitorMode.getRotatedWidth(),monitorMode.getRotatedHeight());
	}
	
	private void updateUiLocationOnCameraChange(final int width,final int height){
		//the camera is going to take into account the change of resolution very soon, the update of the UI must be done later
		final CanvasRenderer canvasRenderer=mainMenuState.canvas.getCanvasRenderer();
    	final RenderContext renderContext=canvasRenderer.getRenderContext();
		GameTaskQueueManager.getManager(renderContext).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>(){
			@Override
			public Void call() throws Exception{
				if(canvasRenderer.getCamera().getWidth()==width&&
				   canvasRenderer.getCamera().getHeight()==height)
				    {final Camera cam=mainMenuState.canvas.getCanvasRenderer().getCamera();
					 mainMenuState.mainFrame.setLocationRelativeTo(cam);
				    }
				else
					updateUiLocationOnCameraChange(width,height);
				return null;
			}
    	});
	}
}
