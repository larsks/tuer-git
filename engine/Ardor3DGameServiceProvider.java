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

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.net.URISyntaxException;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
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
    private final JoglCanvas canvas;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    /**Our timer*/
    private final Timer timer;

    /**boolean allowing us to "pull the plug" from anywhere*/
    private boolean exit;

    /**root of our scene*/
    private final Node root;
    
    enum Step{RATING,
              INITIALIZATION,
              INTRODUCTION,
              MAIN_MENU,
              LOADING_DISPLAY,
              GAME,
              GAME_OVER,
              PAUSE_MENU,
              END_LEVEL_DISPLAY,
              END_GAME_DISPLAY};

    private final StateMachine stateMachine;
    
    
    public static void main(final String[] args){
    	//Disable DirectDraw under Windows in order to avoid conflicts with OpenGL
    	System.setProperty("sun.java2d.noddraw","true");
        final Ardor3DGameServiceProvider application=new Ardor3DGameServiceProvider();
        application.start();
    }

    
    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    private Ardor3DGameServiceProvider(){
        exit=false;
        timer=new Timer();
        root=new Node();
        stateMachine=new StateMachine(root);       
        // Setup a jogl canvas and canvas renderer
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        // Get the default display mode
        final DisplayMode defaultMode=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        // Choose the full-screen mode
        final DisplaySettings settings=new DisplaySettings(defaultMode.getWidth(),defaultMode.getHeight(),defaultMode.getBitDepth(),0,0,8,0,0,true,false);
        canvas=new JoglCanvas(canvasRenderer,settings);
        canvas.init();
        mouseManager=new AwtMouseManager(canvas);
        // remove the mouse cursor
        mouseManager.setGrabbed(GrabbedState.GRABBED);
        physicalLayer=new PhysicalLayer(new AwtKeyboardWrapper(canvas),new AwtMouseWrapper(canvas),new AwtFocusWrapper(canvas));
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
             if(stateMachine.isEnabled(Step.INITIALIZATION.ordinal())&&timer.getTimeInSeconds()>10)
                 {stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),false);
                  stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),true);                 
                 }
             //update controllers/render states/transforms/bounds for rootNode.
             root.updateGeometricState(timer.getTimePerFrame(),true);
             canvas.draw(null);
             //Thread.yield();
            }
        canvas.getCanvasRenderer().setCurrentContext();

        // Done, do cleanup
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
        // Set the location of our resources.
        try{SimpleResourceLocator srl=new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
            srl=new SimpleResourceLocator(getClass().getResource("/dae"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}       
        final InputTrigger escTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                exit = true;
            }
        });
        final InputTrigger returnTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN),new TriggerAction(){
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),false);
                //FIXME: rather go to the main menu
                stateMachine.setEnabled(Step.GAME.ordinal(),true);
            }
        });
        //create one state per step
        stateMachine.addState(new State());       
        stateMachine.addState(new InitializationState(canvas,physicalLayer,new InputTrigger[]{escTrigger}));
        stateMachine.addState(new IntroductionState(canvas,physicalLayer,new InputTrigger[]{escTrigger,returnTrigger}));        
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        stateMachine.addState(new GameState(canvas,physicalLayer,new InputTrigger[]{escTrigger}));
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        stateMachine.addState(new State());
        //Enable the first state
        stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),true);
    }

    private final void updateLogicalLayer(final ReadOnlyTimer timer) {
        // check and execute any input triggers, if we are concerned with input
        stateMachine.updateLogicalLayer(timer);
    }

    @Override
    public final boolean renderUnto(final Renderer renderer){
        final boolean isOpen=!canvas.isClosing();
        if(isOpen)
            {//executes all rendering tasks queued by controllers
             GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute(renderer);
             // Draw the root and all its children.
             renderer.draw(root);
            }
        return(isOpen);
    }

    @Override
    public final PickResults doPick(final Ray3 pickRay){
        // Ignore
        return(null);
    }
}