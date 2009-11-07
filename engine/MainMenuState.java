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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
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
import com.ardor3d.ui.text.BMText;

final class MainMenuState extends State{
    
    
    private final JoglCanvas canvas;
    
    private final PhysicalLayer physicalLayer;
    
    private final AwtMouseManager awtMouseManager;
    
    private final UIFrame mainFrame;
    
    private final UIPanel initialMenuPanel;
    
    private final UIPanel startMenuPanel;
    
    private final UIPanel controlsPanel;
    
    private final UIPanel creditsPanel;
    
    private final UIPanel loadGamePanel;
    
    private static final String creditsPath="/credits.txt";
    
    private static final String controlsPath="/controls.txt";
    
    
    MainMenuState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,
                  final AwtMouseManager awtMouseManager,
                  final TriggerAction exitAction,final TriggerAction toLoadingDisplayAction){
        super();
        this.canvas=canvas;
        this.physicalLayer=physicalLayer;
        this.awtMouseManager=awtMouseManager;
        // create the panels
        initialMenuPanel=createInitialMenuPanel(exitAction);       
        startMenuPanel=createStartMenuPanel(toLoadingDisplayAction);
        loadGamePanel=createLoadGamePanel(toLoadingDisplayAction);
        controlsPanel=createControlsPanel();
        creditsPanel=createCreditsPanel();
        // create the main frame
        mainFrame=createMainFrame();
        // create the head-up display
        final UIHud hud=createHud();        
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        // add some text
        final BMText textNode=new BMText("gameTitleNode","Truly Unusual Experience of Revolution",Ardor3DGameServiceProvider.getFontsList().get(1),BMText.Align.Center,BMText.Justify.Center);
        textNode.setFontScale(2);
        textNode.setTextColor(ColorRGBA.RED);
        textNode.setTranslation(textNode.getTranslation().add(0,3.3,0,null));
        getRoot().attachChild(textNode);
        // setup the keyboard trigger(s)
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
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
                 {awtMouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  // show the initial menu
                  showPanelInMainFrame(initialMenuPanel);
                 }
             else
                 awtMouseManager.setGrabbed(GrabbedState.GRABBED);
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
        final UIButton controlsButton=new UIButton("Controls");
        controlsButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(controlsPanel);
            }
        });
        final UIButton creditsButton=new UIButton("Credits");
        creditsButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(creditsPanel);
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
        initialMenuPanel.add(controlsButton);
        initialMenuPanel.add(creditsButton);
        initialMenuPanel.add(exitButton);
        return(initialMenuPanel);
    }
    
    private final UIPanel createStartMenuPanel(final TriggerAction toLoadingDisplayAction){
        final UIPanel startMenuPanel=new UIPanel(new RowLayout(false));       
        final UIButton newGameButton=new UIButton("New game");
        newGameButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                toLoadingDisplayAction.perform(null,null,-1);
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
    
    private final UIPanel createCreditsPanel(){
        return(createTextualPanel(creditsPath));
    }
    
    private final UIPanel createControlsPanel(){
        return(createTextualPanel(controlsPath));
    }
    
    private final String getTextFileContent(final String path){
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
        String line;
        StringBuffer textContent=new StringBuffer();
        try{while((line=bufferedReader.readLine())!=null)
                textContent.append(line+"\n");
            bufferedReader.close();
           }
        catch(IOException ioe)
        {ioe.printStackTrace();}
        return(textContent.toString());
    }
    
    private final UIPanel createTextualPanel(final String path){
        final UILabel label=new UILabel(getTextFileContent(path));
        final UIPanel textualPanel=new UIPanel(new RowLayout(false));
        textualPanel.add(label);
        final UIButton backButton=new UIButton("Back");
        backButton.addActionListener(new ActionListener(){           
            @Override
            public void actionPerformed(ActionEvent event){
                showPanelInMainFrame(initialMenuPanel);
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
                // update the triggers of the hud
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
