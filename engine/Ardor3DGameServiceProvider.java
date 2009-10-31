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
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.control.FirstPersonControl;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * General entry point of an application created by JFPSM
 * 
 */
public class Ardor3DGameServiceProvider implements Scene {

    // Our native window, not the gl surface itself.
    private final JoglCanvas _canvas;

    private final PhysicalLayer _physicalLayer;

    private final MouseManager _mouseManager;

    private final Vector3 _worldUp = new Vector3(0, 1, 0);

    // Our timer.
    private final Timer _timer = new Timer();

    // A boolean allowing us to "pull the plug" from anywhere.
    private boolean _exit = false;

    // The root of our scene
    private final Node _root = new Node();
    
    //FIXME: remove fixed indices, replace them by these enumerants
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

    private final StateMachine _stateMachine = new StateMachine(_root);
    
    private static final String initialLogoPath = "initial_logo.png";
    
    
    public static void main(final String[] args){
    	//Disable DirectDraw under Windows in order to avoid conflicts with OpenGL
    	System.setProperty("sun.java2d.noddraw","true");
        final Ardor3DGameServiceProvider application = new Ardor3DGameServiceProvider();
        application.start();
    }

    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    public Ardor3DGameServiceProvider() {
        // Setup a jogl canvas and canvas renderer
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        // Get the default display mode
        final DisplayMode defaultMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDisplayMode();
        // Choose the full-screen mode
        final DisplaySettings settings = new DisplaySettings(defaultMode.getWidth(), defaultMode.getHeight(),
                defaultMode.getBitDepth(), 0, 0, 8, 0, 0, true, false);
        _canvas = new JoglCanvas(canvasRenderer, settings);
        _canvas.init();
        _mouseManager = new AwtMouseManager(_canvas);
        // remove the mouse cursor
        _mouseManager.setGrabbed(GrabbedState.GRABBED);
        _physicalLayer = new PhysicalLayer(new AwtKeyboardWrapper(_canvas), new AwtMouseWrapper(_canvas),
                new AwtFocusWrapper(_canvas));
    }

    
    final StateMachine getStateMachine(){
        return(_stateMachine);
    }
    
    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    protected final void start() {
        init();

        // Run in this same thread.
        while (!_exit) {
            if (_canvas.isClosing()) {
                _exit = true;
                return;
            }

            updateLogicalLayer(_timer);
            _timer.update();
            
            if(_stateMachine.isEnabled(0) && _timer.getTimeInSeconds() > 15){
                _stateMachine.setEnabled(0,false);
                _stateMachine.setEnabled(1,true);
            }

            // Update controllers/render states/transforms/bounds for rootNode.
            _root.updateGeometricState(_timer.getTimePerFrame(), true);
            _canvas.draw(null);
            Thread.yield();
        }
        _canvas.getCanvasRenderer().setCurrentContext();

        // Done, do cleanup
        ContextGarbageCollector.doFinalCleanup(_canvas.getCanvasRenderer().getRenderer());
        _canvas.close();
        //necessary for Java Webstart
        System.exit(0);
    }

    /**
     * Initialize our scene.
     */
    protected void init() {
        _canvas.setTitle("Ardor3DGameServiceProvider - close window to exit");

        // Make a box...
        final Box _initialBox = new Box("Box", Vector3.ZERO, 5, 5, 5);

        // Setup a bounding box for it.
        _initialBox.setModelBound(new BoundingBox());

        // Set its location in space.
        _initialBox.setTranslation(new Vector3(0, 0, -15));       
        
        _stateMachine.addState();
        // Add the box to the initial state
        _stateMachine.attachChild(0,_initialBox);
        //Enable the first state
        _stateMachine.setEnabled(0,true);
        
        // Add a second state to the state machine
        _stateMachine.addState();
        // bind the physical layer to the logical layer
        _stateMachine.getLogicalLayer(0).registerInput(_canvas, _physicalLayer);
        _stateMachine.getLogicalLayer(1).registerInput(_canvas, _physicalLayer);

        final InputTrigger escTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                _exit = true;
            }
        });
        _stateMachine.getLogicalLayer(0).registerTrigger(escTrigger);
        _stateMachine.getLogicalLayer(1).registerTrigger(escTrigger);
        
        //Add a state for the game itself
        _stateMachine.addState();
        _stateMachine.getLogicalLayer(2).registerInput(_canvas, _physicalLayer);
        _stateMachine.getLogicalLayer(2).registerTrigger(escTrigger);
        
        // drag only at false to remove the need of pressing a button to move
        FirstPersonControl.setupTriggers(_stateMachine.getLogicalLayer(2), _worldUp, false);
        //FIXME: this is a temporary trigger to enter the game, remove it, put a true menu
        final InputTrigger returnTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                _stateMachine.setEnabled(1,false);
                _stateMachine.setEnabled(2,true);
            }
        });
        _stateMachine.getLogicalLayer(1).registerTrigger(returnTrigger);
        // set it to rotate:
        _initialBox.addController(new SpatialController<Box>() {
            private static final long serialVersionUID = 1L;
            private final Vector3 _axis = new Vector3(0, 1, 0).normalizeLocal();
            private final Matrix3 _rotate = new Matrix3();
            private double _angle = 0;

            public void update(final double time, final Box caller) {
                // update our rotation
                _angle = _angle + (_timer.getTimePerFrame() * 25);
                if (_angle > 180) {
                    _angle = -180;
                }

                _rotate.fromAngleNormalAxis(_angle * MathUtils.DEG_TO_RAD, _axis);
                _initialBox.setRotation(_rotate);
            }
        });

        // Add our awt based image loader.
        AWTImageLoader.registerLoader();

        // Set the location of our example resources.
        try {
            SimpleResourceLocator srl = new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            //srl = new SimpleResourceLocator(getClass().getResource("/"));
            //ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
        } catch (final URISyntaxException ex) {
            ex.printStackTrace();
        }

        // Create a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        _root.setRenderState(buf);

        // Create a texture with the initial logo
        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(initialLogoPath, Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true));
        _initialBox.setRenderState(ts);
    }

    private void updateLogicalLayer(final ReadOnlyTimer timer) {
        // check and execute any input triggers, if we are concerned with input
        _stateMachine.updateLogicalLayer(timer);
    }

    // ------ Scene methods ------

    public boolean renderUnto(final Renderer renderer) {
        if (!_canvas.isClosing()) {

            // Draw the root and all its children.
            renderer.draw(_root);

            return true;
        }
        return false;
    }

    public PickResults doPick(final Ray3 pickRay) {
        // Ignore
        return null;
    }
}