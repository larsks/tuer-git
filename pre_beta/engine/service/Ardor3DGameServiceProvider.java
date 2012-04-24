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
package engine.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.jogl.JoglNewtFocusWrapper;
import com.ardor3d.input.jogl.JoglNewtKeyboardWrapper;
import com.ardor3d.input.jogl.JoglNewtMouseManager;
import com.ardor3d.input.jogl.JoglNewtMouseWrapper;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.ContextCapabilities;
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
import com.ardor3d.util.resource.URLResourceSource;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import engine.integration.DesktopIntegration;
import engine.integration.DesktopIntegration.OS;
import engine.misc.ReliableContextCapabilities;
import engine.statemachine.ScenegraphStateMachine;
import engine.sound.SoundManager;

/**
 * General entry point of an application created by JFPSM
 * 
 */
public final class Ardor3DGameServiceProvider implements Scene{

    /**Our native window, not the GL surface itself*/
    private final NativeCanvas canvas;
    
    /**physical layer of the input system*/
    private final PhysicalLayer physicalLayer;
    
    /**mouse manager used to handle the cursor*/
    private final MouseManager mouseManager;

    /**Our timer*/
    private final Timer timer;

    /**boolean allowing us to "pull the plug" from anywhere*/
    private boolean exit;

    /**root of our scene*/
    private final Node root;

    /**state machine of the scenegraph*/    
    private ScenegraphStateMachine scenegraphStateMachine;
    
    /**
     * sound manager
     * 
     * @deprecated this field should be moved into the state machine of the scenegraph
     * */
    @Deprecated
    private final SoundManager soundManager;
    
    /**list of bitmap fonts*/
    private static ArrayList<BMFont> fontsList;
    
    
    public static void main(final String[] args){
        if(DesktopIntegration.getOperatingSystem().equals(OS.Windows))
            {// Windows-specific workarounds
             /**
              * Forces the use of the high precision timer on Windows.
              * See http://bugs.sun.com/view_bug.do?bug_id=6435126
              * */
             new Thread(new Runnable(){                
                            @Override
                            public void run(){
                                while(true) 
                                    {try{Thread.sleep(Integer.MAX_VALUE);}
                                     catch(InterruptedException ie)
                                     {ie.printStackTrace();}
                                    }
                            }
                        }, "Microsoft Windows Sleeper (see bug 6435126)") {
                        {this.setDaemon(true);}
             }.start();
             // Disables DirectDraw under Windows in order to avoid conflicts with OpenGL
             System.setProperty("sun.java2d.noddraw","true");
             // Disables Direct3D under Windows in order to avoid conflicts with OpenGL
             System.setProperty("sun.java2d.d3d","false");
            }    	
        final Ardor3DGameServiceProvider application=new Ardor3DGameServiceProvider();
        application.start();
    }

    /**
     * Creates font lists
     * 
     * @return font lists
     * @deprecated this service should be moved to the state machine of the scenegraph
     */
    @Deprecated
    private final static ArrayList<BMFont> createFontsList(){
        final ArrayList<BMFont> fontsList=new ArrayList<BMFont>();
        try{fontsList.add(new BMFont(new URLResourceSource(Ardor3DGameServiceProvider.class.getResource("/fonts/DejaVuSansCondensed-20-bold-regular.fnt")),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        try{fontsList.add(new BMFont(new URLResourceSource(Ardor3DGameServiceProvider.class.getResource("/fonts/Computerfont-35-medium-regular.fnt")),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        try{fontsList.add(new BMFont(new URLResourceSource(Ardor3DGameServiceProvider.class.getResource("/fonts/arial-16-bold-regular.fnt")),false));}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        return(fontsList);
    }
    
    /**
     * Returns font lists
     * 
     * @return font lists
     * @deprecated this service should be moved to the state machine of the scenegraph
     */
    @Deprecated
    public static final List<BMFont> getFontsList(){
        if(fontsList==null)
            fontsList=createFontsList();
        return(Collections.unmodifiableList(fontsList));
    }
    
    
    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    private Ardor3DGameServiceProvider(){
        exit=false;
        timer=new Timer();
        root=new Node("root node of the game");
        soundManager=new SoundManager();
        
        //retrieves some parameters of the display
        Display display=NewtFactory.createDisplay(null);
        Screen screen=NewtFactory.createScreen(display,0);
        screen.addReference();
        final int screenWidth=screen.getWidth();
        final int screenHeight=screen.getHeight();
        final int bitDepth=screen.getCurrentScreenMode().getMonitorMode().getSurfaceSize().getBitsPerPixel();
        screen.removeReference();
        
        //initializes the settings, the full-screen mode is enabled
        final DisplaySettings settings=new DisplaySettings(screenWidth,screenHeight,bitDepth,0,0,24,0,0,true,false);
        // Setup the canvas renderer
        final JoglCanvasRenderer canvasRenderer=new JoglCanvasRenderer(this){
        	@Override
        	public final ContextCapabilities createContextCapabilities(){
        		final ContextCapabilities defaultCaps = super.createContextCapabilities();
                final ReliableContextCapabilities realCaps = new ReliableContextCapabilities(defaultCaps);
                return(realCaps);
        	}
        };
        //creates a canvas      
        canvas=new JoglNewtWindow(canvasRenderer,settings);
        canvas.init();
        mouseManager=new JoglNewtMouseManager((JoglNewtWindow)canvas);
        //removes the mouse cursor
        mouseManager.setGrabbed(GrabbedState.GRABBED);
        physicalLayer=new PhysicalLayer(new JoglNewtKeyboardWrapper((JoglNewtWindow)canvas),new JoglNewtMouseWrapper((JoglNewtWindow)canvas,mouseManager),new JoglNewtFocusWrapper((JoglNewtWindow)canvas));
    }

    
    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    private final void start(){
        init();

        //runs in this same thread
        while(!exit)
            {if(canvas.isClosing())
                 {exit=true;
                  return;
                 }
             updateLogicalLayer(timer);
             timer.update();
             //updates controllers/render states/transforms/bounds for rootNode.
             root.updateGeometricState(timer.getTimePerFrame(),true);
             canvas.draw(null);
             //Thread.yield();
            }
        //drives the context current to perform a final cleanup
        canvas.getCanvasRenderer().makeCurrentContext();

        //done, does the cleanup
        soundManager.cleanup();
        ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
        //necessary for Java Web Start
        System.exit(0);
    }

    /**
     * Initialize our scene.
     */
    private final void init(){
        canvas.setTitle("Ardor3DGameServiceProvider - close window to exit");
        //creates a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf=new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);
        //adds our AWT-based image loader.
        AWTImageLoader.registerLoader();
        //sets the default font
        //UIComponent.setDefaultFont(getFontsList().get(2));
        //sets the location of our resources.
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
        scenegraphStateMachine=new ScenegraphStateMachine(root,canvas,physicalLayer,mouseManager,soundManager,exitAction);
    }

    private final void updateLogicalLayer(final ReadOnlyTimer timer) {
        // checks and executes any input triggers, if we are concerned with input
        scenegraphStateMachine.updateLogicalLayer(timer);
    }

    @Override
    public final boolean renderUnto(final Renderer renderer){
        final boolean isOpen=!canvas.isClosing();
        if(isOpen)
            {//draws the root and all its children
             root.onDraw(renderer);
             //executes all update tasks queued by the controllers
             GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.UPDATE).execute(renderer);
             //executes all rendering tasks queued by the controllers
             GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).execute(renderer);
            }
        return(isOpen);
    }

    @Override
    public final PickResults doPick(final Ray3 pickRay){
        //ignores
        return(null);
    }
}