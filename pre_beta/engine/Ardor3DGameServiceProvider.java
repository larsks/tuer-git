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

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMFont;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * General entry point of an application created by JFPSM
 * 
 */
final class Ardor3DGameServiceProvider implements Scene{

    /**Our native window, not the gl surface itself*/
    private final NativeCanvas canvas;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    /**Our timer*/
    private final Timer timer;

    /**boolean allowing us to "pull the plug" from anywhere*/
    private boolean exit;

    /**root of our scene*/
    private final Node root;
    
    private double contentSystemRatingStartTime;
    
    private double initializationStartTime;
    
    private double introductionStartTime;
    
    enum Step{/**PEGI-equivalent rating*/
              CONTENT_RATING_SYSTEM,
              /**logo (trademark or brandname)*/
              INITIALIZATION,
              /**introduction scene*/
              INTRODUCTION,
              /**main menu*/
              MAIN_MENU,
              /**display of the level loading*/
              LEVEL_LOADING_DISPLAY,
              /**in-game display*/
              GAME,
              /**display when the player loses*/
              GAME_OVER,
              /**pause menu*/
              PAUSE_MENU,
              /**display at the end of a level with figures, etc...*/
              LEVEL_END_DISPLAY,
              /**final scene*/
              GAME_END_DISPLAY};

    private final StateMachine stateMachine;
    
    private static ArrayList<BMFont> fontsList;
    
    
    public static void main(final String[] args){
    	//Disable DirectDraw under Windows in order to avoid conflicts with OpenGL
    	System.setProperty("sun.java2d.noddraw","true");
        final Ardor3DGameServiceProvider application=new Ardor3DGameServiceProvider();
        application.start();
    }

    
    private final static ArrayList<BMFont> createFontsList(){
        final ArrayList<BMFont> fontsList=new ArrayList<BMFont>();
        try{fontsList.add(new BMFont(ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"/fonts/DejaVuSansCondensed-20-bold-regular.fnt"),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        try{fontsList.add(new BMFont(ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"/fonts/Computerfont-35-medium-regular.fnt"),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        try{fontsList.add(new BMFont(ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"/fonts/arial-16-bold-regular.fnt"),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        return(fontsList);
    }
    
    static final List<BMFont> getFontsList(){
        if(fontsList==null)
            fontsList=createFontsList();
        return(Collections.unmodifiableList(fontsList));
    }


    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    private Ardor3DGameServiceProvider(){
        exit=false;
        contentSystemRatingStartTime=Double.NaN;
        initializationStartTime=Double.NaN;
        introductionStartTime=Double.NaN;
        timer=new Timer();
        root=new Node("root node of the game");
        stateMachine=new StateMachine(root);       
        // Get the default display mode
        final DisplayMode defaultMode=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        // Choose the full-screen mode
        final DisplaySettings settings=new DisplaySettings(defaultMode.getWidth(),defaultMode.getHeight(),defaultMode.getBitDepth(),0,0,8,0,0,true,false);
        // Setup a canvas and a canvas renderer       
        canvas=new JoglCanvas(new JoglCanvasRenderer(this),settings);
        canvas.init();
        mouseManager=new AwtMouseManager((Component)canvas);
        // remove the mouse cursor
        mouseManager.setGrabbed(GrabbedState.GRABBED);
        physicalLayer=new PhysicalLayer(new AwtKeyboardWrapper((Component)canvas),new AwtMouseWrapper((Component)canvas,mouseManager),new AwtFocusWrapper((Component)canvas));
    }

    
    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    private final void start(){
        init();

        // Run in this same thread.
        while(!exit)
            {if(canvas.isClosing())
                 {exit=true;
                  return;
                 }
             updateLogicalLayer(timer);
             timer.update();
             if(stateMachine.isEnabled(Step.CONTENT_RATING_SYSTEM.ordinal()))
                 {if(Double.isNaN(contentSystemRatingStartTime))
                      contentSystemRatingStartTime=timer.getTimeInSeconds();
                  if(timer.getTimeInSeconds()-contentSystemRatingStartTime>2)
                      {stateMachine.setEnabled(Step.CONTENT_RATING_SYSTEM.ordinal(),false);
                       stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),true);       
                      }
                 }
             if(stateMachine.isEnabled(Step.INITIALIZATION.ordinal()))
                 {if(Double.isNaN(initializationStartTime))
                      initializationStartTime=timer.getTimeInSeconds();
                  if(timer.getTimeInSeconds()-initializationStartTime>5&&
                     TaskManager.getInstance().getTaskCount()==0)
                      {stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),false);
                       stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),true);       
                      }
                 }
             if(stateMachine.isEnabled(Step.INTRODUCTION.ordinal()))
                 {if(Double.isNaN(introductionStartTime))
                      introductionStartTime=timer.getTimeInSeconds();
                  if(timer.getTimeInSeconds()-introductionStartTime>17)
                     {stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),false);
                      stateMachine.setEnabled(Step.MAIN_MENU.ordinal(),true);
                     }
                 }
             //update controllers/render states/transforms/bounds for rootNode.
             root.updateGeometricState(timer.getTimePerFrame(),true);
             canvas.draw(null);
             //Thread.yield();
            }
        canvas.getCanvasRenderer().setCurrentContext();

        // Done, do cleanup
        SoundManager.getInstance().cleanup();
        ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
        canvas.close();
        //necessary for Java Webstart
        System.exit(0);
    }

    /**
     * Initialize our scene.
     */
    private final void init(){
        canvas.setTitle("Ardor3DGameServiceProvider - close window to exit");
        // Create a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf=new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);
        // Add our awt based image loader.
        AWTImageLoader.registerLoader();
        // Set the default font
        UIComponent.setDefaultFont(getFontsList().get(2));
        // Set the location of our resources.
        try{SimpleResourceLocator srl=new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
        final TriggerAction exitAction=new TriggerAction(){
            public final void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
                exit=true;
            }
        };
        final SwitchStepAction fromRatingToInitAction=new SwitchStepAction(stateMachine,Step.CONTENT_RATING_SYSTEM,Step.INITIALIZATION);
        final SwitchStepAction fromInitToIntroAction=new SwitchStepOnlyIfTaskQueueEmptyAction(stateMachine,Step.INITIALIZATION,Step.INTRODUCTION);
        final SwitchStepAction fromIntroToMainMenuAction=new SwitchStepAction(stateMachine,Step.INTRODUCTION,Step.MAIN_MENU);
        final SwitchStepAction fromMainMenuToLoadingDisplayAction=new SwitchStepAction(stateMachine,Step.MAIN_MENU,Step.LEVEL_LOADING_DISPLAY);
        final SwitchStepAction fromLoadingDisplayToGameAction=new SwitchStepOnlyIfTaskQueueEmptyAction(stateMachine,Step.LEVEL_LOADING_DISPLAY,Step.GAME);
        
        final LoadingDisplayState loadingDisplayState;
        //create one state per step
        stateMachine.addState(new ContentRatingSystemState(canvas,physicalLayer,mouseManager,exitAction,fromRatingToInitAction));
        stateMachine.addState(new InitializationState(canvas,physicalLayer,exitAction,fromInitToIntroAction));
        stateMachine.addState(new IntroductionState(canvas,physicalLayer,exitAction,fromIntroToMainMenuAction));        
        stateMachine.addState(new MainMenuState(canvas,physicalLayer,mouseManager,exitAction,fromMainMenuToLoadingDisplayAction));
        stateMachine.addState(loadingDisplayState=new LoadingDisplayState(canvas,physicalLayer,exitAction,fromLoadingDisplayToGameAction));
        stateMachine.addState(new GameState(canvas,physicalLayer,exitAction));
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        // enqueue initialization tasks for states that are not in-game states
        // do not enqueue the task of the first state as it would be called after its display
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.INITIALIZATION.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.INTRODUCTION.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.MAIN_MENU.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.LEVEL_LOADING_DISPLAY.ordinal()));
        // do not enqueue the game task now as it is the role of the level loading display
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.GAME_OVER.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.PAUSE_MENU.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.LEVEL_END_DISPLAY.ordinal()));
        TaskManager.getInstance().enqueueTask(stateMachine.getStateInitializationTask(Step.GAME_END_DISPLAY.ordinal()));        
        // put the task that loads a level into the level loading state
        loadingDisplayState.setLevelInitializationTask(stateMachine.getStateInitializationTask(Step.GAME.ordinal()));
        // enable the first state
        stateMachine.setEnabled(Step.CONTENT_RATING_SYSTEM.ordinal(),true);
    }

    private final void updateLogicalLayer(final ReadOnlyTimer timer) {
        // check and execute any input triggers, if we are concerned with input
        stateMachine.updateLogicalLayer(timer);
    }

    @Override
    public final boolean renderUnto(final Renderer renderer){
        final boolean isOpen=!canvas.isClosing();
        if(isOpen)
            {// Draw the root and all its children.
             renderer.draw(root);
             //executes all rendering tasks queued by controllers
             GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).execute(renderer);
            }
        return(isOpen);
    }

    @Override
    public final PickResults doPick(final Ray3 pickRay){
        // Ignore
        return(null);
    }
    
    
    private static class SwitchStepAction implements TriggerAction{

        
        private final StateMachine stateMachine;
        
        private final Step sourceStep;
        
        private final Step destinationStep;
               
        
        private SwitchStepAction(final StateMachine stateMachine,final Step sourceStep,final Step destinationStep){
            this.stateMachine=stateMachine;
            this.sourceStep=sourceStep;
            this.destinationStep=destinationStep;
        }
        
        
        @Override
        public void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
            stateMachine.setEnabled(sourceStep.ordinal(),false);
            stateMachine.setEnabled(destinationStep.ordinal(),true);           
        }       
    }
    
    private static final class SwitchStepOnlyIfTaskQueueEmptyAction extends SwitchStepAction{
        
        private SwitchStepOnlyIfTaskQueueEmptyAction(final StateMachine stateMachine,final Step sourceStep,final Step destinationStep){
            super(stateMachine,sourceStep,destinationStep);
        }
        
        @Override
        public final void perform(final Canvas source,final TwoInputStates inputState,final double tpf){
            if(TaskManager.getInstance().getTaskCount()==0)
                super.perform(source,inputState,tpf);
        }
    }
}