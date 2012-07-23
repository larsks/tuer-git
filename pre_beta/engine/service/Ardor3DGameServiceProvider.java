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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import com.ardor3d.annotation.MainThread;
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
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;

import engine.integration.DesktopIntegration;
import engine.integration.DesktopIntegration.OS;
import engine.renderer.ReliableContextCapabilities;
import engine.statemachine.ScenegraphStateMachine;

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
             // Disables ANGLE (Direct3D OpenGL-ES 2.0 emulation)
             System.setProperty("jogl.enable.ANGLE","false");
            }    	
        final Ardor3DGameServiceProvider application=new Ardor3DGameServiceProvider();
        application.start();
    }
    
    private abstract static class DesktopShortcutCreationRunnable implements Runnable{
    	
    	protected final String filenameWithoutExtension;
    	
    	protected final String url;

    	private DesktopShortcutCreationRunnable(final String filenameWithoutExtension,final String url){
    		this.filenameWithoutExtension=filenameWithoutExtension;
    		this.url=url;
    	}
    }
    
    private static final class LaunchDesktopShortcutCreationRunnable extends DesktopShortcutCreationRunnable{
    	
    	private LaunchDesktopShortcutCreationRunnable(final String filenameWithoutExtension,final String url){
    		super(filenameWithoutExtension,url);
    	}
    	
    	@Override
		public void run() {
    		DesktopIntegration.createLaunchDesktopShortcut(filenameWithoutExtension,url);
		}
    }
    
    private static final class UninstallDesktopShortcutCreationRunnable extends DesktopShortcutCreationRunnable{
    	
    	private UninstallDesktopShortcutCreationRunnable(final String filenameWithoutExtension,final String url){
    		super(filenameWithoutExtension,url);
    	}
    	
    	@Override
		public void run() {
    		DesktopIntegration.createUninstallDesktopShortcut(filenameWithoutExtension,url);
		}
    }
    
    private static final class ToggleScreenModeAction implements TriggerAction{

		@Override
		@MainThread
		public void perform(Canvas source, TwoInputStates inputStates, double tpf) {
			final JoglNewtWindow joglNewtWindow=(JoglNewtWindow)source;
			final GLWindow glWindow=joglNewtWindow.getNewtWindow();
			final boolean fullscreenOn=glWindow.isFullscreen();
			glWindow.setFullscreen(!fullscreenOn);
			glWindow.setUndecorated(!fullscreenOn);
		}
    	
    }
    
    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    private Ardor3DGameServiceProvider(){
        exit=false;
        timer=new Timer();
        root=new Node("root node of the game");
        
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
        //setups the canvas renderer
        final JoglCanvasRenderer canvasRenderer=new JoglCanvasRenderer(this){
        	@Override
        	public final ContextCapabilities createContextCapabilities(){
        		final ContextCapabilities defaultCaps = super.createContextCapabilities();
                final ReliableContextCapabilities realCaps = new ReliableContextCapabilities(defaultCaps);
                if(DesktopIntegration.getOperatingSystem().equals(OS.Windows))
                    {//gets some information about the OpenGL driver
                	 final String vendor=realCaps.getDisplayVendor();
                	 final String renderer=realCaps.getDisplayRenderer();
                     //checks whether Microsoft’s generic software emulation driver (OpenGL emulation through Direct3D) is installed
                	 if(vendor!=null&&renderer!=null&&vendor.equalsIgnoreCase("Microsoft Corporation")&&
                		realCaps.getDisplayRenderer().equalsIgnoreCase("GDI Generic"))
                         {//prevents the use of this crap, recommends to the end user to install a proper OpenGL driver
                		  JOptionPane.showMessageDialog(null,
                				    "TUER cannot run with your broken OpenGL driver. To resolve this problem, please download and install the very last version of your graphical card's driver from the your graphical card manufacturer (Nvidia, ATI, Intel).",
                				    "OpenGL driver error",
                				    JOptionPane.ERROR_MESSAGE);
                		  System.exit(0);
                         }
                    }
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
     * is flagged. Afterwards, the scene and GL surface are cleaned up.
     */
    private final void start(){
        init();

        //runs in this same thread
        while(!exit)
            {if(canvas.isClosing())
                 exit=true;
             else
                 {timer.update();
                  updateLogicalLayer(timer);
                  //updates controllers/render states/transforms/bounds for rootNode.
                  root.updateGeometricState(timer.getTimePerFrame(),true);
                 }
             canvas.draw(null);
             //Thread.yield();
            }
        try{
        	//drives the context current to perform a final cleanup
            canvas.getCanvasRenderer().makeCurrentContext();
            //done, does the cleanup
            ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
        }
        finally
        {//necessary for Java Web Start
         System.exit(0);
        }
        
        
    }

    /**
     * Initializes our scene.
     */
    private final void init(){
        canvas.setTitle("Ardor3DGameServiceProvider - close window to exit");
        //disables vertical synchronization for tests
        canvas.setVSyncEnabled(false);
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
        final LaunchDesktopShortcutCreationRunnable launchRunnable;
        final UninstallDesktopShortcutCreationRunnable uninstallRunnable;
        if(DesktopIntegration.isDesktopShortcutCreationSupported())
            {launchRunnable=new LaunchDesktopShortcutCreationRunnable("TUER","http://tuer.sourceforge.net/very_experimental/tuer.jnlp");
        	 uninstallRunnable=new UninstallDesktopShortcutCreationRunnable("uninstall_TUER","http://tuer.sourceforge.net/very_experimental/tuer.jnlp");
            }
        else
            {launchRunnable=null;
        	 uninstallRunnable=null;
            }
        final String creditsContent=getTextFileContent("/credits.txt");
        final String controlsContent=getTextFileContent("/controls.txt");
        final TriggerAction toggleScreenModeAction=new ToggleScreenModeAction();
        scenegraphStateMachine=new ScenegraphStateMachine(root,canvas,physicalLayer,mouseManager,exitAction,toggleScreenModeAction,launchRunnable,uninstallRunnable,creditsContent,controlsContent);
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
    
    //TODO move this method into a separate class in order to avoid mixing scene services and file services
    private final String getTextFileContent(final String path){
    	final InputStream stream=getClass().getResourceAsStream(path);        
        final String result;
        if(stream!=null)
            {BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(stream));
        	 String line;
             StringBuilder textContent=new StringBuilder();
        	 try{while((line=bufferedReader.readLine())!=null)
                     textContent.append(line+"\n");
                 bufferedReader.close();
                }
             catch(IOException ioe)
             {ioe.printStackTrace();}
        	 result=textContent.toString();
            }
        else
        	result=null;
        return(result);
    }
}